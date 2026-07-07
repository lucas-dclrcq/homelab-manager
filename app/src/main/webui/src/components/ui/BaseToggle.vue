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
      class="relative h-[27px] w-[46px] shrink-0 rounded-full transition-colors duration-200 focus-visible:ring-[3px] focus-visible:ring-amber/45 focus-visible:outline-none"
      :class="model ? 'bg-sage' : 'bg-line'"
      @click="model = !model"
    >
      <span
        class="absolute top-[3px] left-[3px] size-[21px] rounded-full bg-white shadow-sm transition-transform duration-200"
        :class="{ 'translate-x-[19px]': model }"
      />
    </button>
    <span class="text-sm font-semibold text-ink-soft">{{ label }}</span>
  </label>
</template>
