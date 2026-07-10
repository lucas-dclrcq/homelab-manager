<script setup lang="ts">
import { computed, ref } from 'vue'
import type { ProblemWorkflowDto } from '../../api/model'
import { useSelectProblemMutation } from '../../lib/problemsApi'
import UiIcon from '../ui/UiIcon.vue'
import BaseBadge from '../ui/BaseBadge.vue'
import BaseButton from '../ui/BaseButton.vue'
import BaseTextarea from '../ui/BaseTextarea.vue'

const props = withDefaults(
  defineProps<{ workflow: ProblemWorkflowDto; admin?: boolean }>(),
  { admin: false },
)

const isTv = computed(() => props.workflow.mediaType === 'tv')

const otherOpen = ref(false)
const description = ref('')

const { mutate: selectProblem, isPending } = useSelectProblemMutation(
  props.admin,
)

function reportOther() {
  if (!description.value.trim()) return
  selectProblem({
    id: props.workflow.id,
    data: { problemType: 'other', description: description.value.trim() },
  })
}
</script>

<template>
  <div class="flex flex-col gap-5">
    <div
      class="flex items-center gap-4 rounded-card border-[1.5px] border-line bg-cream p-4"
    >
      <img
        v-if="workflow.media?.posterUrl"
        :src="workflow.media.posterUrl"
        alt=""
        class="h-20 w-14 shrink-0 rounded-lg object-cover"
      />
      <div class="min-w-0">
        <p class="font-display text-[17px] font-bold">
          {{ workflow.media?.title }}
          <span v-if="workflow.media?.year" class="font-normal text-mute">
            ({{ workflow.media.year }})
          </span>
        </p>
        <p class="mt-1 flex flex-wrap items-center gap-2 text-sm">
          <BaseBadge v-if="workflow.media?.currentQuality" color="sky">
            {{ workflow.media.currentQuality }}
          </BaseBadge>
          <BaseBadge
            v-for="language in workflow.media?.currentLanguages ?? []"
            :key="language"
            color="neutral"
          >
            {{ language }}
          </BaseBadge>
        </p>
      </div>
    </div>

    <p class="text-sm text-ink-soft">
      Et c'est quoi le problème avec {{ isTv ? 'elle' : 'lui' }} ?
    </p>

    <button
      v-if="!isTv"
      type="button"
      class="flex cursor-pointer items-center gap-3 rounded-card border-[1.5px] border-line bg-white p-4 text-left transition-colors hover:border-amber disabled:cursor-not-allowed disabled:opacity-50"
      :disabled="isPending"
      @click="
        selectProblem({
          id: workflow.id,
          data: { problemType: 'vo_should_be_french', description: null },
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
      class="rounded-card border-[1.5px] bg-white transition-colors"
      :class="otherOpen ? 'border-amber' : 'border-line'"
    >
      <button
        type="button"
        class="flex w-full cursor-pointer items-center gap-3 p-4 text-left"
        @click="otherOpen = !otherOpen"
      >
        <UiIcon name="message-square" class="size-6 shrink-0 text-amber-deep" />
        <span class="flex-1">
          <span class="block font-display font-bold">Autre problème</span>
          <span class="text-sm text-ink-soft">
            Décris ce qui cloche, quelqu'un s'en occupera.
          </span>
        </span>
        <UiIcon
          name="chevron-right"
          class="size-4 shrink-0 text-mute transition-transform"
          :class="{ 'rotate-90': otherOpen }"
        />
      </button>
      <div v-if="otherOpen" class="flex flex-col gap-3 px-4 pb-4">
        <BaseTextarea
          v-model="description"
          label="C'est quoi le souci ?"
          placeholder="Le son se désynchronise après 20 minutes, il manque l'épisode 3…"
          :rows="4"
        />
        <div>
          <BaseButton
            :loading="isPending"
            :disabled="!description.trim()"
            @click="reportOther"
          >
            <UiIcon name="circle-alert" class="size-4" />
            Signaler
          </BaseButton>
        </div>
      </div>
    </div>
  </div>
</template>
