<script setup lang="ts">
defineProps<{
  label: string
  value: string
  icon?: string
  accent: string
}>()
</script>

<template>
  <div class="sketchy relative overflow-hidden border-2 border-stone-700/60 bg-card p-5 shadow-doodle">
    <div
      class="absolute -top-6 -right-6 size-24 rounded-full opacity-15 blur-2xl"
      :style="{ backgroundColor: accent }"
    />
    <div class="flex items-center gap-2 text-sm font-medium text-stone-500">
      <span v-if="icon" aria-hidden="true">{{ icon }}</span>
      {{ label }}
    </div>
    <div class="font-display mt-1 text-4xl font-bold tracking-tight" :style="{ color: accent }">
      {{ value }}
    </div>
  </div>
</template>
