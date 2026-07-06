<script setup lang="ts">
import { computed } from 'vue'
import { useGetApiApplications } from '../../api/service/homelab'
import type { ApplicationDto } from '../../api/model'
import { colorFromName } from '../../lib/format'
import ApplicationCard from './ApplicationCard.vue'
import BaseSpinner from '../ui/BaseSpinner.vue'

const { data: applications, isPending, isError } = useGetApiApplications()

const byCategory = computed<Map<string, ApplicationDto[]>>(() => {
  const groups = new Map<string, ApplicationDto[]>()
  for (const application of applications.value ?? []) {
    const group = groups.get(application.category) ?? []
    group.push(application)
    groups.set(application.category, group)
  }
  return groups
})
</script>

<template>
  <BaseSpinner v-if="isPending" />
  <p
    v-else-if="isError"
    class="sketchy-sm border-2 border-dashed border-rose-300 bg-rose-50 p-4 text-sm text-rose-700"
  >
    Impossible de récupérer les applications.
  </p>
  <p
    v-else-if="byCategory.size === 0"
    class="sketchy border-2 border-dashed border-stone-300 bg-card p-6 text-sm text-stone-500"
  >
    Aucune application pour l'instant.
  </p>
  <div v-else class="flex flex-col gap-8">
    <section v-for="[category, apps] in byCategory" :key="category" :aria-label="category">
      <h2 class="font-display mb-4 flex items-center gap-2 text-2xl font-bold text-stone-800">
        <span
          class="inline-block size-2.5 rounded-full"
          :style="{ backgroundColor: colorFromName(category) }"
          aria-hidden="true"
        />
        {{ category }}
      </h2>
      <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
        <ApplicationCard v-for="app in apps" :key="app.id" :application="app" />
      </div>
    </section>
  </div>
</template>
