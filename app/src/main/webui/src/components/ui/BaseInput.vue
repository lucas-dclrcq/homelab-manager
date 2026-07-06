<script setup lang="ts">
withDefaults(
  defineProps<{
    label: string
    type?: string
    placeholder?: string
    required?: boolean
    list?: string
  }>(),
  { type: 'text', placeholder: '', required: false, list: undefined },
)

const model = defineModel<string>({ default: '' })
</script>

<template>
  <label class="block">
    <span class="mb-1.5 block text-sm font-medium text-stone-600">{{ label }}</span>
    <input
      v-model="model"
      :type="type"
      :placeholder="placeholder"
      :required="required"
      :list="list"
      class="sketchy-sm w-full border-2 border-stone-400 bg-card px-3.5 py-2 text-sm text-stone-800 placeholder:text-stone-400 focus:border-brand-500 focus:outline-none"
    />
  </label>
</template>
