<script setup lang="ts">
import { computed } from 'vue'
import type { HeatmapCellDto } from '../../api/model'
import { weekdayLabels } from '../../lib/statistics'

const props = defineProps<{ cells: HeatmapCellDto[] }>()

const hours = Array.from({ length: 24 }, (_, h) => h)
const days = [1, 2, 3, 4, 5, 6, 7]

// Matrice [isoDayOfWeek][hour] => lectures
const matrix = computed(() => {
  const grid = new Map<string, number>()
  for (const cell of props.cells) {
    grid.set(`${cell.isoDayOfWeek}-${cell.hour}`, cell.plays)
  }
  return grid
})

const maxPlays = computed(() =>
  props.cells.reduce((max, cell) => Math.max(max, cell.plays), 0),
)

function plays(day: number, hour: number): number {
  return matrix.value.get(`${day}-${hour}`) ?? 0
}

// Intensité amber : transparent (0) -> plein (max)
function cellStyle(day: number, hour: number) {
  const value = plays(day, hour)
  if (value === 0 || maxPlays.value === 0) return { backgroundColor: '#f2ece1' }
  const alpha = 0.15 + 0.85 * (value / maxPlays.value)
  return { backgroundColor: `rgba(232, 151, 58, ${alpha.toFixed(3)})` }
}
</script>

<template>
  <div class="overflow-x-auto">
    <div class="inline-grid min-w-full gap-1" style="grid-template-columns: auto repeat(24, minmax(14px, 1fr))">
      <!-- En-tête heures -->
      <div></div>
      <div
        v-for="hour in hours"
        :key="`h-${hour}`"
        class="text-center text-[10px] text-mute"
      >
        <span v-if="hour % 3 === 0">{{ hour }}</span>
      </div>

      <!-- Lignes par jour -->
      <template v-for="day in days" :key="`d-${day}`">
        <div class="pr-2 text-right text-xs font-bold text-ink-soft">
          {{ weekdayLabels[day - 1] }}
        </div>
        <div
          v-for="hour in hours"
          :key="`${day}-${hour}`"
          class="aspect-square rounded-[3px]"
          :style="cellStyle(day, hour)"
          :title="`${weekdayLabels[day - 1]} ${hour}h · ${plays(day, hour)} lecture(s)`"
        ></div>
      </template>
    </div>
  </div>
</template>
