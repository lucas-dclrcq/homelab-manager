<script setup lang="ts">
import { computed } from 'vue'
import type { CorrectorWorkflowDto } from '../../api/model'
import BaseBadge from '../ui/BaseBadge.vue'
import UiIcon from '../ui/UiIcon.vue'
import { formatBytes, formatDateTime } from '../../lib/format'

const props = defineProps<{ workflow: CorrectorWorkflowDto }>()

const isCompleted = computed(() => props.workflow.status === 'COMPLETED')
</script>

<template>
  <div class="flex flex-col gap-5">
    <div
      class="flex flex-col items-center gap-2 rounded-card border-[1.5px] p-6 text-center"
      :class="
        isCompleted ? 'border-sage/40 bg-sage-soft' : 'border-line bg-cream'
      "
    >
      <UiIcon
        :name="isCompleted ? 'check' : 'clock'"
        class="size-8"
        :class="isCompleted ? 'text-sage' : 'text-amber-deep'"
      />
      <p class="font-display text-lg font-bold">
        {{ isCompleted ? "C'est réglé !" : 'La chouette surveille l’import…' }}
      </p>
      <p class="text-sm text-ink-soft">
        {{
          isCompleted
            ? 'La version française a été importée, bon visionnage !'
            : 'Le téléchargement est lancé. Dès que Radarr importe le fichier, ce workflow passera tout seul en « Terminé ».'
        }}
      </p>
      <p v-if="workflow.completedAt" class="font-mono text-xs text-mute">
        Terminé le {{ formatDateTime(workflow.completedAt) }}
      </p>
    </div>

    <div
      v-if="workflow.grabbedRelease"
      class="rounded-card border-[1.5px] border-line bg-white p-4"
    >
      <p class="mb-2 text-xs font-bold text-ink-soft uppercase tracking-wide">
        Release choisie
      </p>
      <p class="font-mono text-[13px] break-all">
        {{ workflow.grabbedRelease.title }}
      </p>
      <p class="mt-2 flex flex-wrap items-center gap-2">
        <BaseBadge v-if="workflow.grabbedRelease.quality" color="sky">
          {{ workflow.grabbedRelease.quality }}
        </BaseBadge>
        <span v-if="workflow.grabbedRelease.size" class="text-xs text-mute">
          {{ formatBytes(workflow.grabbedRelease.size) }}
        </span>
        <span v-if="workflow.grabbedRelease.indexer" class="text-xs text-mute">
          {{ workflow.grabbedRelease.indexer }}
        </span>
      </p>
    </div>
  </div>
</template>
