<script setup lang="ts">
import { computed, ref } from 'vue'
import type { CleanupCandidateDto } from '../../api/model'
import BaseBadge from '../ui/BaseBadge.vue'
import BaseButton from '../ui/BaseButton.vue'
import UiIcon from '../ui/UiIcon.vue'
import ScoreBreakdown from './ScoreBreakdown.vue'
import { candidateStatusPresentation } from '../../lib/cleanup'
import { formatBytes } from '../../lib/format'

const props = defineProps<{ candidate: CleanupCandidateDto }>()

defineEmits<{ veto: [candidate: CleanupCandidateDto] }>()

const detailsOpen = ref(false)

const status = computed(() =>
  candidateStatusPresentation(props.candidate.status),
)
const scoreLabel = computed(() => Math.round(props.candidate.score))
</script>

<template>
  <article
    class="flex flex-col gap-3 rounded-card border-[1.5px] border-line bg-paper p-4 shadow-soft"
  >
    <div class="flex items-start gap-3">
      <img
        v-if="candidate.posterUrl"
        :src="candidate.posterUrl"
        alt=""
        class="h-24 w-16 shrink-0 rounded-md object-cover"
      />
      <span
        v-else
        class="flex h-24 w-16 shrink-0 items-center justify-center rounded-md bg-cream text-mute"
      >
        <UiIcon
          :name="candidate.mediaKind === 'MOVIE' ? 'film' : 'tv'"
          class="size-6"
        />
      </span>

      <div class="min-w-0 flex-1">
        <div class="flex flex-wrap items-center gap-2">
          <h3 class="min-w-0 truncate font-display text-[17px] font-bold">
            {{ candidate.displayTitle }}
          </h3>
          <BaseBadge :color="status.color">{{ status.label }}</BaseBadge>
        </div>
        <p class="mt-0.5 text-sm text-ink-soft">
          <span v-if="candidate.year">{{ candidate.year }} · </span>
          {{ formatBytes(candidate.sizeBytes) }}
        </p>
        <p v-if="candidate.requester" class="mt-0.5 text-xs text-mute">
          Demandé par {{ candidate.requester }}
        </p>
        <p v-if="candidate.protectedBy" class="mt-0.5 text-xs text-sage">
          <UiIcon name="shield" class="inline size-3.5 align-[-2px]" />
          Gardé par {{ candidate.protectedBy }}
        </p>
        <p v-if="candidate.failureReason" class="mt-0.5 text-xs text-berry">
          {{ candidate.failureReason }}
        </p>
      </div>

      <div
        class="flex size-11 shrink-0 flex-col items-center justify-center rounded-xl bg-amber-soft"
        :title="`Score de nettoyage : ${scoreLabel}/100`"
      >
        <span
          class="font-display text-[17px] font-bold leading-none text-amber-deep"
        >
          {{ scoreLabel }}
        </span>
        <span class="text-[10px] font-bold text-amber-deep/70">/100</span>
      </div>
    </div>

    <div class="flex items-center justify-between gap-2">
      <button
        type="button"
        class="inline-flex cursor-pointer items-center gap-1 text-xs font-bold text-ink-soft transition-colors hover:text-ink"
        @click="detailsOpen = !detailsOpen"
      >
        <UiIcon
          name="chevron-right"
          class="size-3.5 transition-transform"
          :class="{ 'rotate-90': detailsOpen }"
        />
        Pourquoi ce score ?
      </button>

      <BaseButton
        v-if="candidate.status === 'PROPOSED'"
        variant="secondary"
        @click="$emit('veto', candidate)"
      >
        <UiIcon name="shield" class="size-4" />
        Je garde
      </BaseButton>
    </div>

    <div
      v-if="detailsOpen"
      class="rounded-card border-[1.5px] border-line bg-cream/60 p-3"
    >
      <ScoreBreakdown :breakdown="candidate.scoreBreakdown" />
    </div>
  </article>
</template>
