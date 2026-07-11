<script setup lang="ts">
import type { ScoreBreakdown } from '../../api/model'

defineProps<{ breakdown: ScoreBreakdown }>()

function percent(points?: number, weight?: number): number {
  if (!weight) return 0
  return Math.min(100, Math.max(0, ((points ?? 0) / weight) * 100))
}

function formatPoints(points?: number): string {
  return (points ?? 0).toLocaleString('fr-FR', { maximumFractionDigits: 1 })
}
</script>

<template>
  <div class="flex flex-col gap-2.5">
    <div
      v-for="component in breakdown.components ?? []"
      :key="component.key"
      class="flex flex-col gap-1"
    >
      <div class="flex items-baseline justify-between gap-3 text-xs">
        <span class="font-bold text-ink-soft">{{ component.label }}</span>
        <span class="whitespace-nowrap text-mute">
          {{ component.rawValue }} · {{ formatPoints(component.points) }} /
          {{ component.weight }} pts
        </span>
      </div>
      <div class="h-2 overflow-hidden rounded-full bg-line/60">
        <div
          class="h-full rounded-full bg-amber transition-[width]"
          :style="{ width: `${percent(component.points, component.weight)}%` }"
        />
      </div>
    </div>

    <p
      v-if="breakdown.inputs?.correlation === 'NONE'"
      class="mt-1 rounded-card bg-sky-soft px-3 py-2 text-xs text-sky"
    >
      Pas d'historique Jellyfin fiable pour ce média : le score s'appuie surtout
      sur son âge et sa taille.
    </p>
  </div>
</template>
