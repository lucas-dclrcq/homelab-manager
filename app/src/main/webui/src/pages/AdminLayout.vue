<script setup lang="ts">
import { RouterLink, RouterView } from 'vue-router'

const tabs: { name: string; label: string }[] = [
  { name: 'admin-applications', label: 'Applications' },
  { name: 'admin-finances', label: 'Finances' },
  { name: 'admin-members', label: 'Membres' },
  { name: 'admin-statistics', label: 'Statistiques' },
  { name: 'admin-problems', label: 'Problèmes' },
  { name: 'admin-cleanup', label: 'Nettoyage' },
  { name: 'admin-jobs', label: 'Tâches' },
]
</script>

<template>
  <div class="flex max-w-5xl flex-col gap-8">
    <header>
      <h1 class="roost font-display text-[34px] font-extrabold leading-tight">
        Administration
      </h1>
      <p class="mt-2 text-ink-soft">
        Le coin technique de hoohoot : gère le catalogue, les membres, les
        finances et l'entretien du nid.
      </p>
    </header>

    <div class="flex flex-wrap gap-1.5">
      <RouterLink
        v-for="item in tabs"
        :key="item.name"
        v-slot="{ isActive, href, navigate }"
        :to="{ name: item.name }"
        custom
      >
        <a
          :href="href"
          class="rounded-full px-4 py-2 text-sm font-bold transition-colors"
          :class="
            isActive
              ? 'bg-ink text-cream'
              : 'bg-line/60 text-ink-soft hover:bg-line'
          "
          @click="navigate"
        >
          {{ item.label }}
        </a>
      </RouterLink>
    </div>

    <RouterView />
  </div>
</template>
