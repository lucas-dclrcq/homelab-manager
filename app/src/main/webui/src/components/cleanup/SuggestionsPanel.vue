<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import {
  useGetApiCleanupSearchMovies,
  useGetApiCleanupSearchSeries,
} from '../../api/service/homelab'
import type { CleanupMediaDto, CleanupSuggestionDto } from '../../api/model'
import {
  cleanupErrorMessage,
  useCreateSuggestion,
  useSuggestions,
} from '../../lib/cleanupApi'
import { daysUntil, suggestionStatusPresentation } from '../../lib/cleanup'
import { formatBytes } from '../../lib/format'
import BaseBadge from '../ui/BaseBadge.vue'
import BaseCard from '../ui/BaseCard.vue'
import BaseInput from '../ui/BaseInput.vue'
import BaseSpinner from '../ui/BaseSpinner.vue'
import UiIcon from '../ui/UiIcon.vue'

const { data: suggestions, isPending, isError } = useSuggestions()

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

const suggestError = ref('')

const { mutate: createSuggestion, isPending: isCreating } = useCreateSuggestion(
  {
    onSuccess: () => {
      suggestError.value = ''
      seriesChoice.value = null
      searchInput.value = ''
      debouncedQuery.value = ''
    },
    onError: (error) => {
      suggestError.value = cleanupErrorMessage(error)
    },
  },
)

function suggest(media: CleanupMediaDto) {
  if (media.mediaKind === 'MOVIE') {
    createSuggestion({
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

function suggestSeries(media: CleanupMediaDto, seasonNumber: number | null) {
  createSuggestion({
    data: {
      mediaKind: seasonNumber == null ? 'SERIES' : 'SEASON',
      radarrMovieId: null,
      sonarrSeriesId: media.sonarrSeriesId,
      seasonNumber,
    },
  })
}

function deadlineLabel(suggestion: CleanupSuggestionDto): string {
  const days = daysUntil(suggestion.deleteAfter)
  if (days <= 0) return 'Suppression imminente, sauf veto'
  return `Suppression dans ${days} j, sauf veto`
}

function suggestionByline(suggestion: CleanupSuggestionDto): string {
  const base = `Proposé par ${suggestion.suggestedBy} · ${formatBytes(suggestion.sizeBytes)}`
  if (suggestion.status === 'VETOED' && suggestion.vetoedBy) {
    return `${base} · veto de ${suggestion.vetoedBy}`
  }
  return base
}
</script>

<template>
  <BaseCard>
    <div class="flex items-center gap-2">
      <UiIcon name="trash-2" class="size-5 text-berry" />
      <h2 class="font-display text-xl font-bold">
        Propositions de suppression
      </h2>
    </div>
    <p class="mt-1 text-sm text-ink-soft">
      Un média qui n'a plus sa place ? Propose sa suppression : tout le monde
      est prévenu sur Matrix et peut y opposer un veto en réagissant ❌. Sans
      veto, il est supprimé après le délai de grâce.
    </p>

    <div v-if="isPending" class="py-6">
      <BaseSpinner />
    </div>

    <p v-else-if="isError" class="mt-4 text-sm font-bold text-berry">
      Impossible de charger les suggestions. Réessaie dans un instant.
    </p>

    <ul v-else-if="suggestions?.length" class="mt-4 flex flex-col gap-2">
      <li
        v-for="suggestion in suggestions"
        :key="suggestion.id"
        class="flex items-center gap-3 rounded-card border-[1.5px] border-line bg-white p-3"
      >
        <img
          v-if="suggestion.posterUrl"
          :src="suggestion.posterUrl"
          alt=""
          class="h-14 w-10 shrink-0 rounded-md object-cover"
        />
        <span
          v-else
          class="flex h-14 w-10 shrink-0 items-center justify-center rounded-md bg-cream text-mute"
        >
          <UiIcon
            :name="suggestion.mediaKind === 'MOVIE' ? 'film' : 'tv'"
            class="size-5"
          />
        </span>
        <div class="min-w-0 flex-1">
          <p class="truncate font-display font-bold">
            {{ suggestion.displayTitle
            }}<template v-if="suggestion.year"> ({{ suggestion.year }})</template>
          </p>
          <p class="text-xs text-mute">
            {{ suggestionByline(suggestion) }}
          </p>
        </div>
        <span
          v-if="suggestion.status === 'PENDING'"
          class="shrink-0 text-xs font-bold text-amber"
        >
          {{ deadlineLabel(suggestion) }}
        </span>
        <BaseBadge
          v-else
          :color="suggestionStatusPresentation(suggestion.status).color"
        >
          {{ suggestionStatusPresentation(suggestion.status).label }}
        </BaseBadge>
      </li>
    </ul>

    <p v-else class="mt-4 text-sm text-mute">
      Aucune suggestion en attente. Un média inutile encombre le disque ?
      Cherche-le ci-dessous.
    </p>

    <div class="mt-6 border-t-[1.5px] border-line pt-5">
      <BaseInput
        v-model="searchInput"
        label="Proposer une suppression"
        placeholder="Dune, Severance…"
      />

      <p v-if="suggestError" class="mt-3 text-sm font-bold text-berry">
        {{ suggestError }}
      </p>

      <BaseSpinner v-if="isSearching" />

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
            class="flex w-full cursor-pointer items-center gap-3 rounded-card border-[1.5px] border-line bg-white p-3 text-left transition-colors hover:border-berry disabled:cursor-not-allowed disabled:opacity-50"
            :disabled="isCreating"
            @click="suggest(media)"
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
            <UiIcon name="trash-2" class="size-4 shrink-0 text-mute" />
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
              Tu proposes de supprimer quoi exactement ?
            </p>
            <div class="flex flex-wrap gap-2">
              <button
                type="button"
                class="cursor-pointer rounded-full bg-ink px-3.5 py-1.5 text-xs font-bold text-cream transition-opacity hover:opacity-85 disabled:opacity-40"
                :disabled="isCreating"
                @click="suggestSeries(media, null)"
              >
                Toute la série
              </button>
              <button
                v-for="season in media.seasonNumbers"
                :key="season"
                type="button"
                class="cursor-pointer rounded-full bg-line/60 px-3.5 py-1.5 text-xs font-bold text-ink-soft transition-colors hover:bg-line disabled:opacity-40"
                :disabled="isCreating"
                @click="suggestSeries(media, season)"
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
    </div>
  </BaseCard>
</template>
