<script setup lang="ts">
import type { CorrectorWorkflowDto } from '../../api/model'
import BaseBadge from '../ui/BaseBadge.vue'
import BaseButton from '../ui/BaseButton.vue'
import UiIcon from '../ui/UiIcon.vue'
import { formatDateTime } from '../../lib/format'
import {
  isActive,
  problemLabels,
  statusPresentation,
} from '../../lib/corrector'

const props = defineProps<{ workflow: CorrectorWorkflowDto }>()

const status = statusPresentation[props.workflow.status] ?? {
  label: props.workflow.status,
  color: 'neutral' as const,
}
</script>

<template>
  <div
    class="flex items-center gap-4 rounded-tile border-[1.5px] border-line bg-paper p-4 shadow-soft"
  >
    <img
      v-if="workflow.movie?.posterUrl"
      :src="workflow.movie.posterUrl"
      alt=""
      class="h-20 w-14 shrink-0 rounded-lg object-cover"
    />
    <span
      v-else
      class="flex h-20 w-14 shrink-0 items-center justify-center rounded-lg bg-cream text-mute"
    >
      <UiIcon name="film" class="size-6" />
    </span>

    <div class="min-w-0 flex-1">
      <p class="truncate font-display text-[17px] font-bold">
        {{ workflow.movie?.title ?? 'Film à choisir' }}
        <span v-if="workflow.movie?.year" class="font-normal text-mute">
          ({{ workflow.movie.year }})
        </span>
      </p>
      <p class="mt-0.5 truncate text-sm text-ink-soft">
        {{
          workflow.problemType
            ? problemLabels[workflow.problemType]
            : 'Problème à préciser'
        }}
      </p>
      <p class="mt-1 font-mono text-xs text-mute">
        {{ formatDateTime(workflow.updatedAt) }}
      </p>
    </div>

    <div class="flex shrink-0 flex-col items-end gap-2">
      <BaseBadge :color="status.color">{{ status.label }}</BaseBadge>
      <BaseButton
        variant="ghost"
        @click="$router.push(`/corrector/${workflow.id}`)"
      >
        {{ isActive(workflow) ? 'Reprendre' : 'Voir' }}
        <UiIcon name="chevron-right" class="size-4" />
      </BaseButton>
    </div>
  </div>
</template>
