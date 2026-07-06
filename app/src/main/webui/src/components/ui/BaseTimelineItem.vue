<script setup lang="ts">
defineProps<{
  icon: string
  accent: string
  timestamp: string
}>()
</script>

<template>
  <li class="relative pb-6 last:pb-0">
    <span
      class="absolute -start-[37px] flex size-6 items-center justify-center rounded-full text-xs ring-4 ring-slate-950"
      :style="{ backgroundColor: accent + '33', color: accent }"
      aria-hidden="true"
    >
      {{ icon }}
    </span>
    <div class="flex flex-wrap items-baseline gap-x-3 gap-y-1">
      <slot />
      <time class="text-xs text-slate-500">{{ timestamp }}</time>
    </div>
    <div class="mt-0.5 text-sm text-slate-400">
      <slot name="details" />
    </div>
  </li>
</template>
