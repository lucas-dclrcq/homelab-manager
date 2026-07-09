<script setup lang="ts">
import { computed } from 'vue'
import { useGetApiApplications } from '../../api/service/homelab'
import type { ApplicationDto } from '../../api/model'
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
    class="rounded-card border-[1.5px] border-berry/30 bg-berry-soft p-4 text-sm text-berry"
  >
    Les applis font la sieste — on réessaie dans un instant ?
  </p>
  <p
    v-else-if="byCategory.size === 0"
    class="rounded-tile border-[1.5px] border-line bg-paper p-6 text-sm text-ink-soft"
  >
    Aucune appli pour l'instant 🦉
  </p>
  <div v-else class="flex flex-col gap-8">
    <section
      v-for="[category, apps] in byCategory"
      :key="category"
      :aria-label="category"
    >
      <div class="mb-3.5 flex items-baseline gap-3">
        <h2 class="font-display text-[21px] font-bold whitespace-nowrap">
          {{ category }}
        </h2>
        <span class="text-[13px] text-mute"
          >{{ apps.length }} appli{{ apps.length > 1 ? 's' : '' }}</span
        >
      </div>
      <div
        class="grid gap-4 [grid-template-columns:repeat(auto-fill,minmax(220px,1fr))]"
      >
        <ApplicationCard v-for="app in apps" :key="app.id" :application="app" />
      </div>
    </section>
  </div>
</template>
