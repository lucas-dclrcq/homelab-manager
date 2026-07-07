<script setup lang="ts">
withDefaults(
  defineProps<{
    color?: 'sage' | 'berry' | 'sky' | 'amber' | 'dusk' | 'neutral'
  }>(),
  { color: 'neutral' },
)
</script>

<template>
  <span
    class="inline-flex items-center gap-1.5 rounded-full px-3 py-1 text-xs font-bold"
    :class="{
      'bg-sage-soft text-sage': color === 'sage',
      'bg-berry-soft text-berry': color === 'berry',
      'bg-sky-soft text-sky': color === 'sky',
      'bg-amber-soft text-amber-deep': color === 'amber',
      'bg-dusk-soft text-dusk-deep': color === 'dusk',
      'bg-line/60 text-ink-soft': color === 'neutral',
    }"
  >
    <slot />
  </span>
</template>
