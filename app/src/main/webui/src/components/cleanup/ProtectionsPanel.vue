<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import type { CleanupProtectionDto } from '../../api/model'
import { useDeleteProtection, useProtections } from '../../lib/cleanupApi'
import { protectionSourcePresentation } from '../../lib/cleanup'
import { useUserStore } from '../../stores/user'
import BaseBadge from '../ui/BaseBadge.vue'
import BaseButton from '../ui/BaseButton.vue'
import BaseCard from '../ui/BaseCard.vue'
import BaseSpinner from '../ui/BaseSpinner.vue'
import UiIcon from '../ui/UiIcon.vue'
import ProtectMediaModal from './ProtectMediaModal.vue'

const PAGE_SIZE = 8

const userStore = useUserStore()

const showSearch = ref(false)

const page = ref(0)
const params = computed(() => ({ page: page.value, pageSize: PAGE_SIZE }))
const { data, isPending, isError } = useProtections(params)

const protections = computed(() => data.value?.items ?? [])
const total = computed(() => data.value?.total ?? 0)
const totalPages = computed(() => Math.ceil(total.value / PAGE_SIZE))

// La suppression peut vider la dernière page : on recale sur une page valide
watch(totalPages, (count) => {
  if (page.value >= count) page.value = Math.max(0, count - 1)
})

const { mutate: deleteProtection, isPending: isDeleting } =
  useDeleteProtection(false)

function isMine(protection: CleanupProtectionDto): boolean {
  return protection.protectedBy === userStore.username
}

function protectionTitle(protection: CleanupProtectionDto): string {
  const base = protection.year
    ? `${protection.title} (${protection.year})`
    : protection.title
  return protection.seasonNumber != null
    ? `${base} — Saison ${protection.seasonNumber}`
    : base
}
</script>

<template>
  <BaseCard>
    <div class="flex items-start justify-between gap-3">
      <div>
        <div class="flex items-center gap-2">
          <UiIcon name="shield" class="size-5 text-sage" />
          <h2 class="font-display text-xl font-bold">Sous protection</h2>
        </div>
        <p class="mt-1 text-sm text-ink-soft">
          Ces médias sont à l'abri : aucune campagne de nettoyage n'y touchera.
          Tu peux en mettre d'autres au chaud dès maintenant.
        </p>
      </div>
      <BaseButton class="shrink-0" @click="showSearch = true">
        <UiIcon name="shield" class="size-4" />
        Protéger un média
      </BaseButton>
    </div>

    <div v-if="isPending" class="py-6">
      <BaseSpinner />
    </div>

    <p v-else-if="isError" class="mt-4 text-sm font-bold text-berry">
      Impossible de charger les protections. Réessaie dans un instant.
    </p>

    <template v-else-if="total > 0">
      <ul class="mt-4 flex flex-col gap-2">
        <li
          v-for="protection in protections"
          :key="protection.id"
          class="flex items-center gap-3 rounded-card border-[1.5px] border-line bg-white p-3"
        >
          <img
            v-if="protection.posterUrl"
            :src="protection.posterUrl"
            alt=""
            class="h-14 w-10 shrink-0 rounded-md object-cover"
          />
          <span
            v-else
            class="flex h-14 w-10 shrink-0 items-center justify-center rounded-md bg-cream text-mute"
          >
            <UiIcon
              :name="protection.mediaKind === 'MOVIE' ? 'film' : 'tv'"
              class="size-5"
            />
          </span>
          <div class="min-w-0 flex-1">
            <p class="truncate font-display font-bold">
              {{ protectionTitle(protection) }}
            </p>
            <p class="text-xs text-mute">
              Protégé par {{ protection.protectedBy }}
            </p>
          </div>
          <BaseBadge
            :color="protectionSourcePresentation(protection.source).color"
          >
            {{ protectionSourcePresentation(protection.source).label }}
          </BaseBadge>
          <button
            v-if="isMine(protection)"
            type="button"
            class="rounded-lg p-1.5 text-mute transition-colors hover:bg-berry-soft hover:text-berry disabled:opacity-40"
            aria-label="Retirer la protection"
            title="Retirer la protection"
            :disabled="isDeleting"
            @click="deleteProtection({ id: protection.id })"
          >
            <UiIcon name="trash-2" class="size-4" />
          </button>
        </li>
      </ul>

      <div
        v-if="totalPages > 1"
        class="mt-4 flex items-center justify-between text-xs text-mute"
      >
        <span>{{ total }} médias protégés</span>
        <div class="flex items-center gap-2">
          <button
            type="button"
            class="rounded-full bg-line/60 px-3 py-1.5 font-bold text-ink-soft transition-colors hover:bg-line disabled:opacity-40"
            :disabled="page === 0"
            @click="page--"
          >
            Précédent
          </button>
          <span>{{ page + 1 }} / {{ totalPages }}</span>
          <button
            type="button"
            class="rounded-full bg-line/60 px-3 py-1.5 font-bold text-ink-soft transition-colors hover:bg-line disabled:opacity-40"
            :disabled="page + 1 >= totalPages"
            @click="page++"
          >
            Suivant
          </button>
        </div>
      </div>
    </template>

    <p v-else class="mt-4 text-sm text-mute">
      Aucune protection pour l'instant. Un média auquel tu tiens ? Clique sur «
      Protéger un média » et mets-le à l'abri.
    </p>

    <ProtectMediaModal v-if="showSearch" @close="showSearch = false" />
  </BaseCard>
</template>
