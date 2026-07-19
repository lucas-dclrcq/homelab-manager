<script setup lang="ts">
import { computed } from 'vue'
import {
  useGetApiStatisticsActivityByHour,
  useGetApiStatisticsActivityByWeekday,
  useGetApiStatisticsActivityHeatmap,
  useGetApiStatisticsPlatforms,
} from '../../../api/service/homelab'
import BaseCard from '../../ui/BaseCard.vue'
import BaseSpinner from '../../ui/BaseSpinner.vue'
import WeekdayActivityChart from '../WeekdayActivityChart.vue'
import HourlyActivityChart from '../HourlyActivityChart.vue'
import PlatformsChart from '../PlatformsChart.vue'
import ActivityHeatmap from '../ActivityHeatmap.vue'
import SessionHistoryTable from '../SessionHistoryTable.vue'
import { type StatsPeriodValue } from '../../../lib/statistics'

const props = defineProps<{ period: StatsPeriodValue }>()

const params = computed(() => ({ period: props.period }))
const queryOptions = { query: { staleTime: 60_000 } }

const { data: weekdayActivity, isPending: weekdayPending } =
  useGetApiStatisticsActivityByWeekday(params, queryOptions)
const { data: hourlyActivity, isPending: hourlyPending } =
  useGetApiStatisticsActivityByHour(params, queryOptions)
const { data: platforms, isPending: platformsPending } =
  useGetApiStatisticsPlatforms(params, queryOptions)
const { data: heatmap, isPending: heatmapPending } =
  useGetApiStatisticsActivityHeatmap(params, queryOptions)
</script>

<template>
  <div class="flex flex-col gap-8">
    <BaseCard>
      <h2 class="mb-4 font-display text-xl font-bold">
        Activité par jour et par heure
      </h2>
      <BaseSpinner v-if="heatmapPending" />
      <p v-else-if="!heatmap?.length" class="text-sm text-mute">
        Aucune lecture sur cette période.
      </p>
      <ActivityHeatmap v-else :cells="heatmap" />
    </BaseCard>

    <div class="grid gap-6 lg:grid-cols-2">
      <BaseCard>
        <h2 class="mb-4 font-display text-xl font-bold">
          Activité par jour de la semaine
        </h2>
        <BaseSpinner v-if="weekdayPending" />
        <WeekdayActivityChart
          v-else-if="weekdayActivity"
          :activity="weekdayActivity"
        />
      </BaseCard>
      <BaseCard>
        <h2 class="mb-4 font-display text-xl font-bold">Activité par heure</h2>
        <BaseSpinner v-if="hourlyPending" />
        <HourlyActivityChart
          v-else-if="hourlyActivity"
          :activity="hourlyActivity"
        />
      </BaseCard>
      <BaseCard>
        <h2 class="mb-4 font-display text-xl font-bold">Plateformes</h2>
        <BaseSpinner v-if="platformsPending" />
        <p v-else-if="!platforms?.length" class="text-sm text-mute">
          Aucune lecture sur cette période.
        </p>
        <PlatformsChart v-else :platforms="platforms" />
      </BaseCard>
    </div>

    <BaseCard>
      <h2 class="mb-4 font-display text-xl font-bold">Historique des sessions</h2>
      <SessionHistoryTable :period="period" :page-size="20" />
    </BaseCard>
  </div>
</template>
