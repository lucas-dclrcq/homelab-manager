<script setup lang="ts">
import JobsPanel from '../components/app/JobsPanel.vue'
</script>

<template>
  <div class="flex max-w-5xl flex-col gap-8">
    <header>
      <h1 class="roost font-display text-[34px] font-extrabold leading-tight">
        Tâches
      </h1>
      <p class="mt-2 text-ink-soft">
        Les synchronisations et routines planifiées de la maison.
      </p>
    </header>

    <JobsPanel />
  </div>
</template>
