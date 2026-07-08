<script setup lang="ts">
import { useGetApiApplications } from '../../api/service/homelab'
import type { ApplicationDto } from '../../api/model'
import BaseCard from '../ui/BaseCard.vue'
import BaseBadge from '../ui/BaseBadge.vue'
import BaseButton from '../ui/BaseButton.vue'
import BaseSpinner from '../ui/BaseSpinner.vue'
import UiIcon from '../ui/UiIcon.vue'

defineEmits<{
  edit: [application: ApplicationDto]
  delete: [application: ApplicationDto]
}>()

const { data: applications, isPending, isError } = useGetApiApplications()

function hostname(url: string) {
  try {
    return new URL(url).hostname
  } catch {
    return url
  }
}
</script>

<template>
  <BaseCard>
    <BaseSpinner v-if="isPending" />
    <p
      v-else-if="isError"
      class="rounded-card border-[1.5px] border-berry/30 bg-berry-soft p-4 text-sm text-berry"
    >
      Impossible de récupérer le catalogue — on réessaie ?
    </p>
    <p v-else-if="!applications?.length" class="text-sm text-ink-soft">
      Aucune appli dans le catalogue pour l'instant.
    </p>

    <div v-else class="overflow-x-auto">
      <table class="w-full text-left text-sm">
        <thead>
          <tr
            class="border-b-[1.5px] border-line text-xs font-bold tracking-[0.06em] text-mute uppercase"
          >
            <th class="py-2 pr-4">Appli</th>
            <th class="py-2 pr-4">Catégorie</th>
            <th class="py-2 pr-4">URL</th>
            <th class="py-2 pr-4">VPN</th>
            <th class="py-2">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="application in applications"
            :key="application.id"
            class="border-b border-line last:border-none"
          >
            <td class="py-3 pr-4">
              <div class="flex items-center gap-3">
                <span
                  class="flex size-9 shrink-0 items-center justify-center rounded-xl bg-cream"
                >
                  <img
                    v-if="application.hasLogo"
                    :src="`/api/applications/${application.id}/logo?v=${application.updatedAt ?? ''}`"
                    :alt="`Logo de ${application.name}`"
                    class="size-6 object-contain"
                  />
                  <span
                    v-else
                    class="font-display text-sm font-extrabold text-amber-deep"
                    aria-hidden="true"
                  >
                    {{ application.name.charAt(0).toUpperCase() }}
                  </span>
                </span>
                <div class="min-w-0">
                  <p
                    class="flex items-center gap-2 font-semibold text-ink"
                    :title="
                      application.managedBy
                        ? `Synchronisée depuis le cluster (${application.externalId}) — les modifications manuelles seront écrasées`
                        : undefined
                    "
                  >
                    {{ application.name }}
                    <BaseBadge v-if="application.managedBy" color="sky">
                      <UiIcon name="refresh-cw" class="size-3" />
                      Synchronisée
                    </BaseBadge>
                  </p>
                  <p class="line-clamp-1 max-w-xs text-xs text-mute">
                    {{ application.description }}
                  </p>
                </div>
              </div>
            </td>
            <td class="py-3 pr-4 text-ink-soft">{{ application.category }}</td>
            <td class="py-3 pr-4">
              <a
                :href="application.url"
                target="_blank"
                rel="noopener noreferrer"
                class="inline-flex items-center gap-1 font-mono text-xs text-ink-soft transition-colors hover:text-amber-deep"
              >
                {{ hostname(application.url) }}
                <UiIcon name="arrow-up-right" class="size-3" />
              </a>
            </td>
            <td class="py-3 pr-4">
              <BaseBadge v-if="application.requiresVpn" color="berry">
                <UiIcon name="lock" class="size-3" />
                VPN
              </BaseBadge>
              <span v-else class="text-mute">—</span>
            </td>
            <td class="py-3">
              <div class="flex gap-2">
                <BaseButton variant="ghost" @click="$emit('edit', application)">
                  <UiIcon name="pencil" class="size-4" />
                  Éditer
                </BaseButton>
                <BaseButton
                  variant="ghost"
                  @click="$emit('delete', application)"
                >
                  <UiIcon name="trash-2" class="size-4 text-berry" />
                  Supprimer
                </BaseButton>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </BaseCard>
</template>
