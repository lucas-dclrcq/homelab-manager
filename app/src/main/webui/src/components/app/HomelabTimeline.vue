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

const {
  data,
  isPending,
  isError,
  fetchNextPage,
  hasNextPage,
  isFetchingNextPage,
} = useInfiniteQuery({
  queryKey: ['timeline'],
  queryFn: ({ pageParam }) =>
    getApiTimeline({ page: pageParam, pageSize: PAGE_SIZE }),
  initialPageParam: 0,
  getNextPageParam: (lastPage) =>
    lastPage.page + 1 < lastPage.totalPages ? lastPage.page + 1 : undefined,
})

const events = computed<TimelineEventDto[]>(
  () => data.value?.pages.flatMap((page) => page.items) ?? [],
)

type Tone = 'amber' | 'dusk' | 'sage' | 'sky' | 'berry' | 'neutral'

const eventPresentation: Record<
  string,
  { icon: string; tone: Tone; label: string }
> = {
  movie_downloaded: { icon: 'film', tone: 'amber', label: 'Film téléchargé' },
  episode_downloaded: { icon: 'tv', tone: 'sky', label: 'Épisode téléchargé' },
  subtitles_downloaded: {
    icon: 'message-square',
    tone: 'dusk',
    label: 'Sous-titres téléchargés',
  },
  album_downloaded: { icon: 'music', tone: 'sage', label: 'Album téléchargé' },
}

function presentationFor(event: TimelineEventDto) {
  return (
    eventPresentation[event.eventType] ?? {
      icon: 'package',
      tone: 'neutral' as Tone,
      label: event.eventType,
    }
  )
}

const toneText: Record<Tone, string> = {
  amber: 'text-amber-deep',
  dusk: 'text-dusk',
  sage: 'text-sage',
  sky: 'text-sky',
  berry: 'text-berry',
  neutral: 'text-ink-soft',
}

function detailsFor(event: TimelineEventDto): string {
  const details = event.details ?? {}
  const parts: string[] = []
  if (details.seasonNumber && details.episodeNumber) {
    const season = String(details.seasonNumber).padStart(2, '0')
    const episode = String(details.episodeNumber).padStart(2, '0')
    parts.push(
      `S${season}E${episode}${details.episodeTitle ? ` – ${details.episodeTitle}` : ''}`,
    )
  }
  if (details.artist) parts.push(details.artist)
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
    <p
      v-else-if="isError"
      class="rounded-card border-[1.5px] border-berry/30 bg-berry-soft p-4 text-sm text-berry"
    >
      La timeline fait la sieste — on réessaie dans un instant ?
    </p>
    <template v-else>
      <p
        v-if="events.length === 0"
        class="rounded-tile border-[1.5px] border-line bg-paper p-6 text-sm text-ink-soft"
      >
        Tout est calme pour l'instant 🦉 — les téléchargements apparaîtront ici.
      </p>
      <BaseTimeline v-else>
        <BaseTimelineItem
          v-for="event in events"
          :key="event.id"
          :icon="presentationFor(event).icon"
          :tone="presentationFor(event).tone"
          :timestamp="formatDateTime(event.occurredAt)"
        >
          <span class="text-sm font-bold text-ink">{{ event.title }}</span>
          <span
            class="text-xs font-bold"
            :class="toneText[presentationFor(event).tone]"
          >
            {{ presentationFor(event).label }}
          </span>
          <template #details>{{ detailsFor(event) }}</template>
        </BaseTimelineItem>
      </BaseTimeline>

      <div v-if="hasNextPage" class="mt-6 flex justify-center">
        <BaseButton
          variant="ghost"
          :loading="isFetchingNextPage"
          @click="fetchNextPage()"
        >
          Voir plus loin dans la nuit
        </BaseButton>
      </div>
    </template>
  </section>
</template>
