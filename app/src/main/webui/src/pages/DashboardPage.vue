<script setup lang="ts">
import { computed } from 'vue'
import { useUserStore } from '../stores/user'
import StatsPanel from '../components/app/StatsPanel.vue'
import HomelabTimeline from '../components/app/HomelabTimeline.vue'

const userStore = useUserStore()

const greeting = computed(() => {
  const hour = new Date().getHours()
  return hour >= 18 || hour < 5 ? 'Bonsoir' : 'Bonjour'
})
</script>

<template>
  <div class="flex flex-col gap-8">
    <header>
      <h1 class="roost font-display text-[34px] font-extrabold leading-tight">
        {{ greeting
        }}{{ userStore.username ? `, ${userStore.username}` : '' }} 🦉
      </h1>
      <p class="mt-2 text-ink-soft">Voilà ce qui se passe sur Hoohoot.</p>
    </header>

    <StatsPanel />

    <section>
      <h2 class="mb-4 font-display text-[21px] font-bold">Activité récente</h2>
      <HomelabTimeline />
    </section>
  </div>
</template>
