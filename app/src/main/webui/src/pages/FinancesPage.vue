<script setup lang="ts">
import { computed, ref } from 'vue'
import {
  useGetApiFinancesEnergy,
  useGetApiFinancesMonthly,
  useGetApiFinancesSummary,
} from '../api/service/homelab'
import StatCard from '../components/ui/StatCard.vue'
import BaseCard from '../components/ui/BaseCard.vue'
import BaseSpinner from '../components/ui/BaseSpinner.vue'
import MonthlyFinanceChart from '../components/finances/MonthlyFinanceChart.vue'
import FinanceEntriesTable from '../components/finances/FinanceEntriesTable.vue'
import { formatEuros } from '../lib/format'

const FIRST_YEAR = 2024

const currentYear = new Date().getFullYear()
const year = ref(currentYear)
const years = Array.from(
  { length: currentYear - FIRST_YEAR + 1 },
  (_, index) => currentYear - index,
)

const summaryParams = computed(() => ({ year: year.value }))

const { data: summary, isPending: summaryPending } =
  useGetApiFinancesSummary(summaryParams)
const { data: monthly, isPending: monthlyPending } =
  useGetApiFinancesMonthly(summaryParams)
const { data: energy } = useGetApiFinancesEnergy()

const balanceTone = computed(() =>
  (summary.value?.balanceCents ?? 0) >= 0 ? 'amber' : 'berry',
)

const energyValue = computed(() => {
  const watts = energy.value?.currentPowerWatts
  return watts == null ? '—' : `${Math.round(watts)} W`
})

const energyDetail = computed(() => {
  const status = energy.value
  if (!status) return ''
  if (status.estimatedMonthlyCostCents != null) {
    return `≈ ${formatEuros(status.estimatedMonthlyCostCents)} / mois`
  }
  if (status.estimatedMonthlyKwh != null) {
    return `≈ ${Math.round(status.estimatedMonthlyKwh)} kWh / mois`
  }
  return ''
})
</script>

<template>
  <div class="flex max-w-5xl flex-col gap-8">
    <header class="flex flex-wrap items-end justify-between gap-4">
      <div>
        <h1 class="roost font-display text-[34px] font-extrabold leading-tight">
          Finances
        </h1>
        <p class="mt-2 text-ink-soft">
          Les sous de Hoohoot : qui cotise, ce qu'on dépense, et ce que ça
          consomme.
        </p>
      </div>
      <label class="block">
        <span class="mb-1.5 block text-xs font-bold text-ink-soft">Année</span>
        <select
          v-model="year"
          class="rounded-xl border-[1.5px] border-line bg-white px-3.5 py-2.5 text-[15px] font-bold text-ink focus:border-amber focus:ring-[3px] focus:ring-amber/45 focus:outline-none"
        >
          <option v-for="option in years" :key="option" :value="option">
            {{ option }}
          </option>
        </select>
      </label>
    </header>

    <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
      <StatCard
        label="Cotisations sur l'année"
        :value="summary ? formatEuros(summary.totalContributionsCents) : '…'"
        icon="coins"
        tone="sage"
      />
      <StatCard
        label="Dépenses sur l'année"
        :value="summary ? formatEuros(summary.totalExpensesCents) : '…'"
        icon="shopping-cart"
        tone="berry"
      />
      <StatCard
        label="Solde"
        :value="summary ? formatEuros(summary.balanceCents) : '…'"
        icon="scale"
        :tone="balanceTone"
      />
      <StatCard
        :label="
          energyDetail ? `Conso actuelle · ${energyDetail}` : 'Conso actuelle'
        "
        :value="energyValue"
        icon="zap"
        tone="sky"
      />
    </div>

    <BaseCard>
      <h2 class="mb-4 font-display text-xl font-bold">
        Évolution sur {{ year }}
      </h2>
      <BaseSpinner v-if="summaryPending || monthlyPending" />
      <MonthlyFinanceChart v-else-if="monthly" :monthly="monthly" />
    </BaseCard>

    <FinanceEntriesTable :year="year" />
  </div>
</template>
