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
    class="sketchy-sm group flex items-center gap-3 px-3.5 py-2.5 text-sm font-medium text-stone-500 transition-colors hover:bg-stone-200/60 hover:text-stone-900"
    active-class="bg-brand-100 !text-brand-900"
    exact-active-class="bg-brand-100 !text-brand-900"
  >
    <span
      class="sketchy-sm flex size-8 items-center justify-center text-base"
      :style="{ backgroundColor: accent + '26', color: accent }"
      aria-hidden="true"
    >
      {{ icon }}
    </span>
    {{ label }}
  </RouterLink>
</template>
