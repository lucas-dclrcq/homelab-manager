<script setup lang="ts">
withDefaults(
  defineProps<{
    label: string
    type?: string
    placeholder?: string
    required?: boolean
    list?: string
    step?: string
  }>(),
  {
    type: 'text',
    placeholder: '',
    required: false,
    list: undefined,
    step: undefined,
  },
)

const model = defineModel<string>({ default: '' })
</script>

<template>
  <label class="block">
    <span class="mb-1.5 block text-xs font-bold text-ink-soft">{{
      label
    }}</span>
    <input
      v-model="model"
      :type="type"
      :placeholder="placeholder"
      :required="required"
      :list="list"
      :step="step"
      class="w-full rounded-xl border-[1.5px] border-line bg-white px-3.5 py-2.5 text-[15px] text-ink transition-[border-color,box-shadow] placeholder:text-mute focus:border-amber focus:ring-[3px] focus:ring-amber/45 focus:outline-none"
    />
  </label>
</template>
