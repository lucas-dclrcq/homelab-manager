<script setup lang="ts">
import AddApplicationForm from '../components/app/AddApplicationForm.vue'
</script>

<template>
  <div class="flex max-w-2xl flex-col gap-8">
    <header>
      <h1 class="text-2xl font-black tracking-tight">Administration</h1>
      <p class="mt-1 text-sm text-slate-400">Gérez le catalogue d'applications du portail.</p>
    </header>

    <AddApplicationForm />
  </div>
</template>
