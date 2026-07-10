<script setup lang="ts">
import { computed } from 'vue'
import type { TopUserDto } from '../../api/model'
import { formatWatchTime, type TopUsersMetric } from '../../lib/statistics'

const props = defineProps<{ users: TopUserDto[]; metric: TopUsersMetric }>()

// Le backend trie par temps de visionnage : on re-trie selon la métrique choisie
const rankedUsers = computed(() =>
  [...props.users].sort((a, b) =>
    props.metric === 'plays'
      ? b.playCount - a.playCount
      : b.watchTimeSeconds - a.watchTimeSeconds,
  ),
)

const podium = computed(() => rankedUsers.value.slice(0, 3))
const rest = computed(() => rankedUsers.value.slice(3))

const medals = ['🥇', '🥈', '🥉']
// Ordre d'affichage du podium : 2e, 1er, 3e
const podiumOrder = computed(() => {
  const [first, second, third] = podium.value
  return [second, first, third].filter(
    (user): user is TopUserDto => user != null,
  )
})

function rank(user: TopUserDto): number {
  return podium.value.indexOf(user)
}

function primaryValue(user: TopUserDto): string {
  return props.metric === 'plays'
    ? `${user.playCount} visionnages`
    : formatWatchTime(user.watchTimeSeconds)
}

function secondaryValue(user: TopUserDto): string {
  return props.metric === 'plays'
    ? formatWatchTime(user.watchTimeSeconds)
    : `${user.itemsWatched} médias`
}
</script>

<template>
  <div class="flex flex-col gap-5">
    <div class="flex items-end justify-center gap-3">
      <div
        v-for="user in podiumOrder"
        :key="user.userName"
        class="flex w-32 flex-col items-center gap-1 rounded-card border-[1.5px] border-line bg-paper px-3 pb-3 text-center"
        :class="rank(user) === 0 ? 'pt-5' : 'pt-3'"
      >
        <span class="text-3xl" :class="{ 'text-4xl': rank(user) === 0 }">
          {{ medals[rank(user)] }}
        </span>
        <span class="w-full truncate font-display font-bold">
          {{ user.userName }}
        </span>
        <span class="text-sm font-bold text-amber-deep">
          {{ primaryValue(user) }}
        </span>
        <span class="text-xs text-mute">{{ secondaryValue(user) }}</span>
      </div>
    </div>

    <ol v-if="rest.length" class="flex flex-col divide-y divide-line text-sm">
      <li
        v-for="(user, index) in rest"
        :key="user.userName"
        class="flex items-center gap-3 py-2"
      >
        <span class="w-6 shrink-0 text-right font-bold text-mute">
          {{ index + 4 }}
        </span>
        <span class="min-w-0 flex-1 truncate font-bold">
          {{ user.userName }}
        </span>
        <span class="shrink-0 text-ink-soft">
          {{ primaryValue(user) }}
        </span>
        <span class="w-24 shrink-0 text-right text-xs text-mute">
          {{ secondaryValue(user) }}
        </span>
      </li>
    </ol>
  </div>
</template>
