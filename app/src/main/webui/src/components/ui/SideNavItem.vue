<script setup lang="ts">
import UiIcon from './UiIcon.vue'

defineProps<{
  to: string
  label: string
  icon: string
}>()
</script>

<template>
  <RouterLink
    :to="to"
    class="flex items-center gap-3 rounded-xl px-3 py-2.5 text-[14.5px] font-semibold text-[#d8d2df] transition-colors duration-150 hover:bg-white/10 hover:text-white"
    active-class="!bg-amber !text-ink"
    exact-active-class="!bg-amber !text-ink"
  >
    <UiIcon :name="icon" class="size-[19px] shrink-0" />
    {{ label }}
  </RouterLink>
</template>
