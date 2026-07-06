<script setup lang="ts">
import StatsPanel from '../components/app/StatsPanel.vue'
import HomelabTimeline from '../components/app/HomelabTimeline.vue'
</script>

<template>
  <div class="flex flex-col gap-8">
    <header>
      <h1
        class="font-display text-4xl font-bold tracking-tight text-stone-900 underline decoration-brand-400/70 decoration-wavy underline-offset-8"
      >
        Dashboard
      </h1>
      <p class="mt-3 text-sm text-stone-500">L'état du homelab en un coup d'œil.</p>
    </header>

    <StatsPanel />

    <div>
      <h2 class="font-display mb-4 text-2xl font-bold text-stone-800">Activité récente</h2>
      <HomelabTimeline />
    </div>
  </div>
</template>
