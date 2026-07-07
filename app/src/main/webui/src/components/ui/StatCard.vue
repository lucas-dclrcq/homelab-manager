<script setup lang="ts">
import UiIcon from './UiIcon.vue'

defineProps<{
  label: string
  value: string
  icon: string
  tone: 'amber' | 'dusk' | 'sage' | 'sky' | 'berry'
}>()
</script>

<template>
  <div
    class="flex items-center gap-3.5 rounded-card border-[1.5px] border-line bg-paper px-4 py-4"
  >
    <div
      class="flex size-11 shrink-0 items-center justify-center rounded-xl"
      :class="{
        'bg-amber-soft text-amber-deep': tone === 'amber',
        'bg-dusk-soft text-dusk': tone === 'dusk',
        'bg-sage-soft text-sage': tone === 'sage',
        'bg-sky-soft text-sky': tone === 'sky',
        'bg-berry-soft text-berry': tone === 'berry',
      }"
    >
      <UiIcon :name="icon" class="size-[21px]" />
    </div>
    <div class="min-w-0">
      <div class="truncate font-display text-[23px] font-bold leading-tight">
        {{ value }}
      </div>
      <div class="text-xs text-mute">{{ label }}</div>
    </div>
  </div>
</template>
