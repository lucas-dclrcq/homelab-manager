<script setup lang="ts">
import { computed, ref } from 'vue'
import {
  ArcElement,
  Chart,
  DoughnutController,
  Legend,
  Tooltip,
} from 'chart.js'
import { Doughnut } from 'vue-chartjs'
import {
  useGetApiStats,
  useGetApiTimeline,
} from '../../../api/service/homelab'
import BaseCard from '../../ui/BaseCard.vue'
import BaseSpinner from '../../ui/BaseSpinner.vue'
import StatCard from '../../ui/StatCard.vue'
import { formatBytes, formatDateTime } from '../../../lib/format'
import { downloadEventLabel } from '../../../lib/statistics'
import { hoohootChartColors, hoohootLegend } from '../../../lib/charts'

Chart.register(ArcElement, DoughnutController, Tooltip, Legend)

const { data: stats } = useGetApiStats({ query: { staleTime: 60_000 } })

// Disque : pourcentage utilisé
const diskPercent = computed(() => {
  const total = stats.value?.diskTotalBytes ?? 0
  if (total === 0) return 0
  return Math.round((stats.value!.diskUsedBytes / total) * 100)
})

// Répartition des derniers téléchargements par type
const recentParams = { page: 0, pageSize: 100 }
const { data: recent } = useGetApiTimeline(recentParams, {
  query: { staleTime: 60_000 },
})

const byType = computed(() => {
  const counts = new Map<string, number>()
  for (const event of recent.value?.items ?? []) {
    counts.set(event.eventType, (counts.get(event.eventType) ?? 0) + 1)
  }
  return [...counts.entries()].sort((a, b) => b[1] - a[1])
})

const palette = [
  hoohootChartColors.amber,
  hoohootChartColors.dusk,
  hoohootChartColors.sage,
  hoohootChartColors.sky,
  hoohootChartColors.berry,
]

const typeChartData = computed(() => ({
  labels: byType.value.map(([type]) => downloadEventLabel(type)),
  datasets: [
    {
      data: byType.value.map(([, count]) => count),
      backgroundColor: byType.value.map(
        (_, index) => palette[index % palette.length],
      ),
      borderColor: '#fdf9f2',
      borderWidth: 3,
    },
  ],
}))

const typeChartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: { legend: hoohootLegend },
}

// Timeline paginée
const page = ref(0)
const pageSize = 15
const timelineParams = computed(() => ({ page: page.value, pageSize }))
const { data: timeline, isPending: timelinePending } = useGetApiTimeline(
  timelineParams,
  { query: { staleTime: 60_000 } },
)
const totalPages = computed(() => timeline.value?.totalPages ?? 0)

function detailLine(details: Record<string, string>): string {
  return [details.quality, details.provider, details.artist]
    .filter(Boolean)
    .join(' · ')
}
</script>

<template>
  <div class="flex flex-col gap-8">
    <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-4">
      <StatCard
        label="Films"
        :value="stats ? String(stats.movieCount) : '…'"
        icon="film"
        tone="amber"
      />
      <StatCard
        label="Séries"
        :value="stats ? String(stats.seriesCount) : '…'"
        icon="tv"
        tone="dusk"
      />
      <StatCard
        label="Épisodes"
        :value="stats ? String(stats.episodeCount) : '…'"
        icon="clapperboard"
        tone="sage"
      />
      <StatCard
        label="Stockage utilisé"
        :value="stats ? formatBytes(stats.diskUsedBytes) : '…'"
        icon="hard-drive"
        tone="sky"
      />
    </div>

    <BaseCard v-if="stats">
      <div class="mb-2 flex items-baseline justify-between">
        <h2 class="font-display text-xl font-bold">Stockage</h2>
        <span class="text-sm text-mute">
          {{ formatBytes(stats.diskUsedBytes) }} /
          {{ formatBytes(stats.diskTotalBytes) }}
          ({{ diskPercent }} %)
        </span>
      </div>
      <div class="h-4 overflow-hidden rounded-full bg-line">
        <div
          class="h-full rounded-full bg-amber transition-all"
          :style="{ width: `${diskPercent}%` }"
        ></div>
      </div>
      <p class="mt-2 text-xs text-mute">
        {{ formatBytes(stats.diskFreeBytes) }} disponibles
      </p>
    </BaseCard>

    <div class="grid gap-6 lg:grid-cols-[1fr_1.4fr]">
      <BaseCard>
        <h2 class="mb-1 font-display text-xl font-bold">Téléchargements</h2>
        <p class="mb-4 text-xs text-mute">Répartition des 100 derniers</p>
        <p v-if="!byType.length" class="text-sm text-mute">
          Aucun téléchargement enregistré.
        </p>
        <div v-else class="h-64">
          <Doughnut :data="typeChartData" :options="typeChartOptions" />
        </div>
      </BaseCard>

      <BaseCard>
        <h2 class="mb-4 font-display text-xl font-bold">
          Derniers téléchargements
        </h2>
        <BaseSpinner v-if="timelinePending" />
        <p v-else-if="!timeline?.items.length" class="text-sm text-mute">
          Aucun téléchargement enregistré.
        </p>
        <div v-else>
          <ul class="flex flex-col divide-y divide-line text-sm">
            <li
              v-for="event in timeline.items"
              :key="event.id"
              class="flex items-center gap-3 py-2"
            >
              <span
                class="shrink-0 rounded-full bg-amber-soft px-2 py-0.5 text-[11px] font-bold text-amber-deep"
              >
                {{ downloadEventLabel(event.eventType) }}
              </span>
              <span class="min-w-0 flex-1 truncate">
                <span class="font-bold">{{ event.title }}</span>
                <span
                  v-if="detailLine(event.details)"
                  class="ml-1.5 text-xs text-mute"
                >
                  {{ detailLine(event.details) }}
                </span>
              </span>
              <span class="shrink-0 text-xs text-mute whitespace-nowrap">
                {{ formatDateTime(`${event.occurredAt}Z`) }}
              </span>
            </li>
          </ul>

          <div
            v-if="totalPages > 1"
            class="mt-4 flex items-center justify-between gap-3 text-sm"
          >
            <button
              type="button"
              class="cursor-pointer rounded-xl border-[1.5px] border-line px-3.5 py-1.5 font-bold text-ink-soft transition-colors enabled:hover:bg-amber-soft/50 disabled:cursor-default disabled:opacity-40"
              :disabled="page === 0"
              @click="page -= 1"
            >
              Précédent
            </button>
            <span class="text-mute">Page {{ page + 1 }} / {{ totalPages }}</span>
            <button
              type="button"
              class="cursor-pointer rounded-xl border-[1.5px] border-line px-3.5 py-1.5 font-bold text-ink-soft transition-colors enabled:hover:bg-amber-soft/50 disabled:cursor-default disabled:opacity-40"
              :disabled="page + 1 >= totalPages"
              @click="page += 1"
            >
              Suivant
            </button>
          </div>
        </div>
      </BaseCard>
    </div>
  </div>
</template>
