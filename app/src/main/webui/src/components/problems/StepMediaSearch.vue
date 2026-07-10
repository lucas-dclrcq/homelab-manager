<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import {
  useGetApiProblemsMovies,
  useGetApiProblemsSeries,
} from '../../api/service/homelab'
import {
  useSelectMovieMutation,
  useSelectSeriesMutation,
} from '../../lib/problemsApi'
import BaseInput from '../ui/BaseInput.vue'
import BaseSpinner from '../ui/BaseSpinner.vue'
import UiIcon from '../ui/UiIcon.vue'

const props = withDefaults(
  defineProps<{ workflowId: string; mediaType: string; admin?: boolean }>(),
  { admin: false },
)

const isTv = computed(() => props.mediaType === 'tv')

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

const { data: movies, isFetching: isFetchingMovies } = useGetApiProblemsMovies(
  params,
  { query: { enabled: computed(() => searchEnabled.value && !isTv.value) } },
)
const { data: series, isFetching: isFetchingSeries } = useGetApiProblemsSeries(
  params,
  { query: { enabled: computed(() => searchEnabled.value && isTv.value) } },
)

const isFetching = computed(() =>
  isTv.value ? isFetchingSeries.value : isFetchingMovies.value,
)

const results = computed(() =>
  isTv.value
    ? (series.value ?? []).map((s) => ({
        id: s.sonarrSeriesId,
        title: s.title,
        year: s.year,
        posterUrl: s.posterUrl,
      }))
    : (movies.value ?? []).map((m) => ({
        id: m.radarrMovieId,
        title: m.title,
        year: m.year,
        posterUrl: m.posterUrl,
      })),
)

const { mutate: selectMovie, isPending: isSelectingMovie } =
  useSelectMovieMutation(props.admin)
const { mutate: selectSeries, isPending: isSelectingSeries } =
  useSelectSeriesMutation(props.admin)

const isSelecting = computed(
  () => isSelectingMovie.value || isSelectingSeries.value,
)

function select(mediaId: number) {
  if (isTv.value) {
    selectSeries({ id: props.workflowId, data: { sonarrSeriesId: mediaId } })
  } else {
    selectMovie({ id: props.workflowId, data: { radarrMovieId: mediaId } })
  }
}
</script>

<template>
  <div class="flex flex-col gap-4">
    <p class="text-sm text-ink-soft">
      {{
        isTv
          ? 'Quelle série te pose souci ? Cherche-la dans la bibliothèque.'
          : 'Quel film te pose souci ? Cherche-le dans la bibliothèque.'
      }}
    </p>

    <BaseInput
      v-model="searchInput"
      :label="isTv ? 'Titre de la série' : 'Titre du film'"
      :placeholder="isTv ? 'Severance, Dark…' : 'Dune, Oppenheimer…'"
    />

    <BaseSpinner v-if="isFetching" />

    <ul v-else-if="results.length" class="flex flex-col gap-2">
      <li v-for="media in results" :key="media.id">
        <button
          type="button"
          class="flex w-full cursor-pointer items-center gap-3 rounded-card border-[1.5px] border-line bg-white p-3 text-left transition-colors hover:border-amber disabled:cursor-not-allowed disabled:opacity-50"
          :disabled="isSelecting"
          @click="select(media.id)"
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
            <UiIcon :name="isTv ? 'tv' : 'film'" class="size-5" />
          </span>
          <span class="min-w-0 flex-1">
            <span class="block truncate font-display font-bold">
              {{ media.title }}
            </span>
            <span v-if="media.year" class="text-sm text-mute">
              {{ media.year }}
            </span>
          </span>
          <UiIcon name="chevron-right" class="size-4 shrink-0 text-mute" />
        </button>
      </li>
    </ul>

    <p
      v-else-if="searchEnabled"
      class="rounded-card border-[1.5px] border-dashed border-line bg-cream p-4 text-sm text-ink-soft"
    >
      {{
        isTv
          ? "Aucune série trouvée avec ce nom dans la bibliothèque. Vérifie l'orthographe, ou alors elle n'a pas encore été téléchargée."
          : "Aucun film trouvé avec ce nom dans la bibliothèque. Vérifie l'orthographe, ou alors il n'a pas encore été téléchargé."
      }}
    </p>
  </div>
</template>
