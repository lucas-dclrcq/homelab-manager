<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useQueryClient } from '@tanstack/vue-query'
import BaseButton from '../components/ui/BaseButton.vue'
import BaseModal from '../components/ui/BaseModal.vue'
import BaseSpinner from '../components/ui/BaseSpinner.vue'
import BaseBadge from '../components/ui/BaseBadge.vue'
import UiIcon from '../components/ui/UiIcon.vue'
import ProblemStepper from '../components/problems/ProblemStepper.vue'
import StepMediaSearch from '../components/problems/StepMediaSearch.vue'
import StepProblem from '../components/problems/StepProblem.vue'
import StepReleases from '../components/problems/StepReleases.vue'
import StepAwaiting from '../components/problems/StepAwaiting.vue'
import StepReported from '../components/problems/StepReported.vue'
import { isActive, stepIndex, wizardStepsFor } from '../lib/problems'
import {
  invalidateWorkflows,
  useAbandonMutation,
  useWorkflowQuery,
} from '../lib/problemsApi'

const route = useRoute()
const router = useRouter()
const queryClient = useQueryClient()

// Reprise d'un problème par un admin : même wizard, endpoints admin
const admin = computed(() => route.meta.requiresAdmin === true)
const backTo = computed(() => (admin.value ? '/admin/problems' : '/problems'))

const workflowId = computed(() => String(route.params.id))

const abandonOpen = ref(false)

const {
  data: workflow,
  isPending,
  isError,
} = useWorkflowQuery(admin.value, workflowId)

const steps = computed(() =>
  workflow.value ? wizardStepsFor(workflow.value) : [],
)

const { mutate: abandon, isPending: isAbandoning } = useAbandonMutation(
  admin.value,
  {
    onSuccess: () => {
      invalidateWorkflows(queryClient, workflowId.value)
      abandonOpen.value = false
      router.push(backTo.value)
    },
  },
)
</script>

<template>
  <div class="flex max-w-3xl flex-col gap-6">
    <header class="flex flex-wrap items-center justify-between gap-4">
      <div class="flex items-center gap-3">
        <BaseButton variant="ghost" @click="router.push(backTo)">
          Retour
        </BaseButton>
        <h1 class="roost font-display text-[28px] font-extrabold leading-tight">
          {{ workflow?.media?.title ?? 'Réglons ça' }}
        </h1>
        <BaseBadge v-if="admin && workflow?.reportedBy" color="dusk">
          Signalé par {{ workflow.reportedBy }}
        </BaseBadge>
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
      <p class="font-display text-lg font-bold">Problème introuvable</p>
      <p class="mt-1 text-sm text-ink-soft">
        Il a peut-être été supprimé, ou ce n'est pas le tien.
      </p>
    </div>

    <template v-else>
      <div
        v-if="steps.length"
        class="rounded-tile border-[1.5px] border-line bg-paper p-5 shadow-soft"
      >
        <ProblemStepper :steps="steps" :current-index="stepIndex(workflow)" />
      </div>

      <div
        class="rounded-tile border-[1.5px] border-line bg-paper p-6 shadow-soft"
      >
        <StepMediaSearch
          v-if="workflow.currentStep === 'SELECT_MEDIA'"
          :workflow-id="workflow.id"
          :media-type="workflow.mediaType"
          :admin="admin"
        />
        <StepProblem
          v-else-if="workflow.currentStep === 'SELECT_PROBLEM'"
          :workflow="workflow"
          :admin="admin"
        />
        <StepReleases
          v-else-if="workflow.currentStep === 'SELECT_RELEASE'"
          :workflow-id="workflow.id"
          :admin="admin"
        />
        <StepAwaiting
          v-else-if="
            workflow.currentStep === 'AWAITING_IMPORT' ||
            workflow.currentStep === 'COMPLETED'
          "
          :workflow="workflow"
        />
        <StepReported
          v-else-if="
            workflow.currentStep === 'REPORTED' ||
            workflow.currentStep === 'RESOLVED'
          "
          :workflow="workflow"
          :admin="admin"
        />
        <p v-else class="text-sm text-ink-soft">
          Ce problème a été abandonné. Tu peux en signaler un autre depuis la
          page Problèmes.
        </p>
      </div>
    </template>

    <BaseModal
      v-if="abandonOpen"
      title="Abandonner ce problème ?"
      @close="abandonOpen = false"
    >
      <p class="mb-5 text-sm text-ink-soft">
        Le problème restera tel quel, mais tu pourras toujours en relancer une
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
