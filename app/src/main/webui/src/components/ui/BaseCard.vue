<script setup lang="ts">
withDefaults(defineProps<{ accent?: string }>(), { accent: 'transparent' })
</script>

<template>
  <div class="sketchy border-2 border-stone-700/60 bg-card p-5 shadow-doodle">
    <!-- Trait de feutre marquant l'accent de la carte -->
    <div
      v-if="accent !== 'transparent'"
      class="mb-3 h-1.5 w-16 rounded-full opacity-70"
      :style="{ backgroundColor: accent }"
      aria-hidden="true"
    />
    <slot />
  </div>
</template>
