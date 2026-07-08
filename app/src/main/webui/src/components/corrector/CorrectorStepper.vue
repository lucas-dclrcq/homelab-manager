<script setup lang="ts">
import UiIcon from '../ui/UiIcon.vue'
import { wizardSteps } from '../../lib/corrector'

defineProps<{ currentIndex: number }>()
</script>

<template>
  <ol class="flex flex-wrap items-center gap-2">
    <li
      v-for="(step, index) in wizardSteps"
      :key="step.key"
      class="flex items-center gap-2"
    >
      <span
        class="flex size-7 shrink-0 items-center justify-center rounded-full font-display text-[13px] font-bold"
        :class="{
          'bg-sage-soft text-sage': index < currentIndex,
          'bg-amber text-ink': index === currentIndex,
          'bg-line/60 text-mute': index > currentIndex,
        }"
      >
        <UiIcon v-if="index < currentIndex" name="check" class="size-4" />
        <template v-else>{{ index + 1 }}</template>
      </span>
      <span
        class="text-sm font-semibold"
        :class="index === currentIndex ? 'text-ink' : 'text-mute'"
      >
        {{ step.label }}
      </span>
      <UiIcon
        v-if="index < wizardSteps.length - 1"
        name="chevron-right"
        class="size-4 text-line"
      />
    </li>
  </ol>
</template>
