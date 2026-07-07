<script setup lang="ts">
import { onMounted, onUnmounted } from 'vue'
import UiIcon from './UiIcon.vue'

defineProps<{ title: string }>()
const emit = defineEmits<{ close: [] }>()

function onKeydown(event: KeyboardEvent) {
  if (event.key === 'Escape') emit('close')
}
onMounted(() => document.addEventListener('keydown', onKeydown))
onUnmounted(() => document.removeEventListener('keydown', onKeydown))
</script>

<template>
  <Teleport to="body">
    <div
      class="fixed inset-0 z-50 flex items-center justify-center p-4"
      role="dialog"
      aria-modal="true"
      :aria-label="title"
    >
      <div
        class="absolute inset-0 bg-ink/50"
        aria-hidden="true"
        @click="emit('close')"
      />
      <div
        class="relative max-h-[90vh] w-full max-w-xl overflow-y-auto rounded-tile border-[1.5px] border-line bg-paper p-6 shadow-lift"
      >
        <div class="mb-5 flex items-start justify-between gap-4">
          <h2 class="font-display text-[22px] font-bold">{{ title }}</h2>
          <button
            type="button"
            class="flex size-8 shrink-0 cursor-pointer items-center justify-center rounded-xl text-mute transition-colors hover:bg-cream hover:text-ink"
            aria-label="Fermer"
            @click="emit('close')"
          >
            <UiIcon name="x" class="size-5" />
          </button>
        </div>
        <slot />
      </div>
    </div>
  </Teleport>
</template>
