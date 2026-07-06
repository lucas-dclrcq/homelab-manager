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
    class="sketchy-sm inline-flex cursor-pointer items-center justify-center gap-2 border-2 px-4 py-1.5 text-sm font-semibold transition-all focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-brand-500 disabled:cursor-not-allowed disabled:opacity-50"
    :class="{
      'border-brand-700 bg-brand-500 text-white shadow-doodle hover:translate-x-[2px] hover:translate-y-[2px] hover:bg-brand-600 hover:shadow-none':
        variant === 'primary',
      'border-stone-400 bg-card text-stone-700 shadow-doodle hover:translate-x-[2px] hover:translate-y-[2px] hover:bg-stone-100 hover:shadow-none':
        variant === 'secondary',
      'border-transparent text-stone-500 hover:bg-stone-200/60 hover:text-stone-800':
        variant === 'ghost',
    }"
  >
    <span
      v-if="loading"
      class="size-4 animate-spin rounded-full border-2 border-stone-300 border-t-current"
      aria-hidden="true"
    />
    <slot />
  </button>
</template>
