<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import {
  useGetApiCleanupSearchMovies,
  useGetApiCleanupSearchSeries,
} from '../../api/service/homelab'
import type { CleanupMediaDto } from '../../api/model'
import { cleanupErrorMessage, useCreateProtection } from '../../lib/cleanupApi'
import BaseInput from '../ui/BaseInput.vue'
import BaseModal from '../ui/BaseModal.vue'
import BaseSpinner from '../ui/BaseSpinner.vue'
import UiIcon from '../ui/UiIcon.vue'

const emit = defineEmits<{ close: [] }>()

// Recherche débouncée (300 ms) sur films et séries en parallèle
const searchInput = ref('')
const debouncedQuery = ref('')
let debounceTimer: ReturnType<typeof setTimeout> | undefined

watch(searchInput, (value) => {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(() => {
    debouncedQuery.value = value.trim()
  }, 300)
})

const searchEnabled = computed(() => debouncedQuery.value.length >= 2)
const params = computed(() => ({ query: debouncedQuery.value }))

const {
  data: movies,
  isFetching: isFetchingMovies,
  isError: isMoviesError,
} = useGetApiCleanupSearchMovies(params, { query: { enabled: searchEnabled } })
const {
  data: series,
  isFetching: isFetchingSeries,
  isError: isSeriesError,
} = useGetApiCleanupSearchSeries(params, { query: { enabled: searchEnabled } })

const isSearching = computed(
  () => isFetchingMovies.value || isFetchingSeries.value,
)
const isSearchError = computed(() => isMoviesError.value || isSeriesError.value)
const results = computed(() => [
  ...(movies.value ?? []),
  ...(series.value ?? []),
])

// Série sélectionnée : on propose la série entière ou une saison précise
const seriesChoice = ref<CleanupMediaDto | null>(null)

const protectError = ref('')

const { mutate: createProtection, isPending: isCreating } = useCreateProtection(
  {
    onSuccess: () => {
      protectError.value = ''
      emit('close')
    },
    onError: (error) => {
      protectError.value = cleanupErrorMessage(error)
    },
  },
)

function protect(media: CleanupMediaDto) {
  if (media.mediaKind === 'MOVIE') {
    createProtection({
      data: {
        mediaKind: 'MOVIE',
        radarrMovieId: media.radarrMovieId,
        sonarrSeriesId: null,
        seasonNumber: null,
      },
    })
  } else {
    seriesChoice.value = media
  }
}

function protectSeries(media: CleanupMediaDto, seasonNumber: number | null) {
  createProtection({
    data: {
      mediaKind: seasonNumber == null ? 'SERIES' : 'SEASON',
      radarrMovieId: null,
      sonarrSeriesId: media.sonarrSeriesId,
      seasonNumber,
    },
  })
}
</script>

<template>
  <BaseModal title="Protéger un média" @close="emit('close')">
    <BaseInput
      v-model="searchInput"
      label="Cherche un film ou une série"
      placeholder="Dune, Severance…"
    />

    <p v-if="protectError" class="mt-3 text-sm font-bold text-berry">
      {{ protectError }}
    </p>

    <div v-if="isSearching" class="py-6">
      <BaseSpinner />
    </div>

    <p v-else-if="isSearchError" class="mt-3 text-sm font-bold text-berry">
      La recherche n'a pas abouti. Réessaie dans un instant.
    </p>

    <ul v-else-if="results.length" class="mt-3 flex flex-col gap-2">
      <li
        v-for="media in results"
        :key="`${media.mediaKind}-${media.radarrMovieId ?? media.sonarrSeriesId}`"
      >
        <button
          type="button"
          class="flex w-full cursor-pointer items-center gap-3 rounded-card border-[1.5px] border-line bg-white p-3 text-left transition-colors hover:border-amber disabled:cursor-not-allowed disabled:opacity-50"
          :disabled="isCreating"
          @click="protect(media)"
        >
          <img
            v-if="media.posterUrl"
            :src="media.posterUrl"
            alt=""
            class="h-14 w-10 shrink-0 rounded-md object-cover"
          />
          <span
            v-else
            class="flex h-14 w-10 shrink-0 items-center justify-center rounded-md bg-cream text-mute"
          >
            <UiIcon
              :name="media.mediaKind === 'MOVIE' ? 'film' : 'tv'"
              class="size-5"
            />
          </span>
          <span class="min-w-0 flex-1">
            <span class="block truncate font-display font-bold">
              {{ media.title }}
            </span>
            <span class="text-sm text-mute">
              {{ media.mediaKind === 'MOVIE' ? 'Film' : 'Série' }}
              <template v-if="media.year"> · {{ media.year }}</template>
            </span>
          </span>
          <UiIcon name="shield" class="size-4 shrink-0 text-mute" />
        </button>

        <div
          v-if="
            seriesChoice &&
            seriesChoice.sonarrSeriesId === media.sonarrSeriesId &&
            media.mediaKind !== 'MOVIE'
          "
          class="mt-2 ml-4 rounded-card border-[1.5px] border-dashed border-line bg-cream p-3"
        >
          <p class="mb-2 text-xs font-bold text-ink-soft">
            Tu protèges quoi exactement ?
          </p>
          <div class="flex flex-wrap gap-2">
            <button
              type="button"
              class="cursor-pointer rounded-full bg-ink px-3.5 py-1.5 text-xs font-bold text-cream transition-opacity hover:opacity-85 disabled:opacity-40"
              :disabled="isCreating"
              @click="protectSeries(media, null)"
            >
              Toute la série
            </button>
            <button
              v-for="season in media.seasonNumbers"
              :key="season"
              type="button"
              class="cursor-pointer rounded-full bg-line/60 px-3.5 py-1.5 text-xs font-bold text-ink-soft transition-colors hover:bg-line disabled:opacity-40"
              :disabled="isCreating"
              @click="protectSeries(media, season)"
            >
              Saison {{ season }}
            </button>
          </div>
        </div>
      </li>
    </ul>

    <p
      v-else-if="searchEnabled"
      class="mt-3 rounded-card border-[1.5px] border-dashed border-line bg-cream p-4 text-sm text-ink-soft"
    >
      Rien trouvé avec ce nom dans la bibliothèque. Vérifie l'orthographe ?
    </p>

    <p
      v-else
      class="mt-3 rounded-card border-[1.5px] border-dashed border-line bg-cream p-4 text-sm text-ink-soft"
    >
      Tape au moins deux lettres pour lancer la recherche.
    </p>
  </BaseModal>
</template>
