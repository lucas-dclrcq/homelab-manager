<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useQueryClient } from '@tanstack/vue-query'
import {
  getGetApiCorrectorWorkflowsQueryKey,
  useGetApiCorrectorWorkflows,
  usePostApiCorrectorWorkflows,
} from '../api/service/homelab'
import BaseBadge from '../components/ui/BaseBadge.vue'
import BaseButton from '../components/ui/BaseButton.vue'
import BaseModal from '../components/ui/BaseModal.vue'
import BaseSpinner from '../components/ui/BaseSpinner.vue'
import UiIcon from '../components/ui/UiIcon.vue'
import WorkflowCard from '../components/corrector/WorkflowCard.vue'
import { isActive } from '../lib/corrector'

const router = useRouter()
const queryClient = useQueryClient()

const chooseMediaOpen = ref(false)

const { data: workflows, isPending } = useGetApiCorrectorWorkflows()

const activeWorkflows = computed(
  () => workflows.value?.filter((w) => isActive(w)) ?? [],
)
const pastWorkflows = computed(
  () => workflows.value?.filter((w) => !isActive(w)) ?? [],
)

const { mutate: createWorkflow, isPending: isCreating } =
  usePostApiCorrectorWorkflows({
    mutation: {
      onSuccess: (workflow) => {
        queryClient.invalidateQueries({
          queryKey: getGetApiCorrectorWorkflowsQueryKey(),
        })
        chooseMediaOpen.value = false
        router.push(`/corrector/${workflow.id}`)
      },
    },
  })
</script>

<template>
  <div class="flex max-w-5xl flex-col gap-8">
    <header class="flex flex-wrap items-end justify-between gap-4">
      <div>
        <h1 class="roost font-display text-[34px] font-extrabold leading-tight">
          El Corrector
        </h1>
        <p class="mt-2 text-ink-soft">
          Un film en VO qui devrait parler français ? On règle ça ensemble, pas
          besoin d'appeler un adulte.
        </p>
      </div>
      <BaseButton @click="chooseMediaOpen = true">
        <UiIcon name="wrench" class="size-4" />
        Régler un problème
      </BaseButton>
    </header>

    <BaseSpinner v-if="isPending" />

    <template v-else>
      <section v-if="activeWorkflows.length" class="flex flex-col gap-3">
        <h2 class="font-display text-xl font-bold">En cours</h2>
        <WorkflowCard
          v-for="workflow in activeWorkflows"
          :key="workflow.id"
          :workflow="workflow"
        />
      </section>

      <section v-if="pastWorkflows.length" class="flex flex-col gap-3">
        <h2 class="font-display text-xl font-bold">Réglés (ou presque)</h2>
        <WorkflowCard
          v-for="workflow in pastWorkflows"
          :key="workflow.id"
          :workflow="workflow"
        />
      </section>

      <div
        v-if="!activeWorkflows.length && !pastWorkflows.length"
        class="rounded-tile border-[1.5px] border-dashed border-line bg-paper p-10 text-center"
      >
        <p class="font-display text-lg font-bold">Rien à signaler !</p>
        <p class="mt-1 text-sm text-ink-soft">
          Quand quelque chose cloche avec un film, clique sur « Régler un
          problème » et laisse-toi guider.
        </p>
      </div>
    </template>

    <BaseModal
      v-if="chooseMediaOpen"
      title="C'est quoi le souci ?"
      @close="chooseMediaOpen = false"
    >
      <p class="mb-4 text-sm text-ink-soft">
        Ton problème concerne un film ou une série ?
      </p>
      <div class="grid grid-cols-2 gap-3">
        <button
          type="button"
          class="flex cursor-pointer flex-col items-center gap-2 rounded-tile border-[1.5px] border-line bg-white p-6 transition-colors hover:border-amber disabled:cursor-not-allowed"
          :disabled="isCreating"
          @click="createWorkflow({ data: { mediaType: 'movie' } })"
        >
          <UiIcon name="film" class="size-8 text-amber-deep" />
          <span class="font-display font-bold">Un film</span>
        </button>
        <div
          class="flex flex-col items-center gap-2 rounded-tile border-[1.5px] border-line bg-cream p-6 opacity-60"
        >
          <UiIcon name="tv" class="size-8 text-mute" />
          <span class="font-display font-bold text-mute">Une série</span>
          <BaseBadge color="dusk">Bientôt</BaseBadge>
        </div>
      </div>
    </BaseModal>
  </div>
</template>
