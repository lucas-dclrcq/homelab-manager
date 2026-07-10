<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useQueryClient } from '@tanstack/vue-query'
import type { AxiosError } from 'axios'
import {
  useGetApiAdminJobs,
  usePostApiAdminStatisticsImport,
} from '../../api/service/homelab'
import BaseCard from '../ui/BaseCard.vue'
import BaseButton from '../ui/BaseButton.vue'
import UiIcon from '../ui/UiIcon.vue'

const IMPORT_JOB_IDENTITY = 'jellystat-import'

const queryClient = useQueryClient()
const file = ref<File | null>(null)
const importStarted = ref(false)
const error = ref('')

const { mutate: startImport, isPending: uploading } =
  usePostApiAdminStatisticsImport<AxiosError>({
    mutation: {
      onSuccess: () => {
        importStarted.value = true
        error.value = ''
      },
      onError: (uploadError) => {
        error.value =
          uploadError.response?.status === 409
            ? 'Un import est déjà en cours.'
            : "L'upload du backup a échoué."
      },
    },
  })

// Suivi du job d'import : poll tant qu'un import vient d'être lancé
const { data: jobs } = useGetApiAdminJobs({
  query: {
    refetchInterval: () => (importStarted.value ? 3_000 : false),
  },
})

const importJob = computed(() =>
  jobs.value?.find((job) => job.identity === IMPORT_JOB_IDENTITY),
)

watch(
  () => importJob.value?.lastExecution?.runAt,
  (runAt, previousRunAt) => {
    if (!importStarted.value || !runAt || runAt === previousRunAt) return
    importStarted.value = false
    // L'import vient de se terminer : rafraîchir toutes les stats affichées
    queryClient.invalidateQueries({ queryKey: ['api', 'statistics'] })
  },
)

function onFileChange(event: Event) {
  file.value = (event.target as HTMLInputElement).files?.[0] ?? null
}

function upload() {
  if (!file.value) return
  startImport({ data: { file: file.value } })
}
</script>

<template>
  <BaseCard>
    <div class="mb-2 flex items-center gap-2">
      <UiIcon name="upload" class="size-5 text-amber-deep" />
      <h2 class="font-display text-xl font-bold">Import backup Jellystat</h2>
    </div>
    <p class="mb-4 text-sm text-ink-soft">
      Rapatrie l'historique de visionnage depuis un fichier de backup Jellystat.
      L'import tourne en arrière-plan et peut se rejouer sans créer de doublons.
    </p>

    <div class="flex flex-wrap items-center gap-3">
      <input
        type="file"
        accept="application/json,.json"
        class="text-sm text-ink-soft file:mr-3 file:cursor-pointer file:rounded-xl file:border-[1.5px] file:border-line file:bg-white file:px-4 file:py-2 file:text-sm file:font-bold file:text-ink"
        @change="onFileChange"
      />
      <BaseButton :disabled="!file" :loading="uploading" @click="upload">
        Lancer l'import
      </BaseButton>
    </div>

    <p v-if="error" class="mt-3 text-sm font-bold text-berry">{{ error }}</p>
    <p v-else-if="importStarted" class="mt-3 text-sm text-ink-soft">
      Import lancé, ça mouline en arrière-plan…
    </p>
    <p
      v-else-if="importJob?.lastExecution"
      class="mt-3 text-sm"
      :class="
        importJob.lastExecution.status === 'SUCCESS'
          ? 'text-sage'
          : 'text-berry'
      "
    >
      Dernier import : {{ importJob.lastExecution.status }}
      <span v-if="importJob.lastExecution.error" class="text-berry">
        — {{ importJob.lastExecution.error }}
      </span>
    </p>
  </BaseCard>
</template>
