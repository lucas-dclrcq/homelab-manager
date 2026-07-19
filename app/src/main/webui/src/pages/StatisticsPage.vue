<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import StatsTabs from '../components/statistics/StatsTabs.vue'
import OverviewTab from '../components/statistics/tabs/OverviewTab.vue'
import UsersTab from '../components/statistics/tabs/UsersTab.vue'
import MediaTab from '../components/statistics/tabs/MediaTab.vue'
import ActivityTab from '../components/statistics/tabs/ActivityTab.vue'
import QualityTab from '../components/statistics/tabs/QualityTab.vue'
import LibraryTab from '../components/statistics/tabs/LibraryTab.vue'
import { periodOptions, type StatsPeriodValue } from '../lib/statistics'

const tabs = [
  { key: 'overview', label: "Vue d'ensemble", icon: 'chart-line' },
  { key: 'users', label: 'Spectateurs', icon: 'users' },
  { key: 'media', label: 'Médias', icon: 'film' },
  { key: 'activity', label: 'Activité', icon: 'activity' },
  { key: 'quality', label: 'Qualité', icon: 'zap' },
  { key: 'library', label: 'Bibliothèque', icon: 'package' },
] as const

type TabKey = (typeof tabs)[number]['key']

const route = useRoute()
const router = useRouter()

const validTabs = tabs.map((tab) => tab.key) as readonly string[]
const validPeriods = periodOptions.map((option) => option.value) as readonly string[]

const activeTab = computed<TabKey>(() => {
  const tab = route.query.tab
  return (typeof tab === 'string' && validTabs.includes(tab)
    ? tab
    : 'overview') as TabKey
})

const period = computed<StatsPeriodValue>(() => {
  const value = route.query.period
  return (typeof value === 'string' && validPeriods.includes(value)
    ? value
    : 'THIS_WEEK') as StatsPeriodValue
})

function setTab(tab: string) {
  router.replace({ query: { ...route.query, tab } })
}

function setPeriod(event: Event) {
  const value = (event.target as HTMLSelectElement).value
  router.replace({ query: { ...route.query, period: value } })
}
</script>

<template>
  <div class="flex max-w-5xl flex-col gap-6">
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
        <span class="mb-1.5 block text-xs font-bold text-ink-soft">Période</span>
        <select
          :value="period"
          class="rounded-xl border-[1.5px] border-line bg-white px-3.5 py-2.5 text-[15px] font-bold text-ink focus:border-amber focus:ring-[3px] focus:ring-amber/45 focus:outline-none"
          @change="setPeriod"
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

    <StatsTabs
      :tabs="tabs"
      :model-value="activeTab"
      @update:model-value="setTab"
    />

    <OverviewTab v-if="activeTab === 'overview'" :period="period" />
    <UsersTab v-else-if="activeTab === 'users'" :period="period" />
    <MediaTab v-else-if="activeTab === 'media'" :period="period" />
    <ActivityTab v-else-if="activeTab === 'activity'" :period="period" />
    <QualityTab v-else-if="activeTab === 'quality'" :period="period" />
    <LibraryTab v-else-if="activeTab === 'library'" :period="period" />
  </div>
</template>
