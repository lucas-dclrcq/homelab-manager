<script setup lang="ts">
withDefaults(
  defineProps<{ color?: 'violet' | 'cyan' | 'amber' | 'rose' | 'emerald' | 'slate' }>(),
  { color: 'slate' },
)
</script>

<template>
  <span
    class="sketchy-sm inline-flex items-center gap-1 border px-2.5 py-0.5 text-xs font-medium"
    :class="{
      'border-violet-300 bg-violet-100 text-violet-700': color === 'violet',
      'border-cyan-300 bg-cyan-100 text-cyan-800': color === 'cyan',
      'border-amber-300 bg-amber-100 text-amber-800': color === 'amber',
      'border-rose-300 bg-rose-100 text-rose-700': color === 'rose',
      'border-emerald-300 bg-emerald-100 text-emerald-800': color === 'emerald',
      'border-stone-300 bg-stone-100 text-stone-600': color === 'slate',
    }"
  >
    <slot />
  </span>
</template>
