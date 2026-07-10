<script setup lang="ts">
import JellystatImportCard from '../components/statistics/JellystatImportCard.vue'
</script>

<template>
  <div class="flex max-w-5xl flex-col gap-8">
    <header>
      <h1 class="roost font-display text-[34px] font-extrabold leading-tight">
        Statistiques
      </h1>
      <p class="mt-2 text-ink-soft">
        Administration des statistiques de visionnage : import de l'historique
        Jellystat.
      </p>
    </header>

    <JellystatImportCard />
  </div>
</template>
