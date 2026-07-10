<script setup lang="ts">
import { computed } from 'vue'
import {
  BarController,
  BarElement,
  CategoryScale,
  Chart,
  LinearScale,
  Tooltip,
  type TooltipItem,
} from 'chart.js'
import { Bar } from 'vue-chartjs'
import type { HourActivityDto } from '../../api/model'
import { hoohootChartColors, hoohootScales } from '../../lib/charts'
import { formatWatchTime } from '../../lib/statistics'

Chart.register(BarController, BarElement, CategoryScale, LinearScale, Tooltip)

const props = defineProps<{ activity: HourActivityDto[] }>()

const chartData = computed(() => ({
  labels: props.activity.map((hour) => `${hour.hour}h`),
  datasets: [
    {
      label: 'Lectures',
      data: props.activity.map((hour) => hour.plays),
      backgroundColor: hoohootChartColors.dusk,
      borderRadius: 6,
    },
  ],
}))

const chartOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    tooltip: {
      callbacks: {
        label: (context: TooltipItem<'bar'>) => {
          const hour = props.activity[context.dataIndex]
          return `${context.parsed.y} lectures · ${formatWatchTime(hour?.watchTimeSeconds ?? 0)}`
        },
      },
    },
  },
  scales: hoohootScales,
}))
</script>

<template>
  <div class="h-64">
    <Bar :data="chartData" :options="chartOptions" />
  </div>
</template>
