<script setup lang="ts">
import { computed } from 'vue'
import {
  ArcElement,
  BarController,
  BarElement,
  CategoryScale,
  Chart,
  DoughnutController,
  Legend,
  LinearScale,
  Tooltip,
  type TooltipItem,
} from 'chart.js'
import { Bar, Doughnut } from 'vue-chartjs'
import type { QualitySliceDto } from '../../api/model'
import { hoohootChartColors, hoohootLegend, hoohootScales } from '../../lib/charts'
import { formatWatchTime } from '../../lib/statistics'

Chart.register(
  ArcElement,
  DoughnutController,
  BarController,
  BarElement,
  CategoryScale,
  LinearScale,
  Tooltip,
  Legend,
)

const props = defineProps<{
  slices: QualitySliceDto[]
  variant: 'doughnut' | 'bar'
  mapLabel?: (label: string) => string
}>()

const palette = [
  hoohootChartColors.amber,
  hoohootChartColors.dusk,
  hoohootChartColors.sage,
  hoohootChartColors.sky,
  hoohootChartColors.berry,
  hoohootChartColors.tick,
]

const labels = computed(() =>
  props.slices.map((slice) =>
    props.mapLabel ? props.mapLabel(slice.label) : slice.label,
  ),
)

const chartData = computed(() => ({
  labels: labels.value,
  datasets: [
    {
      data: props.slices.map((slice) => slice.plays),
      backgroundColor: props.slices.map(
        (_, index) => palette[index % palette.length],
      ),
      borderColor: '#fdf9f2',
      borderWidth: props.variant === 'doughnut' ? 3 : 0,
      borderRadius: props.variant === 'bar' ? 6 : 0,
    },
  ],
}))

const tooltipLabel = (
  context: TooltipItem<'doughnut'> | TooltipItem<'bar'>,
) => {
  const slice = props.slices[context.dataIndex]
  return `${context.label} : ${slice?.plays ?? 0} lectures · ${formatWatchTime(slice?.watchTimeSeconds ?? 0)}`
}

const doughnutOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: hoohootLegend,
    tooltip: { callbacks: { label: tooltipLabel } },
  },
}))

const barOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
    tooltip: { callbacks: { label: tooltipLabel } },
  },
  scales: hoohootScales,
}))
</script>

<template>
  <div class="h-64">
    <Doughnut
      v-if="variant === 'doughnut'"
      :data="chartData"
      :options="doughnutOptions"
    />
    <Bar v-else :data="chartData" :options="barOptions" />
  </div>
</template>
