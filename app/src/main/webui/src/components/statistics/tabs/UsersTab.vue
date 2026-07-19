<script setup lang="ts">
import { computed, ref } from 'vue'
import { useGetApiStatisticsTopUsers } from '../../../api/service/homelab'
import BaseCard from '../../ui/BaseCard.vue'
import BaseSpinner from '../../ui/BaseSpinner.vue'
import TopUsersPodium from '../TopUsersPodium.vue'
import { type StatsPeriodValue, type TopUsersMetric } from '../../../lib/statistics'

const props = defineProps<{ period: StatsPeriodValue }>()

const topUsersMetric = ref<TopUsersMetric>('time')
const params = computed(() => ({ period: props.period }))

const { data: topUsers, isPending } = useGetApiStatisticsTopUsers(params, {
  query: { staleTime: 60_000 },
})
</script>

<template>
  <BaseCard>
    <div class="mb-4 flex flex-wrap items-center justify-between gap-3">
      <h2 class="font-display text-xl font-bold">Top spectateurs</h2>
      <div
        class="flex overflow-hidden rounded-xl border-[1.5px] border-line text-sm font-bold"
      >
        <button
          type="button"
          class="cursor-pointer px-4 py-1.5 transition-colors"
          :class="
            topUsersMetric === 'time'
              ? 'bg-amber text-ink'
              : 'bg-white text-ink-soft'
          "
          @click="topUsersMetric = 'time'"
        >
          Heures visionnées
        </button>
        <button
          type="button"
          class="cursor-pointer px-4 py-1.5 transition-colors"
          :class="
            topUsersMetric === 'plays'
              ? 'bg-amber text-ink'
              : 'bg-white text-ink-soft'
          "
          @click="topUsersMetric = 'plays'"
        >
          Visionnages
        </button>
      </div>
    </div>
    <BaseSpinner v-if="isPending" />
    <p v-else-if="!topUsers?.length" class="text-sm text-mute">
      Personne n'a rien regardé sur cette période. Suspect.
    </p>
    <TopUsersPodium v-else :users="topUsers" :metric="topUsersMetric" />
  </BaseCard>
</template>
