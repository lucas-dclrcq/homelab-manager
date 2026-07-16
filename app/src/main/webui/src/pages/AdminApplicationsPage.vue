<script setup lang="ts">
import { ref } from 'vue'
import { useQueryClient } from '@tanstack/vue-query'
import {
  getGetApiApplicationsQueryKey,
  useDeleteApiApplicationsId,
} from '../api/service/homelab'
import type { ApplicationDto } from '../api/model'
import ApplicationsAdminTable from '../components/app/ApplicationsAdminTable.vue'
import ApplicationForm from '../components/app/ApplicationForm.vue'
import BaseModal from '../components/ui/BaseModal.vue'
import BaseButton from '../components/ui/BaseButton.vue'
import UiIcon from '../components/ui/UiIcon.vue'

const queryClient = useQueryClient()

const formOpen = ref(false)
const editing = ref<ApplicationDto | null>(null)
const deleting = ref<ApplicationDto | null>(null)

function openCreate() {
  editing.value = null
  formOpen.value = true
}

function openEdit(application: ApplicationDto) {
  editing.value = application
  formOpen.value = true
}

const { mutate: deleteApplication, isPending: isDeleting } =
  useDeleteApiApplicationsId({
    mutation: {
      onSuccess: () => {
        queryClient.invalidateQueries({
          queryKey: getGetApiApplicationsQueryKey(),
        })
        deleting.value = null
      },
    },
  })
</script>

<template>
  <div class="flex max-w-5xl flex-col gap-8">
    <header class="flex flex-wrap items-end justify-end gap-4">
      <BaseButton @click="openCreate">
        <UiIcon name="plus" class="size-4" />
        Ajouter une appli
      </BaseButton>
    </header>

    <ApplicationsAdminTable @edit="openEdit" @delete="deleting = $event" />

    <BaseModal
      v-if="formOpen"
      :title="editing ? `Modifier « ${editing.name} »` : 'Ajouter une appli'"
      @close="formOpen = false"
    >
      <ApplicationForm :application="editing" @saved="formOpen = false" />
    </BaseModal>

    <BaseModal
      v-if="deleting"
      :title="`Supprimer « ${deleting.name} » ?`"
      @close="deleting = null"
    >
      <p class="mb-5 text-sm text-ink-soft">
        Elle disparaîtra du catalogue. Pas de retour en arrière possible.
      </p>
      <div class="flex gap-3">
        <BaseButton
          variant="danger"
          :loading="isDeleting"
          @click="deleteApplication({ id: deleting.id })"
        >
          <UiIcon name="trash-2" class="size-4" />
          Supprimer
        </BaseButton>
        <BaseButton variant="ghost" @click="deleting = null">
          Annuler
        </BaseButton>
      </div>
    </BaseModal>
  </div>
</template>
