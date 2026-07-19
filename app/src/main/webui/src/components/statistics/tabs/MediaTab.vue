<script setup lang="ts">
import TopMediaTable from '../TopMediaTable.vue'
import LeastWatchedCard from '../LeastWatchedCard.vue'
import { type StatsPeriodValue } from '../../../lib/statistics'

defineProps<{ period: StatsPeriodValue }>()
</script>

<template>
  <div class="flex flex-col gap-8">
    <TopMediaTable :period="period" />
    <LeastWatchedCard :period="period" />
  </div>
</template>
