<script setup lang="ts">
import { computed } from 'vue'
import type { ApplicationDto } from '../../api/model'
import BaseBadge from '../ui/BaseBadge.vue'
import UiIcon from '../ui/UiIcon.vue'

const props = defineProps<{ application: ApplicationDto }>()

const hostname = computed(() => {
  try {
    return new URL(props.application.url).hostname
  } catch {
    return props.application.url
  }
})
</script>

<template>
  <a
    :href="application.url"
    target="_blank"
    rel="noopener noreferrer"
    class="group flex flex-col gap-3 rounded-tile border-[1.5px] border-line bg-paper p-[18px] transition-[transform,box-shadow,border-color] duration-200 ease-out hover:-translate-y-[3px] hover:border-amber-soft hover:shadow-lift"
  >
    <div class="flex size-13 items-center justify-center rounded-xl bg-cream">
      <img
        v-if="application.hasLogo"
        :src="`/api/applications/${application.id}/logo?v=${application.updatedAt ?? ''}`"
        :alt="`Logo de ${application.name}`"
        class="size-8 object-contain"
      />
      <span
        v-else
        class="font-display text-[22px] font-extrabold text-amber-deep"
        aria-hidden="true"
      >
        {{ application.name.charAt(0).toUpperCase() }}
      </span>
    </div>

    <div class="flex items-center gap-2">
      <h3 class="truncate font-display text-[17px] font-bold">
        {{ application.name }}
      </h3>
      <BaseBadge v-if="application.requiresVpn" color="berry">
        <UiIcon name="lock" class="size-3" />
        VPN
      </BaseBadge>
    </div>
    <p class="-mt-2 line-clamp-2 text-[13px] leading-snug text-ink-soft">
      {{ application.description }}
    </p>

    <div class="mt-auto flex items-center justify-between">
      <span class="truncate font-mono text-[11px] text-mute">{{
        hostname
      }}</span>
      <UiIcon
        name="arrow-up-right"
        class="size-4 shrink-0 text-mute opacity-0 transition-opacity duration-200 group-hover:opacity-100"
      />
    </div>
  </a>
</template>
