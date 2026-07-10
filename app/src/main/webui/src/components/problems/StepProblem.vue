<script setup lang="ts">
import { useQueryClient } from '@tanstack/vue-query'
import type { CorrectorWorkflowDto } from '../../api/model'
import {
  getGetApiCorrectorWorkflowsIdQueryKey,
  usePostApiCorrectorWorkflowsIdProblem,
} from '../../api/service/homelab'
import UiIcon from '../ui/UiIcon.vue'
import BaseBadge from '../ui/BaseBadge.vue'

const props = defineProps<{ workflow: CorrectorWorkflowDto }>()

const queryClient = useQueryClient()

const { mutate: selectProblem, isPending } =
  usePostApiCorrectorWorkflowsIdProblem({
    mutation: {
      onSuccess: () => {
        queryClient.invalidateQueries({
          queryKey: getGetApiCorrectorWorkflowsIdQueryKey(props.workflow.id),
        })
      },
    },
  })
</script>

<template>
  <div class="flex flex-col gap-5">
    <div
      class="flex items-center gap-4 rounded-card border-[1.5px] border-line bg-cream p-4"
    >
      <img
        v-if="workflow.movie?.posterUrl"
        :src="workflow.movie.posterUrl"
        alt=""
        class="h-20 w-14 shrink-0 rounded-lg object-cover"
      />
      <div class="min-w-0">
        <p class="font-display text-[17px] font-bold">
          {{ workflow.movie?.title }}
          <span v-if="workflow.movie?.year" class="font-normal text-mute">
            ({{ workflow.movie.year }})
          </span>
        </p>
        <p class="mt-1 flex flex-wrap items-center gap-2 text-sm">
          <BaseBadge v-if="workflow.movie?.currentQuality" color="sky">
            {{ workflow.movie.currentQuality }}
          </BaseBadge>
          <BaseBadge
            v-for="language in workflow.movie?.currentLanguages ?? []"
            :key="language"
            color="neutral"
          >
            {{ language }}
          </BaseBadge>
        </p>
      </div>
    </div>

    <p class="text-sm text-ink-soft">Et c'est quoi le problème avec lui ?</p>

    <button
      type="button"
      class="flex cursor-pointer items-center gap-3 rounded-card border-[1.5px] border-line bg-white p-4 text-left transition-colors hover:border-amber disabled:cursor-not-allowed disabled:opacity-50"
      :disabled="isPending"
      @click="
        selectProblem({
          id: workflow.id,
          data: { problemType: 'vo_should_be_french' },
        })
      "
    >
      <UiIcon name="circle-alert" class="size-6 shrink-0 text-amber-deep" />
      <span class="flex-1">
        <span class="block font-display font-bold">
          Il est en VO, je le voulais en VF/MULTI
        </span>
        <span class="text-sm text-ink-soft">
          On va chercher une version doublée en français et remplacer le
          fichier.
        </span>
      </span>
      <UiIcon name="chevron-right" class="size-4 shrink-0 text-mute" />
    </button>

    <div
      class="flex items-center gap-3 rounded-card border-[1.5px] border-line bg-cream p-4 opacity-60"
    >
      <UiIcon name="clock" class="size-6 shrink-0 text-mute" />
      <span class="flex-1">
        <span class="block font-display font-bold text-mute">
          Les sous-titres sont décalés
        </span>
        <span class="text-sm text-mute">La chouette y travaille.</span>
      </span>
      <BaseBadge color="dusk">Bientôt</BaseBadge>
    </div>
  </div>
</template>
