<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useQueryClient } from '@tanstack/vue-query'
import {
  getGetApiCorrectorWorkflowsIdQueryKey,
  useGetApiCorrectorMovies,
  usePostApiCorrectorWorkflowsIdMovie,
} from '../../api/service/homelab'
import BaseInput from '../ui/BaseInput.vue'
import BaseSpinner from '../ui/BaseSpinner.vue'
import UiIcon from '../ui/UiIcon.vue'

const props = defineProps<{ workflowId: string }>()

const queryClient = useQueryClient()

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

const { data: movies, isFetching } = useGetApiCorrectorMovies(
  computed(() => ({ query: debouncedQuery.value })),
  { query: { enabled: searchEnabled } },
)

const { mutate: selectMovie, isPending: isSelecting } =
  usePostApiCorrectorWorkflowsIdMovie({
    mutation: {
      onSuccess: () => {
        queryClient.invalidateQueries({
          queryKey: getGetApiCorrectorWorkflowsIdQueryKey(props.workflowId),
        })
      },
    },
  })
</script>

<template>
  <div class="flex flex-col gap-4">
    <p class="text-sm text-ink-soft">
      Quel film te pose souci ? Cherche-le dans la bibliothèque de la maison.
    </p>

    <BaseInput
      v-model="searchInput"
      label="Titre du film"
      placeholder="Dune, Oppenheimer…"
    />

    <BaseSpinner v-if="isFetching" />

    <ul v-else-if="movies?.length" class="flex flex-col gap-2">
      <li v-for="movie in movies" :key="movie.radarrMovieId">
        <button
          type="button"
          class="flex w-full cursor-pointer items-center gap-3 rounded-card border-[1.5px] border-line bg-white p-3 text-left transition-colors hover:border-amber disabled:cursor-not-allowed disabled:opacity-50"
          :disabled="isSelecting"
          @click="
            selectMovie({
              id: workflowId,
              data: { radarrMovieId: movie.radarrMovieId },
            })
          "
        >
          <img
            v-if="movie.posterUrl"
            :src="movie.posterUrl"
            alt=""
            class="h-14 w-10 shrink-0 rounded-md object-cover"
          />
          <span
            v-else
            class="flex h-14 w-10 shrink-0 items-center justify-center rounded-md bg-cream text-mute"
          >
            <UiIcon name="film" class="size-5" />
          </span>
          <span class="min-w-0 flex-1">
            <span class="block truncate font-display font-bold">
              {{ movie.title }}
            </span>
            <span v-if="movie.year" class="text-sm text-mute">
              {{ movie.year }}
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
      Aucun film trouvé avec ce nom dans la bibliothèque. Vérifie l'orthographe,
      ou alors il n'a pas encore été téléchargé.
    </p>
  </div>
</template>
