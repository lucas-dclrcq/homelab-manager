<script setup lang="ts">
import { useGetApiAdminProblemsWorkflows } from '../../api/service/homelab'
import type { AdminProblemWorkflowDto } from '../../api/model'
import BaseCard from '../ui/BaseCard.vue'
import BaseBadge from '../ui/BaseBadge.vue'
import BaseButton from '../ui/BaseButton.vue'
import BaseSpinner from '../ui/BaseSpinner.vue'
import UiIcon from '../ui/UiIcon.vue'
import { formatDateTime } from '../../lib/format'
import { isActive, problemLabels, statusPresentation } from '../../lib/problems'

defineEmits<{
  resolve: [entry: AdminProblemWorkflowDto]
  delete: [entry: AdminProblemWorkflowDto]
}>()

const { data: entries, isPending, isError } = useGetApiAdminProblemsWorkflows()

function status(value: string) {
  return (
    statusPresentation[value] ?? { label: value, color: 'neutral' as const }
  )
}

function problem(entry: AdminProblemWorkflowDto) {
  const type = entry.workflow.problemType
  return type ? (problemLabels[type] ?? type) : 'À préciser'
}

function canResolve(entry: AdminProblemWorkflowDto) {
  return isActive(entry.workflow)
}
</script>

<template>
  <BaseCard>
    <BaseSpinner v-if="isPending" />
    <p
      v-else-if="isError"
      class="rounded-card border-[1.5px] border-berry/30 bg-berry-soft p-4 text-sm text-berry"
    >
      Impossible de récupérer les problèmes — on réessaie ?
    </p>
    <p v-else-if="!entries?.length" class="text-sm text-ink-soft">
      Aucun problème signalé pour l'instant.
    </p>

    <div v-else class="overflow-x-auto">
      <table class="w-full text-left text-sm">
        <thead>
          <tr
            class="border-b-[1.5px] border-line text-xs font-bold tracking-[0.06em] text-mute uppercase"
          >
            <th class="py-2 pr-4">Média</th>
            <th class="py-2 pr-4">Déclarant</th>
            <th class="py-2 pr-4">Problème</th>
            <th class="py-2 pr-4">Statut</th>
            <th class="py-2 pr-4">Maj</th>
            <th class="py-2">Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="entry in entries"
            :key="entry.workflow.id"
            class="border-b border-line last:border-none"
          >
            <td class="py-3 pr-4">
              <div class="flex items-center gap-2">
                <UiIcon
                  :name="entry.workflow.mediaType === 'tv' ? 'tv' : 'film'"
                  class="size-4 shrink-0 text-mute"
                />
                <div class="min-w-0">
                  <p class="truncate font-semibold text-ink">
                    {{ entry.workflow.media?.title ?? '—' }}
                  </p>
                  <p
                    v-if="
                      entry.workflow.problemType === 'other' &&
                      entry.workflow.description
                    "
                    class="line-clamp-1 max-w-xs text-xs text-mute"
                  >
                    {{ entry.workflow.description }}
                  </p>
                </div>
              </div>
            </td>
            <td class="py-3 pr-4 text-ink-soft">{{ entry.username }}</td>
            <td class="py-3 pr-4 text-ink-soft">{{ problem(entry) }}</td>
            <td class="py-3 pr-4">
              <BaseBadge :color="status(entry.workflow.status).color">
                {{ status(entry.workflow.status).label }}
              </BaseBadge>
            </td>
            <td class="py-3 pr-4 font-mono text-xs text-mute">
              {{ formatDateTime(entry.workflow.updatedAt) }}
            </td>
            <td class="py-3">
              <div class="flex gap-2">
                <BaseButton
                  variant="ghost"
                  @click="$router.push(`/admin/problems/${entry.workflow.id}`)"
                >
                  <UiIcon name="chevron-right" class="size-4" />
                  {{ canResolve(entry) ? 'Reprendre' : 'Voir' }}
                </BaseButton>
                <BaseButton
                  v-if="canResolve(entry)"
                  variant="ghost"
                  @click="$emit('resolve', entry)"
                >
                  <UiIcon name="check" class="size-4 text-sage" />
                  Résoudre
                </BaseButton>
                <BaseButton variant="ghost" @click="$emit('delete', entry)">
                  <UiIcon name="trash-2" class="size-4 text-berry" />
                  Supprimer
                </BaseButton>
              </div>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </BaseCard>
</template>
