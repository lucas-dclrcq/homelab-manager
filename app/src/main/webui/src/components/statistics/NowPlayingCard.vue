<script setup lang="ts">
import { useGetApiStatisticsNowPlaying } from '../../api/service/homelab'
import BaseCard from '../ui/BaseCard.vue'
import BaseBadge from '../ui/BaseBadge.vue'
import UiIcon from '../ui/UiIcon.vue'
import {
  formatEpisodeRef,
  formatSince,
  platformLabel,
} from '../../lib/statistics'
import type { NowPlayingDto } from '../../api/model'

const { data: sessions } = useGetApiStatisticsNowPlaying({
  query: { refetchInterval: 15_000 },
})

function mediaTitle(session: NowPlayingDto): string {
  if (session.mediaType === 'EPISODE' && session.seriesName) {
    const episodeRef = formatEpisodeRef(
      session.seasonNumber,
      session.episodeNumber,
    )
    return [session.seriesName, episodeRef, `· ${session.itemName}`]
      .filter(Boolean)
      .join(' ')
  }
  return session.itemName
}
</script>

<template>
  <BaseCard>
    <div class="mb-4 flex items-center gap-2">
      <UiIcon name="play" class="size-5 text-amber-deep" />
      <h2 class="font-display text-xl font-bold">En cours de lecture</h2>
    </div>

    <p v-if="!sessions || sessions.length === 0" class="text-sm text-mute">
      Aucune lecture en cours. La chouette somnole.
    </p>

    <ul v-else class="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
      <li
        v-for="session in sessions"
        :key="`${session.userName}-${session.itemName}-${session.startedAt}`"
        class="flex flex-col gap-1.5 rounded-card border-[1.5px] border-line bg-paper px-3 py-2.5"
      >
        <div class="flex items-center justify-between gap-2 text-xs text-mute">
          <span class="truncate font-bold text-ink">{{
            session.userName
          }}</span>
          <span class="flex shrink-0 items-center gap-1.5">
            <BaseBadge v-if="session.paused">En pause</BaseBadge>
            <span>{{ platformLabel(session.platform) }}</span>
          </span>
        </div>
        <p class="flex items-start gap-1.5 text-sm leading-snug font-bold">
          <UiIcon
            :name="session.mediaType === 'EPISODE' ? 'tv' : 'film'"
            class="mt-0.5 size-4 shrink-0 text-amber-deep"
          />
          <span>{{ mediaTitle(session) }}</span>
        </p>
        <div class="flex items-center gap-2">
          <div
            v-if="session.progressPercent != null"
            class="h-1.5 min-w-0 flex-1 overflow-hidden rounded-full bg-line"
          >
            <div
              class="h-full rounded-full bg-amber transition-[width]"
              :style="{ width: `${Math.round(session.progressPercent)}%` }"
            />
          </div>
          <span class="shrink-0 text-xs text-mute">
            depuis {{ formatSince(session.startedAt) }}
          </span>
        </div>
      </li>
    </ul>
  </BaseCard>
</template>
