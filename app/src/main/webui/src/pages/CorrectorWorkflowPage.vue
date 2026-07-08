<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useQueryClient } from '@tanstack/vue-query'
import {
  getGetApiCorrectorWorkflowsIdQueryKey,
  getGetApiCorrectorWorkflowsQueryKey,
  useGetApiCorrectorWorkflowsId,
  usePostApiCorrectorWorkflowsIdAbandon,
} from '../api/service/homelab'
import BaseButton from '../components/ui/BaseButton.vue'
import BaseModal from '../components/ui/BaseModal.vue'
import BaseSpinner from '../components/ui/BaseSpinner.vue'
import UiIcon from '../components/ui/UiIcon.vue'
import CorrectorStepper from '../components/corrector/CorrectorStepper.vue'
import StepMovieSearch from '../components/corrector/StepMovieSearch.vue'
import StepProblem from '../components/corrector/StepProblem.vue'
import StepReleases from '../components/corrector/StepReleases.vue'
import StepAwaiting from '../components/corrector/StepAwaiting.vue'
import { isActive, stepIndex } from '../lib/corrector'

const route = useRoute()
const router = useRouter()
const queryClient = useQueryClient()

const workflowId = computed(() => String(route.params.id))

const abandonOpen = ref(false)

const {
  data: workflow,
  isPending,
  isError,
} = useGetApiCorrectorWorkflowsId(workflowId, {
  query: {
    // Tant qu'on attend l'import Radarr, on vérifie régulièrement si c'est réglé
    refetchInterval: (query) =>
      query.state.data?.status === 'AWAITING_IMPORT' ? 30_000 : false,
  },
})

const { mutate: abandon, isPending: isAbandoning } =
  usePostApiCorrectorWorkflowsIdAbandon({
    mutation: {
      onSuccess: () => {
        queryClient.invalidateQueries({
          queryKey: getGetApiCorrectorWorkflowsIdQueryKey(workflowId.value),
        })
        queryClient.invalidateQueries({
          queryKey: getGetApiCorrectorWorkflowsQueryKey(),
        })
        abandonOpen.value = false
        router.push('/corrector')
      },
    },
  })
</script>

<template>
  <div class="flex max-w-3xl flex-col gap-6">
    <header class="flex flex-wrap items-center justify-between gap-4">
      <div class="flex items-center gap-3">
        <BaseButton variant="ghost" @click="router.push('/corrector')">
          Retour
        </BaseButton>
        <h1 class="roost font-display text-[28px] font-extrabold leading-tight">
          {{ workflow?.movie?.title ?? 'Réglons ça' }}
        </h1>
      </div>
      <BaseButton
        v-if="workflow && isActive(workflow)"
        variant="ghost"
        @click="abandonOpen = true"
      >
        <UiIcon name="trash-2" class="size-4" />
        Abandonner
      </BaseButton>
    </header>

    <BaseSpinner v-if="isPending" />

    <div
      v-else-if="isError || !workflow"
      class="rounded-tile border-[1.5px] border-line bg-paper p-8 text-center"
    >
      <p class="font-display text-lg font-bold">Workflow introuvable</p>
      <p class="mt-1 text-sm text-ink-soft">
        Il a peut-être été supprimé, ou ce n'est pas le tien.
      </p>
    </div>

    <template v-else>
      <div
        class="rounded-tile border-[1.5px] border-line bg-paper p-5 shadow-soft"
      >
        <CorrectorStepper :current-index="stepIndex(workflow)" />
      </div>

      <div
        class="rounded-tile border-[1.5px] border-line bg-paper p-6 shadow-soft"
      >
        <StepMovieSearch
          v-if="workflow.currentStep === 'SELECT_MOVIE'"
          :workflow-id="workflow.id"
        />
        <StepProblem
          v-else-if="workflow.currentStep === 'SELECT_PROBLEM'"
          :workflow="workflow"
        />
        <StepReleases
          v-else-if="workflow.currentStep === 'SELECT_RELEASE'"
          :workflow-id="workflow.id"
        />
        <StepAwaiting
          v-else-if="
            workflow.currentStep === 'AWAITING_IMPORT' ||
            workflow.currentStep === 'COMPLETED'
          "
          :workflow="workflow"
        />
        <p v-else class="text-sm text-ink-soft">
          Ce workflow a été abandonné. Tu peux en relancer un depuis la page El
          Corrector.
        </p>
      </div>
    </template>

    <BaseModal
      v-if="abandonOpen"
      title="Abandonner ce workflow ?"
      @close="abandonOpen = false"
    >
      <p class="mb-5 text-sm text-ink-soft">
        Le problème restera tel quel, mais tu pourras toujours relancer une
        résolution plus tard.
      </p>
      <div class="flex gap-3">
        <BaseButton
          variant="danger"
          :loading="isAbandoning"
          @click="abandon({ id: workflowId })"
        >
          <UiIcon name="trash-2" class="size-4" />
          Abandonner
        </BaseButton>
        <BaseButton variant="ghost" @click="abandonOpen = false">
          Annuler
        </BaseButton>
      </div>
    </BaseModal>
  </div>
</template>
