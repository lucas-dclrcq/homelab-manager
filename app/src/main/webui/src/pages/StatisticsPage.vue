<script setup lang="ts">
import { computed, ref } from 'vue'
import {
  useGetApiStatisticsActivityByHour,
  useGetApiStatisticsActivityByWeekday,
  useGetApiStatisticsPlatforms,
  useGetApiStatisticsPlaysOverTime,
  useGetApiStatisticsSummary,
  useGetApiStatisticsTopUsers,
} from '../api/service/homelab'
import StatCard from '../components/ui/StatCard.vue'
import BaseCard from '../components/ui/BaseCard.vue'
import BaseSpinner from '../components/ui/BaseSpinner.vue'
import NowPlayingCard from '../components/statistics/NowPlayingCard.vue'
import TopUsersPodium from '../components/statistics/TopUsersPodium.vue'
import TopMediaTable from '../components/statistics/TopMediaTable.vue'
import WeekdayActivityChart from '../components/statistics/WeekdayActivityChart.vue'
import HourlyActivityChart from '../components/statistics/HourlyActivityChart.vue'
import PlatformsChart from '../components/statistics/PlatformsChart.vue'
import PlaysOverTimeChart from '../components/statistics/PlaysOverTimeChart.vue'
import {
  formatWatchTime,
  periodOptions,
  type StatsPeriodValue,
  type TopUsersMetric,
} from '../lib/statistics'

const period = ref<StatsPeriodValue>('THIS_WEEK')
const topUsersMetric = ref<TopUsersMetric>('time')
const params = computed(() => ({ period: period.value }))
// Le changement de période resert le cache TanStack au lieu de re-marteler l'API
const queryOptions = { query: { staleTime: 60_000 } }

const { data: summary } = useGetApiStatisticsSummary(params, queryOptions)
const { data: topUsers, isPending: topUsersPending } =
  useGetApiStatisticsTopUsers(params, queryOptions)
const { data: weekdayActivity, isPending: weekdayPending } =
  useGetApiStatisticsActivityByWeekday(params, queryOptions)
const { data: hourlyActivity, isPending: hourlyPending } =
  useGetApiStatisticsActivityByHour(params, queryOptions)
const { data: platforms, isPending: platformsPending } =
  useGetApiStatisticsPlatforms(params, queryOptions)
const { data: playsOverTime, isPending: overTimePending } =
  useGetApiStatisticsPlaysOverTime(params, queryOptions)

const peakHourValue = computed(() => {
  const peakHour = summary.value?.peakHour
  return peakHour == null ? '—' : `${peakHour}h`
})
</script>

<template>
  <div class="flex max-w-5xl flex-col gap-8">
    <header class="flex flex-wrap items-end justify-between gap-4">
      <div>
        <h1 class="roost font-display text-[34px] font-extrabold leading-tight">
          Statistiques
        </h1>
        <p class="mt-2 text-ink-soft">
          Ce que la chouette a vu passer sur Jellyfin : qui regarde quoi, et
          quand.
        </p>
      </div>
      <label class="block">
        <span class="mb-1.5 block text-xs font-bold text-ink-soft"
          >Période</span
        >
        <select
          v-model="period"
          class="rounded-xl border-[1.5px] border-line bg-white px-3.5 py-2.5 text-[15px] font-bold text-ink focus:border-amber focus:ring-[3px] focus:ring-amber/45 focus:outline-none"
        >
          <option
            v-for="option in periodOptions"
            :key="option.value"
            :value="option.value"
          >
            {{ option.label }}
          </option>
        </select>
      </label>
    </header>

    <NowPlayingCard />

    <div class="grid gap-4 sm:grid-cols-3">
      <StatCard
        label="Temps de visionnage"
        :value="summary ? formatWatchTime(summary.totalWatchTimeSeconds) : '…'"
        icon="clock"
        tone="amber"
      />
      <StatCard
        label="Médias complétés"
        :value="summary ? String(summary.completedItems) : '…'"
        icon="circle-check"
        tone="sage"
      />
      <StatCard
        label="Heure de pointe"
        :value="summary ? peakHourValue : '…'"
        icon="activity"
        tone="sky"
      />
    </div>

    <BaseCard>
      <div class="mb-4 flex flex-wrap items-center justify-between gap-3">
        <h2 class="font-display text-xl font-bold">Top visionneurs</h2>
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
      <BaseSpinner v-if="topUsersPending" />
      <p v-else-if="!topUsers?.length" class="text-sm text-mute">
        Personne n'a rien regardé sur cette période. Suspect.
      </p>
      <TopUsersPodium v-else :users="topUsers" :metric="topUsersMetric" />
    </BaseCard>

    <TopMediaTable :period="period" />

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
      <BaseCard>
        <h2 class="mb-4 font-display text-xl font-bold">
          Lectures dans le temps
        </h2>
        <BaseSpinner v-if="overTimePending" />
        <PlaysOverTimeChart
          v-else-if="playsOverTime"
          :over-time="playsOverTime"
        />
      </BaseCard>
    </div>
  </div>
</template>
