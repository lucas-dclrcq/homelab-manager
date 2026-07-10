<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useGetApiStatisticsTopMedia } from '../../api/service/homelab'
import type { GetApiStatisticsTopMediaParams } from '../../api/model'
import BaseCard from '../ui/BaseCard.vue'
import BaseSpinner from '../ui/BaseSpinner.vue'
import { formatHours, type StatsPeriodValue } from '../../lib/statistics'

type SortKey =
  | 'NAME'
  | 'PLAYS'
  | 'WATCH_TIME'
  | 'UNIQUE_VIEWERS'
  | 'COMPLETION_RATE'
  | 'BINGE_SCORE'

interface Column {
  sortKey: SortKey
  label: string
  tooltip?: string
}

const props = defineProps<{ period: StatsPeriodValue }>()

const kind = ref<'SERIES' | 'MOVIE'>('SERIES')
const sortKey = ref<SortKey>('PLAYS')
const sortDir = ref<'ASC' | 'DESC'>('DESC')

const columns = computed<Column[]>(() => [
  { sortKey: 'NAME', label: kind.value === 'SERIES' ? 'Série' : 'Film' },
  { sortKey: 'PLAYS', label: 'Visionnages' },
  { sortKey: 'WATCH_TIME', label: 'Heures visionnées' },
  { sortKey: 'UNIQUE_VIEWERS', label: 'Visionneurs' },
  { sortKey: 'COMPLETION_RATE', label: 'Complétion' },
  ...(kind.value === 'SERIES'
    ? [
        {
          sortKey: 'BINGE_SCORE' as SortKey,
          label: 'Binge score',
          tooltip:
            "Nombre moyen d'épisodes regardés par jour actif, normalisé : 6 épisodes/jour = 100",
        },
      ]
    : []),
])

function toggleSort(key: SortKey) {
  if (sortKey.value === key) {
    sortDir.value = sortDir.value === 'DESC' ? 'ASC' : 'DESC'
  } else {
    sortKey.value = key
    sortDir.value = key === 'NAME' ? 'ASC' : 'DESC'
  }
}

// Le binge score n'existe pas côté films : retomber sur le tri par défaut
watch(kind, () => {
  if (kind.value === 'MOVIE' && sortKey.value === 'BINGE_SCORE') {
    sortKey.value = 'PLAYS'
    sortDir.value = 'DESC'
  }
})

const params = computed<GetApiStatisticsTopMediaParams>(() => ({
  period: props.period,
  type: kind.value,
  sort: sortKey.value,
  order: sortDir.value,
}))

const { data: media, isPending } = useGetApiStatisticsTopMedia(params, {
  query: { staleTime: 60_000 },
})

function formatRate(rate?: number | null): string {
  return rate == null ? '—' : `${rate.toLocaleString('fr-FR')} %`
}
</script>

<template>
  <BaseCard>
    <div class="mb-4 flex flex-wrap items-center justify-between gap-3">
      <h2 class="font-display text-xl font-bold">Les plus regardés</h2>
      <div
        class="flex overflow-hidden rounded-xl border-[1.5px] border-line text-sm font-bold"
      >
        <button
          type="button"
          class="cursor-pointer px-4 py-1.5 transition-colors"
          :class="
            kind === 'SERIES' ? 'bg-amber text-ink' : 'bg-white text-ink-soft'
          "
          @click="kind = 'SERIES'"
        >
          Séries
        </button>
        <button
          type="button"
          class="cursor-pointer px-4 py-1.5 transition-colors"
          :class="
            kind === 'MOVIE' ? 'bg-amber text-ink' : 'bg-white text-ink-soft'
          "
          @click="kind = 'MOVIE'"
        >
          Films
        </button>
      </div>
    </div>

    <BaseSpinner v-if="isPending" />
    <p v-else-if="!media?.length" class="text-sm text-mute">
      Rien à afficher sur cette période.
    </p>
    <div v-else class="overflow-x-auto">
      <table class="w-full text-left text-sm">
        <thead>
          <tr class="border-b-[1.5px] border-line text-xs text-mute">
            <th
              v-for="column in columns"
              :key="column.sortKey"
              class="py-2 pr-3"
            >
              <button
                type="button"
                class="inline-flex cursor-pointer items-center gap-1 font-bold transition-colors hover:text-ink"
                :class="{ 'text-amber-deep': sortKey === column.sortKey }"
                :title="column.tooltip"
                @click="toggleSort(column.sortKey)"
              >
                {{ column.label }}
                <span v-if="sortKey === column.sortKey" aria-hidden="true">
                  {{ sortDir === 'DESC' ? '▼' : '▲' }}
                </span>
              </button>
            </th>
          </tr>
        </thead>
        <tbody class="divide-y divide-line">
          <tr v-for="(item, index) in media" :key="item.name">
            <td class="py-2.5 pr-3 font-bold">
              <span class="mr-2 text-mute">{{ index + 1 }}.</span
              >{{ item.name }}
            </td>
            <td class="py-2.5 pr-3">{{ item.plays }}</td>
            <td class="py-2.5 pr-3">
              {{ formatHours(item.watchTimeSeconds) }}
            </td>
            <td class="py-2.5 pr-3">{{ item.uniqueViewers }}</td>
            <td class="py-2.5 pr-3">{{ formatRate(item.completionRate) }}</td>
            <td v-if="kind === 'SERIES'" class="py-2.5">
              <span
                class="inline-block rounded-full bg-amber-soft px-2.5 py-0.5 text-xs font-bold text-amber-deep"
              >
                {{ item.bingeScore ?? '—' }}
              </span>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </BaseCard>
</template>
