<script setup lang="ts">
import { ref } from 'vue'

withDefaults(defineProps<{ label: string; accept?: string }>(), { accept: undefined })

const model = defineModel<File | null>({ default: null })
const inputRef = ref<HTMLInputElement>()

function onChange(event: Event) {
  model.value = (event.target as HTMLInputElement).files?.[0] ?? null
}

function clear() {
  model.value = null
  if (inputRef.value) inputRef.value.value = ''
}

defineExpose({ clear })
</script>

<template>
  <label class="block">
    <span class="mb-1.5 block text-sm font-medium text-stone-600">{{ label }}</span>
    <div class="flex items-center gap-3">
      <input
        ref="inputRef"
        type="file"
        :accept="accept"
        class="block w-full cursor-pointer text-sm text-stone-500 file:sketchy-sm file:mr-3 file:cursor-pointer file:border-2 file:border-stone-400 file:bg-card file:px-3.5 file:py-1.5 file:text-sm file:font-medium file:text-stone-700 hover:file:bg-stone-100"
        @change="onChange"
      />
      <button
        v-if="model"
        type="button"
        class="cursor-pointer text-xs text-stone-400 hover:text-stone-600"
        @click="clear"
      >
        ✕
      </button>
    </div>
  </label>
</template>
