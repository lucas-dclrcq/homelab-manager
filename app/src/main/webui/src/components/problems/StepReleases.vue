<script setup lang="ts">
import { computed, ref } from 'vue'
import type { ProblemReleaseDto } from '../../api/model'
import { useGrabMutation, useReleasesQuery } from '../../lib/problemsApi'
import BaseBadge from '../ui/BaseBadge.vue'
import BaseButton from '../ui/BaseButton.vue'
import BaseToggle from '../ui/BaseToggle.vue'
import UiIcon from '../ui/UiIcon.vue'
import { formatBytes } from '../../lib/format'

const props = withDefaults(
  defineProps<{ workflowId: string; admin?: boolean }>(),
  { admin: false },
)

const showAll = ref(false)
const grabbingGuid = ref<string | null>(null)
const grabError = ref(false)

const {
  data: releases,
  isPending,
  isError,
  refetch,
} = useReleasesQuery(props.admin, props.workflowId)

const visibleReleases = computed(() => {
  const all = releases.value ?? []
  return showAll.value ? all : all.filter((r) => r.isRecommended)
})

const { mutate: grab, isPending: isGrabbing } = useGrabMutation(props.admin, {
  onError: () => {
    grabError.value = true
    grabbingGuid.value = null
  },
})

function grabRelease(release: ProblemReleaseDto) {
  grabError.value = false
  grabbingGuid.value = release.guid
  grab({
    id: props.workflowId,
    data: {
      guid: release.guid,
      indexerId: release.indexerId,
      title: release.title,
      indexer: release.indexer,
      quality: release.quality,
      size: release.size,
    },
  })
}
</script>

<template>
  <div class="flex flex-col gap-4">
    <div v-if="isPending" class="flex flex-col items-center gap-3 p-8">
      <span
        class="size-8 animate-spin rounded-full border-3 border-amber-soft border-t-amber"
        aria-hidden="true"
      />
      <p class="text-center text-sm text-ink-soft">
        On secoue les indexers pour trouver des versions françaises… ça peut
        prendre une bonne minute, le temps d'un café.
      </p>
    </div>

    <div
      v-else-if="isError"
      class="flex flex-col items-center gap-3 rounded-card border-[1.5px] border-line bg-cream p-6 text-center"
    >
      <UiIcon name="circle-alert" class="size-6 text-berry" />
      <p class="text-sm text-ink-soft">
        La recherche a échoué, les indexers font des leurs. On retente ?
      </p>
      <BaseButton variant="ghost" @click="refetch()">Relancer</BaseButton>
    </div>

    <template v-else>
      <div class="flex flex-wrap items-center justify-between gap-3">
        <p class="text-sm text-ink-soft">
          {{ visibleReleases.length }}
          {{ showAll ? 'release(s)' : 'version(s) recommandée(s)' }} — choisis
          celle à télécharger.
        </p>
        <BaseToggle v-model="showAll" label="Voir toutes les releases" />
      </div>

      <p
        v-if="grabError"
        class="rounded-card border-[1.5px] border-berry/40 bg-berry-soft p-3 text-sm text-berry"
      >
        Radarr a refusé le téléchargement — la release n'est peut-être plus
        disponible. Réessaie avec une autre.
      </p>

      <ul v-if="visibleReleases.length" class="flex flex-col gap-2">
        <li
          v-for="release in visibleReleases"
          :key="release.guid"
          class="rounded-card border-[1.5px] bg-white p-4"
          :class="release.isRecommended ? 'border-sage/50' : 'border-line'"
        >
          <div class="flex items-start justify-between gap-3">
            <div
              class="min-w-0 flex-1"
              :class="{ 'opacity-60': release.rejected }"
            >
              <p class="font-mono text-[13px] break-all text-ink">
                {{ release.title }}
              </p>
              <p class="mt-2 flex flex-wrap items-center gap-2">
                <BaseBadge v-if="release.isRecommended" color="sage">
                  Recommandée
                </BaseBadge>
                <BaseBadge v-if="release.isFrench" color="amber">
                  VF/MULTI
                </BaseBadge>
                <BaseBadge v-if="release.quality" color="sky">
                  {{ release.quality }}
                </BaseBadge>
                <span v-if="release.size" class="text-xs text-mute">
                  {{ formatBytes(release.size) }}
                </span>
                <span v-if="release.seeders != null" class="text-xs text-mute">
                  {{ release.seeders }} seeders
                </span>
                <span v-if="release.indexer" class="text-xs text-mute">
                  {{ release.indexer }}
                </span>
              </p>
              <p
                v-if="release.rejected && release.rejections.length"
                class="mt-2 text-xs text-berry"
              >
                Refusée par Radarr : {{ release.rejections.join(' · ') }}
              </p>
            </div>
            <BaseButton
              :variant="release.rejected ? 'danger' : 'primary'"
              :loading="isGrabbing && grabbingGuid === release.guid"
              :disabled="isGrabbing"
              @click="grabRelease(release)"
            >
              <UiIcon name="download" class="size-4" />
              {{ release.rejected ? 'Forcer' : 'Télécharger' }}
            </BaseButton>
          </div>
        </li>
      </ul>

      <p
        v-else
        class="rounded-card border-[1.5px] border-dashed border-line bg-cream p-4 text-sm text-ink-soft"
      >
        Aucune version recommandée pour l'instant. Active « Voir toutes les
        releases » pour parcourir tout ce que les indexers proposent.
      </p>
    </template>
  </div>
</template>
