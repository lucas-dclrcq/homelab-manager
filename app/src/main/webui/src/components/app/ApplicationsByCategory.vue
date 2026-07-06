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
  <p v-else-if="isError" class="rounded-xl bg-rose-500/10 p-4 text-sm text-rose-300">
    Impossible de récupérer les applications.
  </p>
  <p
    v-else-if="byCategory.size === 0"
    class="rounded-xl bg-slate-900 p-6 text-sm text-slate-400"
  >
    Aucune application pour l'instant.
  </p>
  <div v-else class="flex flex-col gap-8">
    <section v-for="[category, apps] in byCategory" :key="category" :aria-label="category">
      <h2 class="mb-4 flex items-center gap-2 text-lg font-bold">
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
