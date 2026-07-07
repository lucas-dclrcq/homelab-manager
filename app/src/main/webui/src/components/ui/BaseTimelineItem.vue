<script setup lang="ts">
import UiIcon from './UiIcon.vue'

defineProps<{
  icon: string
  tone: 'amber' | 'dusk' | 'sage' | 'sky' | 'berry' | 'neutral'
  timestamp: string
}>()
</script>

<template>
  <li class="relative pb-6 last:pb-0">
    <span
      class="absolute -start-[39px] flex size-7 items-center justify-center rounded-full ring-4 ring-cream"
      :class="{
        'bg-amber-soft text-amber-deep': tone === 'amber',
        'bg-dusk-soft text-dusk': tone === 'dusk',
        'bg-sage-soft text-sage': tone === 'sage',
        'bg-sky-soft text-sky': tone === 'sky',
        'bg-berry-soft text-berry': tone === 'berry',
        'bg-line/60 text-ink-soft': tone === 'neutral',
      }"
      aria-hidden="true"
    >
      <UiIcon :name="icon" class="size-3.5" />
    </span>
    <div class="flex flex-wrap items-baseline gap-x-3 gap-y-1">
      <slot />
      <time class="font-mono text-[11px] text-mute">{{ timestamp }}</time>
    </div>
    <div class="mt-0.5 text-sm text-ink-soft">
      <slot name="details" />
    </div>
  </li>
</template>
