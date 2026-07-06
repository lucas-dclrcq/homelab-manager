<script setup lang="ts">
import AddApplicationForm from '../components/app/AddApplicationForm.vue'
</script>

<template>
  <div class="flex max-w-2xl flex-col gap-8">
    <header>
      <h1
        class="font-display text-4xl font-bold tracking-tight text-stone-900 underline decoration-brand-400/70 decoration-wavy underline-offset-8"
      >
        Administration
      </h1>
      <p class="mt-3 text-sm text-stone-500">Gérez le catalogue d'applications du portail.</p>
    </header>

    <AddApplicationForm />
  </div>
</template>
