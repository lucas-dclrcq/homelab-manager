<script setup lang="ts">
import { computed } from 'vue'
import {
  ArcElement,
  Chart,
  DoughnutController,
  Legend,
  Tooltip,
  type TooltipItem,
} from 'chart.js'
import { Doughnut } from 'vue-chartjs'
import type { PlatformShareDto } from '../../api/model'
import { hoohootChartColors, hoohootLegend } from '../../lib/charts'
import { formatWatchTime, platformLabel } from '../../lib/statistics'

Chart.register(ArcElement, DoughnutController, Tooltip, Legend)

const props = defineProps<{ platforms: PlatformShareDto[] }>()

const palette = [
  hoohootChartColors.amber,
  hoohootChartColors.dusk,
  hoohootChartColors.sage,
  hoohootChartColors.sky,
  hoohootChartColors.berry,
  hoohootChartColors.tick,
]

const chartData = computed(() => ({
  labels: props.platforms.map((share) => platformLabel(share.platform)),
  datasets: [
    {
      data: props.platforms.map((share) => share.plays),
      backgroundColor: props.platforms.map(
        (_, index) => palette[index % palette.length],
      ),
      borderColor: '#fdf9f2',
      borderWidth: 3,
    },
  ],
}))

const chartOptions = computed(() => ({
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: hoohootLegend,
    tooltip: {
      callbacks: {
        label: (context: TooltipItem<'doughnut'>) => {
          const share = props.platforms[context.dataIndex]
          return `${context.label} : ${context.parsed} lectures · ${formatWatchTime(share?.watchTimeSeconds ?? 0)}`
        },
      },
    },
  },
}))
</script>

<template>
  <div class="h-64">
    <Doughnut :data="chartData" :options="chartOptions" />
  </div>
</template>
