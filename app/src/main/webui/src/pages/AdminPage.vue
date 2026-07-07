<script setup lang="ts">
import AddApplicationForm from '../components/app/AddApplicationForm.vue'
import JobsPanel from '../components/app/JobsPanel.vue'
</script>

<template>
  <div class="flex max-w-4xl flex-col gap-8">
    <header>
      <h1 class="roost font-display text-[34px] font-extrabold leading-tight">
        Atelier
      </h1>
      <p class="mt-2 text-ink-soft">
        Le coin technique de la maison : le catalogue d'applis et les tâches
        planifiées.
      </p>
    </header>

    <div class="max-w-2xl">
      <AddApplicationForm />
    </div>

    <JobsPanel />
  </div>
</template>
