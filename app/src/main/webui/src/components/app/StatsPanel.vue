<script setup lang="ts">
import { useGetApiStats } from '../../api/service/homelab'
import { formatBytes } from '../../lib/format'
import StatCard from '../ui/StatCard.vue'
import BaseSpinner from '../ui/BaseSpinner.vue'

const { data: stats, isPending, isError } = useGetApiStats()
</script>

<template>
  <section aria-label="Statistiques du homelab">
    <BaseSpinner v-if="isPending" />
    <p
      v-else-if="isError"
      class="sketchy-sm border-2 border-dashed border-rose-300 bg-rose-50 p-4 text-sm text-rose-700"
    >
      Impossible de récupérer les statistiques.
    </p>
    <div v-else-if="stats" class="grid grid-cols-2 gap-4 lg:grid-cols-5">
      <StatCard label="Films" icon="🎬" accent="#c1663f" :value="stats.movieCount.toLocaleString('fr-FR')" />
      <StatCard label="Séries" icon="📺" accent="#5d84a6" :value="stats.seriesCount.toLocaleString('fr-FR')" />
      <StatCard label="Épisodes" icon="🎞️" accent="#8a6ea3" :value="stats.episodeCount.toLocaleString('fr-FR')" />
      <StatCard label="Disque occupé" icon="💾" accent="#c99a2e" :value="formatBytes(stats.diskUsedBytes)" />
      <StatCard label="Disque restant" icon="🌱" accent="#5f854a" :value="formatBytes(stats.diskFreeBytes)" />
    </div>
  </section>
</template>
