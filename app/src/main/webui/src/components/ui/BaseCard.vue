<script setup lang="ts">
withDefaults(defineProps<{ accent?: string }>(), { accent: 'transparent' })
</script>

<template>
  <div
    class="rounded-2xl border-t-4 bg-slate-900 p-5 shadow-lg ring-1 ring-slate-800"
    :style="{ borderTopColor: accent }"
  >
    <slot />
  </div>
</template>
