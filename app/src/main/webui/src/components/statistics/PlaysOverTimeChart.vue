<script setup lang="ts">
import { computed } from 'vue'
import {
  CategoryScale,
  Chart,
  Filler,
  LinearScale,
  LineController,
  LineElement,
  PointElement,
  Tooltip,
  type TooltipItem,
} from 'chart.js'
import { Line } from 'vue-chartjs'
import type { PlaysOverTimeDto } from '../../api/model'
import { hoohootChartColors, hoohootScales } from '../../lib/charts'
import { formatWatchTime } from '../../lib/statistics'

Chart.register(
  LineController,
  LineElement,
  PointElement,
  CategoryScale,
  LinearScale,
  Filler,
  Tooltip,
)

const props = defineProps<{ overTime: PlaysOverTimeDto }>()

function bucketLabel(bucketStart: string): string {
  const date = new Date(bucketStart)
  switch (props.overTime.granularity) {
    case 'HOUR':
      return `${date.getHours()}h`
    case 'DAY':
      return date.toLocaleDateString('fr-FR', {
        day: 'numeric',
        month: 'short',
      })
    default:
      return date.toLocaleDateString('fr-FR', {
        month: 'short',
        year: '2-digit',
      })
  }
}

const chartData = computed(() => ({
  labels: (props.overTime.points ?? []).map((point) =>
    bucketLabel(point.bucketStart),
  ),
  datasets: [
    {
      label: 'Lectures',
      data: (props.overTime.points ?? []).map((point) => point.plays),
      borderColor: hoohootChartColors.amber,
      backgroundColor: 'rgba(232, 151, 58, 0.15)',
      fill: true,
      tension: 0.3,
      pointRadius: 2,
      pointBackgroundColor: hoohootChartColors.amber,
    },
  ],
}))

const chartOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    tooltip: {
      callbacks: {
        label: (context: TooltipItem<'line'>) => {
          const point = props.overTime.points?.[context.dataIndex]
          return `${context.parsed.y} lectures · ${formatWatchTime(point?.watchTimeSeconds ?? 0)}`
        },
      },
    },
  },
  scales: hoohootScales,
}))
</script>

<template>
  <div class="h-64">
    <Line :data="chartData" :options="chartOptions" />
  </div>
</template>
