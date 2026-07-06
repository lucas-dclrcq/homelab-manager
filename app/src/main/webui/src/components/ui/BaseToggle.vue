<script setup lang="ts">
defineProps<{ label: string }>()

const model = defineModel<boolean>({ default: false })
</script>

<template>
  <label class="flex cursor-pointer items-center gap-3">
    <button
      type="button"
      role="switch"
      :aria-checked="model"
      class="relative h-6 w-11 rounded-full border-2 transition-colors"
      :class="model ? 'border-brand-700 bg-brand-500' : 'border-stone-400 bg-stone-200'"
      @click="model = !model"
    >
      <span
        class="absolute top-0.5 left-0.5 size-4 rounded-full bg-card shadow-sm transition-transform"
        :class="{ 'translate-x-5': model }"
      />
    </button>
    <span class="text-sm font-medium text-stone-600">{{ label }}</span>
  </label>
</template>
