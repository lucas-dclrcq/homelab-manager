<script setup lang="ts">
import { computed } from 'vue'
import {
  BarController,
  BarElement,
  CategoryScale,
  Chart,
  Legend,
  LinearScale,
  Tooltip,
  type TooltipItem,
} from 'chart.js'
import { Bar } from 'vue-chartjs'
import type { MonthlyTotalsDto } from '../../api/model'
import { formatEuros } from '../../lib/format'
import { monthLabels } from '../../lib/finances'

Chart.register(
  BarController,
  BarElement,
  CategoryScale,
  LinearScale,
  Tooltip,
  Legend,
)

const props = defineProps<{ monthly: MonthlyTotalsDto[] }>()

// Couleurs de la charte Hoohoot (sage / berry, voir style.css)
const chartData = computed(() => ({
  labels: monthLabels,
  datasets: [
    {
      label: 'Cotisations',
      data: props.monthly.map((m) => m.contributionsCents / 100),
      backgroundColor: '#5e9e7a',
      borderRadius: 6,
    },
    {
      label: 'Dépenses',
      data: props.monthly.map((m) => m.expensesCents / 100),
      backgroundColor: '#c4574f',
      borderRadius: 6,
    },
  ],
}))

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: {
      position: 'top' as const,
      labels: {
        font: { family: 'Nunito, sans-serif', weight: 'bold' as const },
        color: '#5c5566',
        boxWidth: 14,
        boxHeight: 14,
        borderRadius: 4,
        useBorderRadius: true,
      },
    },
    tooltip: {
      callbacks: {
        label: (context: TooltipItem<'bar'>) =>
          `${context.dataset.label} : ${formatEuros(Math.round((context.parsed.y ?? 0) * 100))}`,
      },
    },
  },
  scales: {
    x: {
      grid: { display: false },
      ticks: { color: '#a79e92', font: { family: 'Nunito, sans-serif' } },
    },
    y: {
      grid: { color: '#ece4d8' },
      ticks: {
        color: '#a79e92',
        font: { family: 'Nunito, sans-serif' },
        callback: (value: string | number) => `${value} €`,
      },
    },
  },
}
</script>

<template>
  <div class="h-72">
    <Bar :data="chartData" :options="chartOptions" />
  </div>
</template>
