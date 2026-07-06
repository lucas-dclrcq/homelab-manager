<script setup lang="ts">
import ApplicationsByCategory from '../components/app/ApplicationsByCategory.vue'
</script>

<template>
  <div class="flex flex-col gap-8">
    <header>
      <h1 class="text-2xl font-black tracking-tight">Applications</h1>
      <p class="mt-1 text-sm text-slate-400">
        Les services du homelab — le cadenas indique qu'un VPN est nécessaire.
      </p>
    </header>

    <ApplicationsByCategory />
  </div>
</template>
