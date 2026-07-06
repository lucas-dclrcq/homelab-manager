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
    <p v-else-if="isError" class="rounded-xl bg-rose-500/10 p-4 text-sm text-rose-300">
      Impossible de récupérer les statistiques.
    </p>
    <div v-else-if="stats" class="grid grid-cols-2 gap-4 lg:grid-cols-5">
      <StatCard label="Films" icon="🎬" accent="#8b5cf6" :value="stats.movieCount.toLocaleString('fr-FR')" />
      <StatCard label="Séries" icon="📺" accent="#06b6d4" :value="stats.seriesCount.toLocaleString('fr-FR')" />
      <StatCard label="Épisodes" icon="🎞️" accent="#3b82f6" :value="stats.episodeCount.toLocaleString('fr-FR')" />
      <StatCard label="Disque occupé" icon="💾" accent="#f59e0b" :value="formatBytes(stats.diskUsedBytes)" />
      <StatCard label="Disque restant" icon="🟢" accent="#10b981" :value="formatBytes(stats.diskFreeBytes)" />
    </div>
  </section>
</template>
