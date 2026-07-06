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
    <span class="mb-1.5 block text-sm font-medium text-slate-300">{{ label }}</span>
    <input
      v-model="model"
      :type="type"
      :placeholder="placeholder"
      :required="required"
      :list="list"
      class="w-full rounded-xl border-0 bg-slate-800 px-3.5 py-2.5 text-sm text-slate-100 ring-1 ring-slate-700 placeholder:text-slate-500 focus:ring-2 focus:ring-brand-400 focus:outline-none"
    />
  </label>
</template>
