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
      class="rounded-card border-[1.5px] border-berry/30 bg-berry-soft p-4 text-sm text-berry"
    >
      Les statistiques font la sieste — on réessaie dans un instant ?
    </p>
    <div v-else-if="stats" class="grid grid-cols-2 gap-4 lg:grid-cols-5">
      <StatCard
        label="Films"
        icon="film"
        tone="amber"
        :value="stats.movieCount.toLocaleString('fr-FR')"
      />
      <StatCard
        label="Séries"
        icon="tv"
        tone="dusk"
        :value="stats.seriesCount.toLocaleString('fr-FR')"
      />
      <StatCard
        label="Épisodes"
        icon="clapperboard"
        tone="sky"
        :value="stats.episodeCount.toLocaleString('fr-FR')"
      />
      <StatCard
        label="Disque occupé"
        icon="hard-drive"
        tone="berry"
        :value="formatBytes(stats.diskUsedBytes)"
      />
      <StatCard
        label="Disque libre"
        icon="leaf"
        tone="sage"
        :value="formatBytes(stats.diskFreeBytes)"
      />
    </div>
  </section>
</template>
