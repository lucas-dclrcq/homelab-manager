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

const {
  data: jobs,
  isPending,
  isError,
} = useGetApiAdminJobs({
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
  <BaseCard>
    <h2 class="mb-1 font-display text-[22px] font-bold">Tâches planifiées</h2>
    <p class="mb-5 text-sm text-ink-soft">
      Les synchronisations et autres routines qui tournent pendant que tout le
      monde dort.
    </p>

    <BaseSpinner v-if="isPending" />
    <p
      v-else-if="isError"
      class="rounded-card border-[1.5px] border-berry/30 bg-berry-soft p-4 text-sm text-berry"
    >
      Impossible de récupérer l'état des tâches — on réessaie ?
    </p>

    <div v-else-if="jobs" class="overflow-x-auto">
      <table class="w-full text-left text-sm">
        <thead>
          <tr
            class="border-b-[1.5px] border-line text-xs font-bold tracking-[0.06em] text-mute uppercase"
          >
            <th class="py-2 pr-4">Tâche</th>
            <th class="py-2 pr-4">Planification</th>
            <th class="py-2 pr-4">Prochaine exécution</th>
            <th class="py-2 pr-4">Dernière exécution</th>
            <th class="py-2">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="job in jobs"
            :key="job.identity"
            class="border-b border-line align-top last:border-none"
          >
            <td class="py-3 pr-4">
              <p class="font-semibold text-ink">
                {{ job.displayName ?? job.identity }}
              </p>
              <p class="font-mono text-xs text-mute">{{ job.identity }}</p>
            </td>
            <td class="py-3 pr-4 font-mono text-xs text-ink-soft">
              {{ job.schedule ?? '—' }}
            </td>
            <td class="py-3 pr-4 text-ink-soft">
              <BaseBadge v-if="job.paused" color="amber">⏸ En pause</BaseBadge>
              <template v-else>{{
                job.nextFireTime ? formatDateTime(job.nextFireTime) : '—'
              }}</template>
            </td>
            <td class="py-3 pr-4">
              <template v-if="job.lastExecution">
                <div class="flex flex-wrap items-center gap-2">
                  <BaseBadge
                    :color="
                      job.lastExecution.status === 'SUCCESS' ? 'sage' : 'berry'
                    "
                  >
                    {{
                      job.lastExecution.status === 'SUCCESS'
                        ? '✓ Succès'
                        : '✗ Échec'
                    }}
                  </BaseBadge>
                  <BaseBadge v-if="job.lastExecution.manual" color="dusk"
                    >manuelle</BaseBadge
                  >
                  <span class="text-ink-soft">{{
                    formatDateTime(job.lastExecution.runAt)
                  }}</span>
                  <span
                    v-if="job.lastExecution.durationMs != null"
                    class="font-mono text-xs text-mute"
                  >
                    ({{ job.lastExecution.durationMs.toLocaleString('fr-FR') }}
                    ms)
                  </span>
                </div>
                <p
                  v-if="job.lastExecution.error"
                  class="mt-1 max-w-md font-mono text-xs text-berry"
                >
                  {{ job.lastExecution.error }}
                </p>
              </template>
              <span v-else class="text-mute">Jamais exécutée</span>
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
