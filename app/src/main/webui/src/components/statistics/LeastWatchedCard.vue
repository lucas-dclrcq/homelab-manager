<script setup lang="ts">
import { computed, ref } from 'vue'
import { useGetApiStatisticsTopMedia } from '../../api/service/homelab'
import type { GetApiStatisticsTopMediaParams } from '../../api/model'
import BaseCard from '../ui/BaseCard.vue'
import BaseSpinner from '../ui/BaseSpinner.vue'
import { formatHours, type StatsPeriodValue } from '../../lib/statistics'

const props = defineProps<{ period: StatsPeriodValue }>()

const kind = ref<'SERIES' | 'MOVIE'>('SERIES')

const params = computed<GetApiStatisticsTopMediaParams>(() => ({
  period: props.period,
  type: kind.value,
  sort: 'PLAYS',
  order: 'ASC',
}))

const { data: media, isPending } = useGetApiStatisticsTopMedia(params, {
  query: { staleTime: 60_000 },
})
</script>

<template>
  <BaseCard>
    <div class="mb-4 flex flex-wrap items-center justify-between gap-3">
      <div>
        <h2 class="font-display text-xl font-bold">Les moins regardés</h2>
        <p class="text-xs text-mute">Parmi les médias déjà lancés au moins une fois</p>
      </div>
      <div
        class="flex overflow-hidden rounded-xl border-[1.5px] border-line text-sm font-bold"
      >
        <button
          type="button"
          class="cursor-pointer px-4 py-1.5 transition-colors"
          :class="kind === 'SERIES' ? 'bg-amber text-ink' : 'bg-white text-ink-soft'"
          @click="kind = 'SERIES'"
        >
          Séries
        </button>
        <button
          type="button"
          class="cursor-pointer px-4 py-1.5 transition-colors"
          :class="kind === 'MOVIE' ? 'bg-amber text-ink' : 'bg-white text-ink-soft'"
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
    <ol v-else class="flex flex-col divide-y divide-line text-sm">
      <li
        v-for="(item, index) in media.slice(0, 10)"
        :key="item.name"
        class="flex items-center gap-3 py-2"
      >
        <span class="w-6 shrink-0 text-right font-bold text-mute">
          {{ index + 1 }}
        </span>
        <span class="min-w-0 flex-1 truncate font-bold">{{ item.name }}</span>
        <span class="shrink-0 text-ink-soft">{{ item.plays }} lectures</span>
        <span class="w-24 shrink-0 text-right text-xs text-mute">
          {{ formatHours(item.watchTimeSeconds) }}
        </span>
      </li>
    </ol>
  </BaseCard>
</template>
