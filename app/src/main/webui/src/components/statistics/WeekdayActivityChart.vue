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
import type { WeekdayActivityDto } from '../../api/model'
import { hoohootChartColors, hoohootScales } from '../../lib/charts'
import { formatWatchTime, weekdayLabels } from '../../lib/statistics'

Chart.register(BarController, BarElement, CategoryScale, LinearScale, Tooltip)

const props = defineProps<{ activity: WeekdayActivityDto[] }>()

const chartData = computed(() => ({
  labels: weekdayLabels,
  datasets: [
    {
      label: 'Lectures',
      data: props.activity.map((day) => day.plays),
      backgroundColor: hoohootChartColors.amber,
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
          const day = props.activity[context.dataIndex]
          return `${context.parsed.y} lectures · ${formatWatchTime(day?.watchTimeSeconds ?? 0)}`
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
