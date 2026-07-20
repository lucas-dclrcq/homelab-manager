<script setup lang="ts">
import UiIcon from '../ui/UiIcon.vue'

interface Tab {
  key: string
  label: string
  icon: string
}

defineProps<{ tabs: readonly Tab[]; modelValue: string }>()
defineEmits<{ 'update:modelValue': [key: string] }>()
</script>

<template>
  <div class="-mx-1 overflow-x-auto px-1">
    <nav
      class="inline-flex min-w-full gap-1 rounded-2xl border-[1.5px] border-line bg-paper p-1"
    >
      <button
        v-for="tab in tabs"
        :key="tab.key"
        type="button"
        class="flex shrink-0 cursor-pointer items-center gap-2 rounded-xl px-3.5 py-2 text-sm font-bold whitespace-nowrap transition-colors"
        :class="
          modelValue === tab.key
            ? 'bg-amber text-ink'
            : 'text-ink-soft hover:bg-amber-soft/50'
        "
        @click="$emit('update:modelValue', tab.key)"
      >
        <UiIcon :name="tab.icon" class="size-[18px]" />
        {{ tab.label }}
      </button>
    </nav>
  </div>
</template>
