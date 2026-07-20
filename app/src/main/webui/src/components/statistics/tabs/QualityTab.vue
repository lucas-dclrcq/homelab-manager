<script setup lang="ts">
import { computed } from 'vue'
import { useGetApiStatisticsQuality } from '../../../api/service/homelab'
import BaseCard from '../../ui/BaseCard.vue'
import BaseSpinner from '../../ui/BaseSpinner.vue'
import QualitySliceChart from '../QualitySliceChart.vue'
import { playbackMethodLabel, type StatsPeriodValue } from '../../../lib/statistics'

const props = defineProps<{ period: StatsPeriodValue }>()

const params = computed(() => ({ period: props.period }))

const { data: quality, isPending } = useGetApiStatisticsQuality(params, {
  query: { staleTime: 60_000 },
})

const hasData = computed(
  () =>
    (quality.value?.resolutions.length ?? 0) > 0 ||
    (quality.value?.videoCodecs.length ?? 0) > 0,
)
</script>

<template>
  <div>
    <BaseSpinner v-if="isPending" />
    <BaseCard v-else-if="!hasData">
      <p class="text-sm text-mute">
        Aucune donnée de qualité sur cette période. Les résolutions, codecs et
        méthodes de lecture sont capturés depuis Jellyfin au fil des lectures (et
        depuis les imports Jellystat qui les contiennent).
      </p>
    </BaseCard>
    <div v-else class="grid gap-6 lg:grid-cols-2">
      <BaseCard>
        <h2 class="mb-4 font-display text-xl font-bold">Résolutions</h2>
        <QualitySliceChart
          :slices="quality!.resolutions"
          variant="doughnut"
        />
      </BaseCard>
      <BaseCard>
        <h2 class="mb-4 font-display text-xl font-bold">Méthode de lecture</h2>
        <QualitySliceChart
          :slices="quality!.playbackMethods"
          variant="doughnut"
          :map-label="playbackMethodLabel"
        />
      </BaseCard>
      <BaseCard>
        <h2 class="mb-4 font-display text-xl font-bold">Codecs vidéo</h2>
        <QualitySliceChart :slices="quality!.videoCodecs" variant="bar" />
      </BaseCard>
      <BaseCard>
        <h2 class="mb-4 font-display text-xl font-bold">Codecs audio</h2>
        <QualitySliceChart :slices="quality!.audioCodecs" variant="bar" />
      </BaseCard>
    </div>
  </div>
</template>
