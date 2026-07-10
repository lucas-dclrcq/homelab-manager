<script setup lang="ts">
import { computed } from 'vue'
import type { ProblemWorkflowDto } from '../../api/model'
import { useResolveMutation } from '../../lib/problemsApi'
import BaseButton from '../ui/BaseButton.vue'
import UiIcon from '../ui/UiIcon.vue'
import { formatDateTime } from '../../lib/format'

const props = withDefaults(
  defineProps<{ workflow: ProblemWorkflowDto; admin?: boolean }>(),
  { admin: false },
)

const isResolved = computed(() => props.workflow.status === 'RESOLVED')

const { mutate: resolve, isPending: isResolving } = useResolveMutation(
  props.admin,
)
</script>

<template>
  <div class="flex flex-col gap-5">
    <div
      class="flex flex-col items-center gap-2 rounded-card border-[1.5px] p-6 text-center"
      :class="
        isResolved ? 'border-sage/40 bg-sage-soft' : 'border-line bg-cream'
      "
    >
      <UiIcon
        :name="isResolved ? 'check' : 'circle-alert'"
        class="size-8"
        :class="isResolved ? 'text-sage' : 'text-berry'"
      />
      <p class="font-display text-lg font-bold">
        {{ isResolved ? "C'est réglé !" : 'Problème signalé' }}
      </p>
      <p class="text-sm text-ink-soft">
        {{
          isResolved
            ? 'Ce problème a été marqué comme résolu.'
            : "La chouette a transmis le signalement. Quelqu'un va s'en occuper — et si ça se règle entre-temps, clique sur « C'est réglé »."
        }}
      </p>
      <p v-if="workflow.completedAt" class="font-mono text-xs text-mute">
        Résolu le {{ formatDateTime(workflow.completedAt) }}
      </p>
    </div>

    <div
      v-if="workflow.description"
      class="rounded-card border-[1.5px] border-line bg-white p-4"
    >
      <p class="mb-2 text-xs font-bold text-ink-soft uppercase tracking-wide">
        Le souci décrit
      </p>
      <p class="text-sm whitespace-pre-line text-ink">
        {{ workflow.description }}
      </p>
    </div>

    <div v-if="!isResolved">
      <BaseButton :loading="isResolving" @click="resolve({ id: workflow.id })">
        <UiIcon name="check" class="size-4" />
        C'est réglé
      </BaseButton>
    </div>
  </div>
</template>
