<script setup lang="ts">
import ApplicationsByCategory from '../components/app/ApplicationsByCategory.vue'
</script>

<template>
  <div class="flex flex-col gap-8">
    <header>
      <h1 class="roost font-display text-[34px] font-extrabold leading-tight">
        Tes applis
      </h1>
      <p class="mt-2 text-ink-soft">
        Tout ce que hoohoot héberge — le cadenas veut dire qu'il faut le VPN.
      </p>
    </header>

    <ApplicationsByCategory />
  </div>
</template>
