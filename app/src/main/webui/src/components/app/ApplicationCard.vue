<script setup lang="ts">
import type { ApplicationDto } from '../../api/model'
import { colorFromName } from '../../lib/format'
import BaseCard from '../ui/BaseCard.vue'
import BaseBadge from '../ui/BaseBadge.vue'
import PadlockIcon from '../ui/PadlockIcon.vue'

const props = defineProps<{ application: ApplicationDto }>()

const accent = colorFromName(props.application.name)
</script>

<template>
  <BaseCard :accent="accent">
    <div class="flex items-start gap-4">
      <img
        v-if="application.hasLogo"
        :src="`/api/applications/${application.id}/logo`"
        :alt="`Logo de ${application.name}`"
        class="sketchy-sm size-12 object-contain"
      />
      <div
        v-else
        class="sketchy-sm flex size-12 shrink-0 items-center justify-center text-xl font-bold text-white"
        :style="{ backgroundColor: accent }"
        aria-hidden="true"
      >
        {{ application.name.charAt(0).toUpperCase() }}
      </div>

      <div class="min-w-0 flex-1">
        <div class="flex items-center gap-2">
          <h3 class="truncate font-semibold text-stone-800">{{ application.name }}</h3>
          <BaseBadge v-if="application.requiresVpn" color="rose">
            <PadlockIcon />
            VPN
          </BaseBadge>
        </div>
        <p class="mt-1 line-clamp-2 text-sm text-stone-500">{{ application.description }}</p>
        <a
          :href="application.url"
          target="_blank"
          rel="noopener noreferrer"
          class="mt-3 inline-flex items-center gap-1 text-sm font-medium transition-colors hover:underline hover:decoration-wavy"
          :style="{ color: accent }"
        >
          Ouvrir ↗
        </a>
      </div>
    </div>
  </BaseCard>
</template>
