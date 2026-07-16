<script setup lang="ts">
import { computed } from 'vue'
import { useQueryClient } from '@tanstack/vue-query'
import {
  getGetApiMembersQueryKey,
  useGetApiMembers,
  usePostApiAdminJobsIdentityRun,
} from '../api/service/homelab'
import BaseCard from '../components/ui/BaseCard.vue'
import BaseBadge from '../components/ui/BaseBadge.vue'
import BaseButton from '../components/ui/BaseButton.vue'
import BaseSpinner from '../components/ui/BaseSpinner.vue'
import UiIcon from '../components/ui/UiIcon.vue'

const queryClient = useQueryClient()

const { data: members, isPending, isError } = useGetApiMembers()

const {
  mutate: runMemberSync,
  isPending: isSyncing,
  data: syncData,
} = usePostApiAdminJobsIdentityRun({
  mutation: {
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: getGetApiMembersQueryKey(),
      })
    },
  },
})

const syncResult = computed(
  () =>
    syncData.value as { status?: string; error?: string | null } | undefined,
)
</script>

<template>
  <div class="flex max-w-5xl flex-col gap-8">
    <header class="flex flex-wrap items-end justify-end gap-4">
      <BaseButton
        variant="secondary"
        :loading="isSyncing"
        @click="runMemberSync({ identity: 'member-sync' })"
      >
        <UiIcon name="refresh-cw" class="size-4" />
        Synchroniser maintenant
      </BaseButton>
    </header>

    <p
      v-if="syncResult && syncResult.status === 'FAILURE'"
      class="text-sm font-bold text-berry"
    >
      La synchronisation a échoué :
      {{ syncResult.error ?? 'erreur inconnue' }}
    </p>

    <BaseCard>
      <BaseSpinner v-if="isPending" />
      <p v-else-if="isError" class="py-6 text-sm font-bold text-berry">
        Impossible de récupérer la liste des membres.
      </p>
      <p
        v-else-if="!members || members.length === 0"
        class="py-6 text-sm text-mute"
      >
        Aucun membre pour l'instant — lance une synchronisation.
      </p>
      <table v-else class="w-full text-sm">
        <thead>
          <tr class="border-b-[1.5px] border-line text-left text-xs text-mute">
            <th class="pb-2 pr-3 font-bold">Nom</th>
            <th class="hidden pb-2 pr-3 font-bold sm:table-cell">
              Identifiant
            </th>
            <th class="hidden pb-2 pr-3 font-bold md:table-cell">Email</th>
            <th class="pb-2 text-right font-bold">Statut</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="member in members"
            :key="member.id"
            class="border-b border-line/60 last:border-b-0"
          >
            <td class="py-2.5 pr-3 font-bold">{{ member.displayName }}</td>
            <td class="hidden py-2.5 pr-3 text-ink-soft sm:table-cell">
              {{ member.username }}
            </td>
            <td class="hidden py-2.5 pr-3 text-ink-soft md:table-cell">
              {{ member.email ?? '—' }}
            </td>
            <td class="py-2.5 text-right">
              <BaseBadge :color="member.active ? 'sage' : 'neutral'">
                {{ member.active ? 'Actif' : 'Inactif' }}
              </BaseBadge>
            </td>
          </tr>
        </tbody>
      </table>
    </BaseCard>
  </div>
</template>
