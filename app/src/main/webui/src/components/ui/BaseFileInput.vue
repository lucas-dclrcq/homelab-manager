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
    <span class="mb-1.5 block text-sm font-medium text-slate-300">{{ label }}</span>
    <div class="flex items-center gap-3">
      <input
        ref="inputRef"
        type="file"
        :accept="accept"
        class="block w-full cursor-pointer text-sm text-slate-400 file:mr-3 file:cursor-pointer file:rounded-xl file:border-0 file:bg-slate-800 file:px-3.5 file:py-2 file:text-sm file:font-medium file:text-slate-200 hover:file:bg-slate-700"
        @change="onChange"
      />
      <button
        v-if="model"
        type="button"
        class="cursor-pointer text-xs text-slate-500 hover:text-slate-300"
        @click="clear"
      >
        ✕
      </button>
    </div>
  </label>
</template>
