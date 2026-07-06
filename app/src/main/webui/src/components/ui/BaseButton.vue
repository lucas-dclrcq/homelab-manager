<script setup lang="ts">
withDefaults(
  defineProps<{
    variant?: 'primary' | 'secondary' | 'ghost'
    type?: 'button' | 'submit'
    disabled?: boolean
    loading?: boolean
  }>(),
  { variant: 'primary', type: 'button', disabled: false, loading: false },
)
</script>

<template>
  <button
    :type="type"
    :disabled="disabled || loading"
    class="inline-flex cursor-pointer items-center justify-center gap-2 rounded-xl px-4 py-2 text-sm font-semibold transition-colors focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand-400 disabled:cursor-not-allowed disabled:opacity-50"
    :class="{
      'bg-gradient-to-r from-brand-600 to-fuchsia-600 text-white hover:from-brand-500 hover:to-fuchsia-500':
        variant === 'primary',
      'bg-slate-800 text-slate-100 ring-1 ring-slate-700 hover:bg-slate-700': variant === 'secondary',
      'text-slate-300 hover:bg-slate-800 hover:text-white': variant === 'ghost',
    }"
  >
    <span
      v-if="loading"
      class="size-4 animate-spin rounded-full border-2 border-white/40 border-t-white"
      aria-hidden="true"
    />
    <slot />
  </button>
</template>
