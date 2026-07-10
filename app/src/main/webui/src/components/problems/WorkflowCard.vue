<script setup lang="ts">
import { computed } from 'vue'
import type { ProblemWorkflowDto } from '../../api/model'
import BaseBadge from '../ui/BaseBadge.vue'
import BaseButton from '../ui/BaseButton.vue'
import UiIcon from '../ui/UiIcon.vue'
import { formatDateTime } from '../../lib/format'
import { isActive, problemLabels, statusPresentation } from '../../lib/problems'

const props = withDefaults(
  defineProps<{ workflow: ProblemWorkflowDto; to?: string }>(),
  { to: undefined },
)

const status = statusPresentation[props.workflow.status] ?? {
  label: props.workflow.status,
  color: 'neutral' as const,
}

const target = computed(() => props.to ?? `/problems/${props.workflow.id}`)
const mediaIcon = computed(() =>
  props.workflow.mediaType === 'tv' ? 'tv' : 'film',
)
</script>

<template>
  <div
    class="flex items-center gap-4 rounded-tile border-[1.5px] border-line bg-paper p-4 shadow-soft"
  >
    <img
      v-if="workflow.media?.posterUrl"
      :src="workflow.media.posterUrl"
      alt=""
      class="h-20 w-14 shrink-0 rounded-lg object-cover"
    />
    <span
      v-else
      class="flex h-20 w-14 shrink-0 items-center justify-center rounded-lg bg-cream text-mute"
    >
      <UiIcon :name="mediaIcon" class="size-6" />
    </span>

    <div class="min-w-0 flex-1">
      <p class="truncate font-display text-[17px] font-bold">
        {{
          workflow.media?.title ??
          (workflow.mediaType === 'tv' ? 'Série à choisir' : 'Film à choisir')
        }}
        <span v-if="workflow.media?.year" class="font-normal text-mute">
          ({{ workflow.media.year }})
        </span>
      </p>
      <p class="mt-0.5 truncate text-sm text-ink-soft">
        {{
          workflow.problemType
            ? (problemLabels[workflow.problemType] ?? workflow.problemType)
            : 'Problème à préciser'
        }}
      </p>
      <p
        v-if="workflow.problemType === 'other' && workflow.description"
        class="mt-0.5 truncate text-xs text-mute"
      >
        {{ workflow.description }}
      </p>
      <p class="mt-1 font-mono text-xs text-mute">
        {{ formatDateTime(workflow.updatedAt) }}
      </p>
    </div>

    <div class="flex shrink-0 flex-col items-end gap-2">
      <BaseBadge :color="status.color">{{ status.label }}</BaseBadge>
      <BaseButton variant="ghost" @click="$router.push(target)">
        {{ isActive(workflow) ? 'Reprendre' : 'Voir' }}
        <UiIcon name="chevron-right" class="size-4" />
      </BaseButton>
    </div>
  </div>
</template>
