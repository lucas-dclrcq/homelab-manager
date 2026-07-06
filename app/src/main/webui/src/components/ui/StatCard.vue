<script setup lang="ts">
defineProps<{
  label: string
  value: string
  icon?: string
  accent: string
}>()
</script>

<template>
  <div
    class="relative overflow-hidden rounded-2xl bg-slate-900 p-5 ring-1 ring-slate-800"
  >
    <div
      class="absolute -top-6 -right-6 size-24 rounded-full opacity-20 blur-2xl"
      :style="{ backgroundColor: accent }"
    />
    <div class="flex items-center gap-2 text-sm font-medium text-slate-400">
      <span v-if="icon" aria-hidden="true">{{ icon }}</span>
      {{ label }}
    </div>
    <div class="mt-2 text-3xl font-bold tracking-tight" :style="{ color: accent }">
      {{ value }}
    </div>
  </div>
</template>
