<script setup lang="ts">
import ApplicationsByCategory from '../components/app/ApplicationsByCategory.vue'
</script>

<template>
  <div class="flex flex-col gap-8">
    <header>
      <h1
        class="font-display text-4xl font-bold tracking-tight text-stone-900 underline decoration-brand-400/70 decoration-wavy underline-offset-8"
      >
        Applications
      </h1>
      <p class="mt-3 text-sm text-stone-500">
        Les services du homelab — le cadenas indique qu'un VPN est nécessaire.
      </p>
    </header>

    <ApplicationsByCategory />
  </div>
</template>
