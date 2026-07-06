<script setup lang="ts">
defineProps<{
  to: string
  label: string
  icon?: string
  accent: string
}>()
</script>

<template>
  <RouterLink
    :to="to"
    class="group flex items-center gap-3 rounded-xl px-3.5 py-2.5 text-sm font-medium text-slate-400 transition-colors hover:bg-slate-800 hover:text-white"
    active-class="bg-slate-800 !text-white"
    exact-active-class="bg-slate-800 !text-white"
  >
    <span
      class="flex size-8 items-center justify-center rounded-lg text-base"
      :style="{ backgroundColor: accent + '26', color: accent }"
      aria-hidden="true"
    >
      {{ icon }}
    </span>
    {{ label }}
  </RouterLink>
</template>
