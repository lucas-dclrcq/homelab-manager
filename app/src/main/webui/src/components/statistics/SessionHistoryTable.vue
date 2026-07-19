<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useGetApiStatisticsHistory } from '../../api/service/homelab'
import type { GetApiStatisticsHistoryParams } from '../../api/model'
import BaseSpinner from '../ui/BaseSpinner.vue'
import { formatDateTime } from '../../lib/format'
import {
  formatEpisodeRef,
  formatWatchTime,
  platformLabel,
  playbackMethodLabel,
  type StatsPeriodValue,
} from '../../lib/statistics'

const props = withDefaults(
  defineProps<{
    period: StatsPeriodValue
    pageSize?: number
    paginated?: boolean
  }>(),
  { pageSize: 20, paginated: true },
)

const page = ref(0)

// Repartir de la première page quand la période change
watch(
  () => props.period,
  () => {
    page.value = 0
  },
)

const params = computed<GetApiStatisticsHistoryParams>(() => ({
  period: props.period,
  page: page.value,
  pageSize: props.pageSize,
}))

const { data, isPending } = useGetApiStatisticsHistory(params, {
  query: { staleTime: 60_000 },
})

const totalPages = computed(() => data.value?.totalPages ?? 0)

function localDateTime(value: string): string {
  // Les timestamps sont stockés en UTC sans suffixe : on force la zone
  return formatDateTime(`${value}Z`)
}

function completion(progressPercent?: number | null): string {
  return progressPercent == null
    ? '—'
    : `${Math.round(progressPercent)} %`
}
</script>

<template>
  <div>
    <BaseSpinner v-if="isPending" />
    <p v-else-if="!data?.items.length" class="text-sm text-mute">
      Aucune session sur cette période.
    </p>
    <div v-else class="overflow-x-auto">
      <table class="w-full text-left text-sm">
        <thead>
          <tr class="border-b-[1.5px] border-line text-xs text-mute">
            <th class="py-2 pr-3 font-bold">Utilisateur</th>
            <th class="py-2 pr-3 font-bold">Titre</th>
            <th class="py-2 pr-3 font-bold">Quand</th>
            <th class="py-2 pr-3 font-bold">Durée</th>
            <th class="py-2 pr-3 font-bold">Complétion</th>
            <th class="py-2 pr-3 font-bold">Appareil</th>
            <th class="py-2 font-bold">Qualité</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-line">
          <tr v-for="(entry, index) in data.items" :key="`${entry.startedAt}-${index}`">
            <td class="py-2.5 pr-3 font-bold whitespace-nowrap">
              {{ entry.userName }}
            </td>
            <td class="py-2.5 pr-3">
              <span class="font-bold">{{
                entry.seriesName ?? entry.itemName
              }}</span>
              <span
                v-if="entry.seriesName"
                class="ml-1.5 text-xs text-mute"
              >
                {{ formatEpisodeRef(entry.seasonNumber, entry.episodeNumber) }}
              </span>
            </td>
            <td class="py-2.5 pr-3 whitespace-nowrap text-ink-soft">
              {{ localDateTime(entry.startedAt) }}
            </td>
            <td class="py-2.5 pr-3 whitespace-nowrap">
              {{ formatWatchTime(entry.playDurationSeconds) }}
            </td>
            <td class="py-2.5 pr-3">{{ completion(entry.progressPercent) }}</td>
            <td class="py-2.5 pr-3 whitespace-nowrap text-ink-soft">
              {{ platformLabel(entry.platform) }}
            </td>
            <td class="py-2.5 whitespace-nowrap">
              <span v-if="entry.resolution" class="text-ink-soft">{{
                entry.resolution
              }}</span>
              <span
                v-if="entry.playMethod"
                class="ml-1.5 inline-block rounded-full px-2 py-0.5 text-[11px] font-bold"
                :class="
                  entry.playMethod === 'TRANSCODE'
                    ? 'bg-berry-soft text-berry'
                    : 'bg-sage-soft text-sage'
                "
              >
                {{ playbackMethodLabel(entry.playMethod) }}
              </span>
              <span
                v-if="!entry.resolution && !entry.playMethod"
                class="text-mute"
                >—</span
              >
            </td>
          </tr>
        </tbody>
      </table>

      <div
        v-if="paginated && totalPages > 1"
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
        <span class="text-mute">
          Page {{ page + 1 }} / {{ totalPages }}
        </span>
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
  </div>
</template>
