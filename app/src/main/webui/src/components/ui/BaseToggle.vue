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
      class="relative h-6 w-11 rounded-full transition-colors"
      :class="model ? 'bg-brand-500' : 'bg-slate-700'"
      @click="model = !model"
    >
      <span
        class="absolute top-0.5 left-0.5 size-5 rounded-full bg-white transition-transform"
        :class="{ 'translate-x-5': model }"
      />
    </button>
    <span class="text-sm font-medium text-slate-300">{{ label }}</span>
  </label>
</template>
