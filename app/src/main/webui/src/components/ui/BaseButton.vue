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
    class="inline-flex cursor-pointer items-center justify-center gap-2 rounded-xl border-[1.5px] px-5 py-2.5 font-display text-sm font-bold transition-[background-color,transform] duration-150 ease-out focus-visible:ring-[3px] focus-visible:ring-amber/45 focus-visible:outline-none active:scale-[0.98] disabled:cursor-not-allowed disabled:opacity-50"
    :class="{
      'border-ink bg-amber text-ink hover:bg-amber-deep': variant === 'primary',
      'border-ink bg-ink text-amber hover:bg-ink-deep': variant === 'secondary',
      'border-line bg-transparent text-ink hover:bg-paper': variant === 'ghost',
    }"
  >
    <span
      v-if="loading"
      class="size-4 animate-spin rounded-full border-2 border-current border-t-transparent opacity-70"
      aria-hidden="true"
    />
    <slot />
  </button>
</template>
