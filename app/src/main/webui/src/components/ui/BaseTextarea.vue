<script setup lang="ts">
withDefaults(
  defineProps<{ label: string; placeholder?: string; required?: boolean; rows?: number }>(),
  { placeholder: '', required: false, rows: 3 },
)

const model = defineModel<string>({ default: '' })
</script>

<template>
  <label class="block">
    <span class="mb-1.5 block text-sm font-medium text-stone-600">{{ label }}</span>
    <textarea
      v-model="model"
      :placeholder="placeholder"
      :required="required"
      :rows="rows"
      class="sketchy-sm w-full border-2 border-stone-400 bg-card px-3.5 py-2 text-sm text-stone-800 placeholder:text-stone-400 focus:border-brand-500 focus:outline-none"
    />
  </label>
</template>
