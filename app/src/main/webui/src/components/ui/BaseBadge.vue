<script setup lang="ts">
withDefaults(
  defineProps<{ color?: 'violet' | 'cyan' | 'amber' | 'rose' | 'emerald' | 'slate' }>(),
  { color: 'slate' },
)
</script>

<template>
  <span
    class="inline-flex items-center gap-1 rounded-full px-2.5 py-0.5 text-xs font-medium"
    :class="{
      'bg-violet-500/15 text-violet-300 ring-1 ring-violet-500/30': color === 'violet',
      'bg-cyan-500/15 text-cyan-300 ring-1 ring-cyan-500/30': color === 'cyan',
      'bg-amber-500/15 text-amber-300 ring-1 ring-amber-500/30': color === 'amber',
      'bg-rose-500/15 text-rose-300 ring-1 ring-rose-500/30': color === 'rose',
      'bg-emerald-500/15 text-emerald-300 ring-1 ring-emerald-500/30': color === 'emerald',
      'bg-slate-500/15 text-slate-300 ring-1 ring-slate-500/30': color === 'slate',
    }"
  >
    <slot />
  </span>
</template>
