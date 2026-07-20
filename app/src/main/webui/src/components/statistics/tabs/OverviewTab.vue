<script setup lang="ts">
import { computed } from 'vue'
import {
  useGetApiStatisticsPlaysOverTime,
  useGetApiStatisticsSummary,
} from '../../../api/service/homelab'
import StatCard from '../../ui/StatCard.vue'
import BaseCard from '../../ui/BaseCard.vue'
import BaseSpinner from '../../ui/BaseSpinner.vue'
import NowPlayingCard from '../NowPlayingCard.vue'
import PlaysOverTimeChart from '../PlaysOverTimeChart.vue'
import SessionHistoryTable from '../SessionHistoryTable.vue'
import { formatWatchTime, type StatsPeriodValue } from '../../../lib/statistics'

const props = defineProps<{ period: StatsPeriodValue }>()

const params = computed(() => ({ period: props.period }))
const queryOptions = { query: { staleTime: 60_000 } }

const { data: summary } = useGetApiStatisticsSummary(params, queryOptions)
const { data: playsOverTime, isPending: overTimePending } =
  useGetApiStatisticsPlaysOverTime(params, queryOptions)

const peakHourValue = computed(() => {
  const peakHour = summary.value?.peakHour
  return peakHour == null ? '—' : `${peakHour}h`
})
</script>

<template>
  <div class="flex flex-col gap-8">
    <NowPlayingCard />

    <div class="grid gap-4 sm:grid-cols-2 lg:grid-cols-5">
      <StatCard
        label="Temps de visionnage"
        :value="summary ? formatWatchTime(summary.totalWatchTimeSeconds) : '…'"
        icon="clock"
        tone="amber"
      />
      <StatCard
        label="Visionnages"
        :value="summary ? String(summary.playCount) : '…'"
        icon="play"
        tone="dusk"
      />
      <StatCard
        label="Médias complétés"
        :value="summary ? String(summary.completedItems) : '…'"
        icon="circle-check"
        tone="sage"
      />
      <StatCard
        label="Spectateurs actifs"
        :value="summary ? String(summary.activeUsers) : '…'"
        icon="users"
        tone="sky"
      />
      <StatCard
        label="Heure de pointe"
        :value="summary ? peakHourValue : '…'"
        icon="activity"
        tone="berry"
      />
    </div>

    <BaseCard>
      <h2 class="mb-4 font-display text-xl font-bold">Lectures dans le temps</h2>
      <BaseSpinner v-if="overTimePending" />
      <PlaysOverTimeChart v-else-if="playsOverTime" :over-time="playsOverTime" />
    </BaseCard>

    <BaseCard>
      <h2 class="mb-4 font-display text-xl font-bold">Activité récente</h2>
      <SessionHistoryTable
        :period="period"
        :page-size="10"
        :paginated="false"
      />
    </BaseCard>
  </div>
</template>
