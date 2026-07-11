<script setup lang="ts">
import { computed } from 'vue'
import type { CleanupCampaignDetailsDto } from '../../api/model'
import BaseCard from '../ui/BaseCard.vue'
import UiIcon from '../ui/UiIcon.vue'
import { daysUntil } from '../../lib/cleanup'
import { formatBytes, formatDate } from '../../lib/format'

const props = defineProps<{
  campaign: CleanupCampaignDetailsDto
  diskFreeBytes?: number | null
}>()

// Ce que la campagne compte libérer : les candidats encore en sursis + déjà supprimés
const plannedBytes = computed(() =>
  props.campaign.candidates
    .filter((c) => c.status === 'PROPOSED' || c.status === 'DELETED')
    .reduce((sum, c) => sum + c.sizeBytes, 0),
)

const progressPercent = computed(() => {
  if (props.campaign.targetBytesToFree <= 0) return 0
  return Math.min(
    100,
    (plannedBytes.value / props.campaign.targetBytesToFree) * 100,
  )
})

const daysLeft = computed(() => daysUntil(props.campaign.graceEndsAt))

const countdownLabel = computed(() => {
  if (daysLeft.value > 1)
    return `${daysLeft.value} jours pour sauver tes favoris`
  if (daysLeft.value === 1) return 'Dernier jour pour sauver tes favoris'
  return 'La suppression est imminente'
})
</script>

<template>
  <BaseCard>
    <div class="flex flex-wrap items-start justify-between gap-4">
      <div>
        <h2 class="font-display text-xl font-bold">Campagne en cours</h2>
        <p class="mt-1 text-sm text-ink-soft">
          Lancée le {{ formatDate(campaign.createdAt) }} — le disque
          {{ campaign.diskPath }} arrive à saturation, on fait de la place.
        </p>
      </div>
      <div
        class="flex items-center gap-2 rounded-full bg-amber-soft px-4 py-2 text-sm font-bold text-amber-deep"
      >
        <UiIcon name="clock" class="size-4" />
        {{ countdownLabel }}
      </div>
    </div>

    <div class="mt-5">
      <div class="mb-1.5 flex items-baseline justify-between text-sm">
        <span class="font-bold text-ink-soft">
          {{ formatBytes(plannedBytes) }} en passe d'être libérés
        </span>
        <span class="text-mute">
          objectif : {{ formatBytes(campaign.targetBytesToFree) }}
        </span>
      </div>
      <div class="h-3.5 overflow-hidden rounded-full bg-line/60">
        <div
          class="h-full rounded-full bg-amber transition-[width] duration-500"
          :style="{ width: `${progressPercent}%` }"
        />
      </div>
    </div>

    <div class="mt-4 flex flex-wrap gap-x-6 gap-y-1 text-xs text-mute">
      <span v-if="diskFreeBytes != null">
        Espace libre actuel : {{ formatBytes(diskFreeBytes) }}
      </span>
      <span>
        Fin de la période de grâce : {{ formatDate(campaign.graceEndsAt) }}
      </span>
      <span v-if="campaign.freedBytes > 0">
        Déjà libéré : {{ formatBytes(campaign.freedBytes) }}
      </span>
    </div>
  </BaseCard>
</template>
