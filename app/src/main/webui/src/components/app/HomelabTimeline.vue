<script setup lang="ts">
import { computed } from 'vue'
import { useInfiniteQuery } from '@tanstack/vue-query'
import { getApiTimeline } from '../../api/service/homelab'
import type { TimelineEventDto } from '../../api/model'
import { formatDateTime } from '../../lib/format'
import BaseTimeline from '../ui/BaseTimeline.vue'
import BaseTimelineItem from '../ui/BaseTimelineItem.vue'
import BaseButton from '../ui/BaseButton.vue'
import BaseSpinner from '../ui/BaseSpinner.vue'

const PAGE_SIZE = 20

const { data, isPending, isError, fetchNextPage, hasNextPage, isFetchingNextPage } =
  useInfiniteQuery({
    queryKey: ['timeline'],
    queryFn: ({ pageParam }) => getApiTimeline({ page: pageParam, pageSize: PAGE_SIZE }),
    initialPageParam: 0,
    getNextPageParam: (lastPage) =>
      lastPage.page + 1 < lastPage.totalPages ? lastPage.page + 1 : undefined,
  })

const events = computed<TimelineEventDto[]>(() => data.value?.pages.flatMap((page) => page.items) ?? [])

const eventPresentation: Record<string, { icon: string; accent: string; label: string }> = {
  movie_downloaded: { icon: '🎬', accent: '#8b5cf6', label: 'Film téléchargé' },
  episode_downloaded: { icon: '📺', accent: '#06b6d4', label: 'Épisode téléchargé' },
  subtitles_downloaded: { icon: '💬', accent: '#f59e0b', label: 'Sous-titres téléchargés' },
}

function presentationFor(event: TimelineEventDto) {
  return eventPresentation[event.eventType] ?? { icon: '📦', accent: '#64748b', label: event.eventType }
}

function detailsFor(event: TimelineEventDto): string {
  const details = event.details ?? {}
  const parts: string[] = []
  if (details.seasonNumber && details.episodeNumber) {
    const season = String(details.seasonNumber).padStart(2, '0')
    const episode = String(details.episodeNumber).padStart(2, '0')
    parts.push(`S${season}E${episode}${details.episodeTitle ? ` – ${details.episodeTitle}` : ''}`)
  }
  if (details.language) parts.push(details.language)
  if (details.provider) parts.push(details.provider)
  if (details.episodeInfo) parts.push(details.episodeInfo)
  if (details.quality) parts.push(details.quality)
  return parts.join(' · ')
}
</script>

<template>
  <section aria-label="Activité récente">
    <BaseSpinner v-if="isPending" />
    <p v-else-if="isError" class="rounded-xl bg-rose-500/10 p-4 text-sm text-rose-300">
      Impossible de récupérer la timeline.
    </p>
    <template v-else>
      <p v-if="events.length === 0" class="rounded-xl bg-slate-900 p-6 text-sm text-slate-400">
        Rien à signaler pour l'instant — les téléchargements apparaîtront ici.
      </p>
      <BaseTimeline v-else>
        <BaseTimelineItem
          v-for="event in events"
          :key="event.id"
          :icon="presentationFor(event).icon"
          :accent="presentationFor(event).accent"
          :timestamp="formatDateTime(event.occurredAt)"
        >
          <span class="text-sm font-semibold text-slate-100">{{ event.title }}</span>
          <span
            class="text-xs font-medium"
            :style="{ color: presentationFor(event).accent }"
          >
            {{ presentationFor(event).label }}
          </span>
          <template #details>{{ detailsFor(event) }}</template>
        </BaseTimelineItem>
      </BaseTimeline>

      <div v-if="hasNextPage" class="mt-6 flex justify-center">
        <BaseButton variant="secondary" :loading="isFetchingNextPage" @click="fetchNextPage()">
          Charger plus
        </BaseButton>
      </div>
    </template>
  </section>
</template>
