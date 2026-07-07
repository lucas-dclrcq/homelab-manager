<script setup lang="ts">
import { ref } from 'vue'
import { useQueryClient } from '@tanstack/vue-query'
import {
  getGetApiAdminJobsQueryKey,
  useGetApiAdminJobs,
  usePostApiAdminJobsIdentityPause,
  usePostApiAdminJobsIdentityResume,
  usePostApiAdminJobsIdentityRun,
} from '../../api/service/homelab'
import type { JobStatusDto } from '../../api/model'
import { formatDateTime } from '../../lib/format'
import BaseCard from '../ui/BaseCard.vue'
import BaseBadge from '../ui/BaseBadge.vue'
import BaseButton from '../ui/BaseButton.vue'
import BaseSpinner from '../ui/BaseSpinner.vue'

const queryClient = useQueryClient()

const { data: jobs, isPending, isError } = useGetApiAdminJobs({
  query: { refetchInterval: 30_000 },
})

const runningIdentity = ref<string | null>(null)
const togglingIdentity = ref<string | null>(null)

function refresh() {
  queryClient.invalidateQueries({ queryKey: getGetApiAdminJobsQueryKey() })
}

const { mutate: runJob } = usePostApiAdminJobsIdentityRun({
  mutation: {
    onSettled: () => {
      runningIdentity.value = null
      refresh()
    },
  },
})

const { mutate: pauseJob } = usePostApiAdminJobsIdentityPause({
  mutation: {
    onSettled: () => {
      togglingIdentity.value = null
      refresh()
    },
  },
})

const { mutate: resumeJob } = usePostApiAdminJobsIdentityResume({
  mutation: {
    onSettled: () => {
      togglingIdentity.value = null
      refresh()
    },
  },
})

function run(job: JobStatusDto) {
  runningIdentity.value = job.identity
  runJob({ identity: job.identity })
}

function togglePause(job: JobStatusDto) {
  togglingIdentity.value = job.identity
  if (job.paused) {
    resumeJob({ identity: job.identity })
  } else {
    pauseJob({ identity: job.identity })
  }
}
</script>

<template>
  <BaseCard accent="#5d84a6">
    <h2 class="font-display mb-1 text-2xl font-bold text-stone-800">Tâches planifiées</h2>
    <p class="mb-5 text-sm text-stone-500">
      État des synchronisations et autres tâches récurrentes du portail.
    </p>

    <BaseSpinner v-if="isPending" />
    <p
      v-else-if="isError"
      class="sketchy-sm border-2 border-dashed border-rose-300 bg-rose-50 p-4 text-sm text-rose-700"
    >
      Impossible de récupérer l'état des tâches.
    </p>

    <div v-else-if="jobs" class="overflow-x-auto">
      <table class="w-full text-left text-sm">
        <thead>
          <tr class="border-b-2 border-dashed border-stone-300 text-xs uppercase tracking-wide text-stone-500">
            <th class="py-2 pr-4 font-semibold">Tâche</th>
            <th class="py-2 pr-4 font-semibold">Planification</th>
            <th class="py-2 pr-4 font-semibold">Prochaine exécution</th>
            <th class="py-2 pr-4 font-semibold">Dernière exécution</th>
            <th class="py-2 font-semibold">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="job in jobs"
            :key="job.identity"
            class="border-b border-dashed border-stone-200 align-top last:border-none"
          >
            <td class="py-3 pr-4">
              <p class="font-medium text-stone-800">{{ job.displayName ?? job.identity }}</p>
              <p class="font-mono text-xs text-stone-400">{{ job.identity }}</p>
            </td>
            <td class="py-3 pr-4 font-mono text-xs text-stone-600">{{ job.schedule ?? '—' }}</td>
            <td class="py-3 pr-4 text-stone-600">
              <BaseBadge v-if="job.paused" color="amber">⏸ En pause</BaseBadge>
              <template v-else>{{ job.nextFireTime ? formatDateTime(job.nextFireTime) : '—' }}</template>
            </td>
            <td class="py-3 pr-4">
              <template v-if="job.lastExecution">
                <div class="flex flex-wrap items-center gap-2">
                  <BaseBadge :color="job.lastExecution.status === 'SUCCESS' ? 'emerald' : 'rose'">
                    {{ job.lastExecution.status === 'SUCCESS' ? '✓ Succès' : '✗ Échec' }}
                  </BaseBadge>
                  <BaseBadge v-if="job.lastExecution.manual" color="violet">manuelle</BaseBadge>
                  <span class="text-stone-600">{{ formatDateTime(job.lastExecution.runAt) }}</span>
                  <span v-if="job.lastExecution.durationMs != null" class="text-xs text-stone-400">
                    ({{ job.lastExecution.durationMs.toLocaleString('fr-FR') }} ms)
                  </span>
                </div>
                <p v-if="job.lastExecution.error" class="mt-1 max-w-md text-xs text-rose-600">
                  {{ job.lastExecution.error }}
                </p>
              </template>
              <span v-else class="text-stone-400">Jamais exécutée</span>
            </td>
            <td class="py-3">
              <div class="flex gap-2">
                <BaseButton
                  v-if="job.runnable"
                  variant="secondary"
                  :loading="runningIdentity === job.identity"
                  @click="run(job)"
                >
                  Exécuter
                </BaseButton>
                <BaseButton
                  variant="ghost"
                  :loading="togglingIdentity === job.identity"
                  @click="togglePause(job)"
                >
                  {{ job.paused ? 'Reprendre' : 'Mettre en pause' }}
                </BaseButton>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </BaseCard>
</template>
