<script setup lang="ts">
import { ref } from 'vue'

withDefaults(defineProps<{ label: string; accept?: string }>(), {
  accept: undefined,
})

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
    <span class="mb-1.5 block text-xs font-bold text-ink-soft">{{
      label
    }}</span>
    <div class="flex items-center gap-3">
      <input
        ref="inputRef"
        type="file"
        :accept="accept"
        class="block w-full cursor-pointer text-sm text-ink-soft file:mr-3 file:cursor-pointer file:rounded-xl file:border-[1.5px] file:border-line file:bg-paper file:px-4 file:py-2 file:font-display file:text-sm file:font-bold file:text-ink hover:file:bg-amber-soft"
        @change="onChange"
      />
      <button
        v-if="model"
        type="button"
        class="cursor-pointer text-xs text-mute hover:text-ink-soft"
        @click="clear"
      >
        ✕
      </button>
    </div>
  </label>
</template>
