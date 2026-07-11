<script setup lang="ts">
import { computed, ref } from 'vue'
import type { CleanupCandidateDto } from '../api/model'
import {
  cleanupErrorMessage,
  useCleanupOverview,
  useVetoMutation,
} from '../lib/cleanupApi'
import BaseButton from '../components/ui/BaseButton.vue'
import BaseModal from '../components/ui/BaseModal.vue'
import BaseSpinner from '../components/ui/BaseSpinner.vue'
import UiIcon from '../components/ui/UiIcon.vue'
import CampaignProgress from '../components/cleanup/CampaignProgress.vue'
import CandidateCard from '../components/cleanup/CandidateCard.vue'
import ProtectionsPanel from '../components/cleanup/ProtectionsPanel.vue'
import { formatBytes } from '../lib/format'

const { data: overview, isPending, isError } = useCleanupOverview()

const campaign = computed(() => overview.value?.campaign ?? null)

const vetoTarget = ref<CleanupCandidateDto | null>(null)
const vetoError = ref('')

const { mutate: veto, isPending: isVetoing } = useVetoMutation({
  onSuccess: () => {
    vetoTarget.value = null
  },
  onError: (error) => {
    vetoError.value = cleanupErrorMessage(error)
  },
})

function openVeto(candidate: CleanupCandidateDto) {
  vetoError.value = ''
  vetoTarget.value = candidate
}

function confirmVeto() {
  if (vetoTarget.value) {
    vetoError.value = ''
    veto({ id: vetoTarget.value.id })
  }
}
</script>

<template>
  <div class="flex max-w-5xl flex-col gap-8">
    <header>
      <h1 class="roost font-display text-[34px] font-extrabold leading-tight">
        Nettoyage
      </h1>
      <p class="mt-2 text-ink-soft">
        Quand le disque sature, on fait le tri ensemble. Les médias en sursis
        sont listés ici : un clic suffit pour sauver ceux auxquels tu tiens.
      </p>
    </header>

    <BaseSpinner v-if="isPending" />

    <div
      v-else-if="isError"
      class="rounded-tile border-[1.5px] border-line bg-paper p-8 text-center"
    >
      <p class="font-display text-lg font-bold">
        Impossible de charger la campagne
      </p>
      <p class="mt-1 text-sm text-ink-soft">
        Le serveur n'a pas répondu. Réessaie dans un instant.
      </p>
    </div>

    <template v-else-if="overview">
      <div
        v-if="!campaign"
        class="rounded-tile border-[1.5px] border-dashed border-line bg-paper p-10 text-center"
      >
        <p class="font-display text-lg font-bold">
          Aucune campagne en cours 🎉
        </p>
        <p class="mt-1 text-sm text-ink-soft">
          Le disque respire<template v-if="overview.diskFreeBytes != null">
            : il reste {{ formatBytes(overview.diskFreeBytes) }} de libre sur
            {{ overview.diskPath }}</template
          >. Si ça se resserre, une campagne s'ouvrira et tout le monde pourra
          voter pour garder ses favoris.
        </p>
      </div>

      <template v-else>
        <CampaignProgress
          :campaign="campaign"
          :disk-free-bytes="overview.diskFreeBytes"
        />

        <section class="flex flex-col gap-3">
          <h2 class="font-display text-xl font-bold">Les médias en sursis</h2>
          <p class="-mt-2 text-sm text-ink-soft">
            Sans veto d'ici la fin de la période de grâce, ils seront supprimés
            pour faire de la place.
          </p>
          <div class="grid gap-4 md:grid-cols-2">
            <CandidateCard
              v-for="candidate in campaign.candidates"
              :key="candidate.id"
              :candidate="candidate"
              @veto="openVeto($event)"
            />
          </div>
        </section>
      </template>
    </template>

    <!-- Les protections ne dépendent pas de la campagne : elles restent utilisables même si le GET campagne échoue -->
    <ProtectionsPanel v-if="!isPending" />

    <BaseModal
      v-if="vetoTarget"
      title="Tu le gardes ?"
      @close="vetoTarget = null"
    >
      <p class="text-sm text-ink-soft">
        « {{ vetoTarget.displayTitle }} » sera retiré de la campagne et protégé
        des prochains nettoyages. Personne n'y touchera.
      </p>
      <p v-if="vetoError" class="mt-3 text-sm font-bold text-berry">
        {{ vetoError }}
      </p>
      <div class="mt-5 flex justify-end gap-2">
        <BaseButton variant="ghost" @click="vetoTarget = null">
          Finalement non
        </BaseButton>
        <BaseButton :loading="isVetoing" @click="confirmVeto">
          <UiIcon name="shield" class="size-4" />
          Oui, je garde
        </BaseButton>
      </div>
    </BaseModal>
  </div>
</template>
