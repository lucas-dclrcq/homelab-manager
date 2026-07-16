<script setup lang="ts">
import { ref } from 'vue'
import { useQueryClient } from '@tanstack/vue-query'
import {
  useDeleteApiAdminProblemsWorkflowsId,
  usePostApiAdminProblemsWorkflowsIdResolve,
} from '../api/service/homelab'
import type { AdminProblemWorkflowDto } from '../api/model'
import ProblemsAdminTable from '../components/problems/ProblemsAdminTable.vue'
import BaseModal from '../components/ui/BaseModal.vue'
import BaseButton from '../components/ui/BaseButton.vue'
import UiIcon from '../components/ui/UiIcon.vue'
import { invalidateWorkflows } from '../lib/problemsApi'

const queryClient = useQueryClient()

const resolving = ref<AdminProblemWorkflowDto | null>(null)
const deleting = ref<AdminProblemWorkflowDto | null>(null)

const { mutate: resolveWorkflow, isPending: isResolving } =
  usePostApiAdminProblemsWorkflowsIdResolve({
    mutation: {
      onSuccess: () => {
        invalidateWorkflows(queryClient, resolving.value?.workflow.id)
        resolving.value = null
      },
    },
  })

const { mutate: deleteWorkflow, isPending: isDeleting } =
  useDeleteApiAdminProblemsWorkflowsId({
    mutation: {
      onSuccess: () => {
        invalidateWorkflows(queryClient, deleting.value?.workflow.id)
        deleting.value = null
      },
    },
  })
</script>

<template>
  <div class="flex max-w-5xl flex-col gap-8">
    <ProblemsAdminTable
      @resolve="resolving = $event"
      @delete="deleting = $event"
    />

    <BaseModal
      v-if="resolving"
      :title="`Marquer « ${resolving.workflow.media?.title ?? 'ce problème'} » comme résolu ?`"
      @close="resolving = null"
    >
      <p class="mb-5 text-sm text-ink-soft">
        Le problème passera en « Résolu » pour {{ resolving.username }}.
      </p>
      <div class="flex gap-3">
        <BaseButton
          :loading="isResolving"
          @click="resolveWorkflow({ id: resolving.workflow.id })"
        >
          <UiIcon name="check" class="size-4" />
          Marquer résolu
        </BaseButton>
        <BaseButton variant="ghost" @click="resolving = null"
          >Annuler</BaseButton
        >
      </div>
    </BaseModal>

    <BaseModal
      v-if="deleting"
      :title="`Supprimer « ${deleting.workflow.media?.title ?? 'ce problème'} » ?`"
      @close="deleting = null"
    >
      <p class="mb-5 text-sm text-ink-soft">
        Le problème de {{ deleting.username }} disparaîtra définitivement. Pas
        de retour en arrière possible.
      </p>
      <div class="flex gap-3">
        <BaseButton
          variant="danger"
          :loading="isDeleting"
          @click="deleteWorkflow({ id: deleting.workflow.id })"
        >
          <UiIcon name="trash-2" class="size-4" />
          Supprimer
        </BaseButton>
        <BaseButton variant="ghost" @click="deleting = null"
          >Annuler</BaseButton
        >
      </div>
    </BaseModal>
  </div>
</template>
