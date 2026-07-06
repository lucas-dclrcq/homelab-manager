<script setup lang="ts">
import StatsPanel from '../components/app/StatsPanel.vue'
import HomelabTimeline from '../components/app/HomelabTimeline.vue'
</script>

<template>
  <div class="flex flex-col gap-8">
    <header>
      <h1 class="text-2xl font-black tracking-tight">Dashboard</h1>
      <p class="mt-1 text-sm text-slate-400">L'état du homelab en un coup d'œil.</p>
    </header>

    <StatsPanel />

    <div>
      <h2 class="mb-4 text-lg font-bold">Activité récente</h2>
      <HomelabTimeline />
    </div>
  </div>
</template>
