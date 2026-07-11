<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import {
  cleanupErrorMessage,
  useCancelCampaign,
  useCleanupCampaignDetails,
  useCleanupCampaigns,
  useCleanupConfig,
  useForceScan,
  useRetryCandidate,
} from '../lib/cleanupApi'
import {
  campaignStatusPresentation,
  candidateStatusPresentation,
} from '../lib/cleanup'
import { formatBytes, formatDate } from '../lib/format'
import BaseBadge from '../components/ui/BaseBadge.vue'
import BaseButton from '../components/ui/BaseButton.vue'
import BaseCard from '../components/ui/BaseCard.vue'
import BaseModal from '../components/ui/BaseModal.vue'
import BaseSpinner from '../components/ui/BaseSpinner.vue'
import StatCard from '../components/ui/StatCard.vue'
import UiIcon from '../components/ui/UiIcon.vue'

const {
  data: config,
  isPending: configPending,
  isError: configError,
} = useCleanupConfig()

// Forçage de scan : confirmation, puis le job tourne en arrière-plan (202).
// Tant que scanStarted est vrai, l'historique est pollé pour voir naître la campagne.
const scanConfirmOpen = ref(false)
const scanStarted = ref(false)
const scanError = ref('')

const {
  data: campaigns,
  isPending: campaignsPending,
  isError: campaignsError,
} = useCleanupCampaigns(scanStarted)

const hasActiveCampaign = computed(
  () => campaigns.value?.some((c) => c.status === 'ANNOUNCED') ?? false,
)

watch(hasActiveCampaign, (active) => {
  if (active) scanStarted.value = false
})

const { mutate: forceScan, isPending: isScanning } = useForceScan({
  onSuccess: () => {
    scanConfirmOpen.value = false
    scanStarted.value = true
    // Si le disque est au-dessus du seuil, aucune campagne ne naîtra : on borne le polling
    setTimeout(() => {
      scanStarted.value = false
    }, 60_000)
  },
  onError: (error) => {
    scanError.value = cleanupErrorMessage(error)
  },
})

function openScanModal() {
  scanError.value = ''
  scanConfirmOpen.value = true
}

// Détail d'une campagne, chargé à la demande
const selectedCampaignId = ref('')
const detailEnabled = computed(() => selectedCampaignId.value !== '')
const {
  data: campaignDetail,
  isFetching: detailFetching,
  isError: detailError,
} = useCleanupCampaignDetails(selectedCampaignId, detailEnabled)

function toggleDetail(id: string) {
  selectedCampaignId.value = selectedCampaignId.value === id ? '' : id
}

const cancelConfirmOpen = ref(false)
const cancelError = ref('')

const { mutate: cancelCampaign, isPending: isCancelling } = useCancelCampaign({
  onSuccess: () => {
    cancelConfirmOpen.value = false
  },
  onError: (error) => {
    cancelError.value = cleanupErrorMessage(error)
  },
})

function openCancelModal() {
  cancelError.value = ''
  cancelConfirmOpen.value = true
}

const retryError = ref('')

const { mutate: retryCandidate, isPending: isRetrying } = useRetryCandidate({
  onSuccess: () => {
    retryError.value = ''
  },
  onError: (error) => {
    retryError.value = cleanupErrorMessage(error)
  },
})

const weights = computed(() => {
  if (!config.value) return []
  return [
    { label: 'Dernier visionnage', value: config.value.weightLastWatched },
    { label: 'Âge du téléchargement', value: config.value.weightDownloadAge },
    { label: 'Taille', value: config.value.weightSize },
    { label: 'Complétion', value: config.value.weightCompletion },
    {
      label: 'Activité du demandeur',
      value: config.value.weightRequesterActivity,
    },
  ]
})
</script>

<template>
  <div class="flex max-w-5xl flex-col gap-8">
    <header class="flex flex-wrap items-end justify-between gap-4">
      <div>
        <h1 class="roost font-display text-[34px] font-extrabold leading-tight">
          Nettoyage
        </h1>
        <p class="mt-2 text-ink-soft">
          La config effective des campagnes, l'historique, et la grosse manette
          pour lancer un scan à la main.
        </p>
      </div>
      <BaseButton
        :disabled="hasActiveCampaign"
        :title="
          hasActiveCampaign
            ? 'Une campagne est déjà en cours : termine-la ou annule-la d\'abord'
            : undefined
        "
        @click="openScanModal"
      >
        <UiIcon name="refresh-cw" class="size-4" />
        Forcer un scan
      </BaseButton>
    </header>

    <p
      v-if="scanStarted"
      class="rounded-card border-[1.5px] border-sage bg-sage-soft px-4 py-3 text-sm font-bold text-sage"
    >
      Scan lancé en arrière-plan. Si le disque manque de place, une campagne
      apparaîtra dans l'historique d'ici quelques instants.
    </p>

    <BaseSpinner v-if="configPending" />

    <p v-else-if="configError" class="text-sm font-bold text-berry">
      Impossible de charger la config du nettoyage. Réessaie dans un instant.
    </p>

    <template v-else-if="config">
      <div class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <StatCard
          label="Seuil de déclenchement"
          :value="formatBytes(config.thresholdBytes)"
          icon="circle-alert"
          tone="berry"
        />
        <StatCard
          label="Objectif d'espace libre"
          :value="formatBytes(config.targetFreeBytes)"
          icon="hard-drive"
          tone="sage"
        />
        <StatCard
          label="Espace libre actuel"
          :value="
            config.diskFreeBytes != null
              ? formatBytes(config.diskFreeBytes)
              : '—'
          "
          icon="hard-drive"
          tone="sky"
        />
        <StatCard
          label="Période de grâce"
          :value="`${config.graceDays} jours`"
          icon="clock"
          tone="amber"
        />
      </div>

      <BaseCard>
        <h2 class="mb-4 font-display text-xl font-bold">Config effective</h2>
        <dl class="grid gap-x-8 gap-y-3 text-sm sm:grid-cols-2">
          <div class="flex justify-between gap-4">
            <dt class="text-ink-soft">Disque surveillé</dt>
            <dd class="font-mono text-xs font-bold">{{ config.diskPath }}</dd>
          </div>
          <div class="flex justify-between gap-4">
            <dt class="text-ink-soft">Âge minimum d'un média</dt>
            <dd class="font-bold">{{ config.minAgeDays }} jours</dd>
          </div>
          <div class="flex justify-between gap-4">
            <dt class="text-ink-soft">Score minimum</dt>
            <dd class="font-bold">{{ config.minScore }} / 100</dd>
          </div>
          <div class="flex justify-between gap-4">
            <dt class="text-ink-soft">Candidats max par campagne</dt>
            <dd class="font-bold">{{ config.maxCandidates }}</dd>
          </div>
          <div class="flex justify-between gap-4">
            <dt class="text-ink-soft">Série vue récemment (exclusion)</dt>
            <dd class="font-bold">{{ config.recentSeriesWatchDays }} jours</dd>
          </div>
          <div class="flex justify-between gap-4">
            <dt class="text-ink-soft">Visionnage en cours (exclusion)</dt>
            <dd class="font-bold">{{ config.inProgressDays }} jours</dd>
          </div>
        </dl>

        <div class="mt-5 border-t-[1.5px] border-line pt-4">
          <h3 class="mb-2 text-xs font-bold text-ink-soft">
            Pondérations du score
          </h3>
          <div class="flex flex-wrap gap-2">
            <BaseBadge
              v-for="weight in weights"
              :key="weight.label"
              color="dusk"
            >
              {{ weight.label }} · {{ weight.value }}
            </BaseBadge>
          </div>
        </div>

        <div
          v-if="config.knownDiskPaths.length"
          class="mt-4 border-t-[1.5px] border-line pt-4"
        >
          <h3 class="mb-2 text-xs font-bold text-ink-soft">
            Chemins de disque connus
          </h3>
          <div class="flex flex-wrap gap-2">
            <span
              v-for="path in config.knownDiskPaths"
              :key="path"
              class="rounded-full bg-line/60 px-3 py-1 font-mono text-xs text-ink-soft"
            >
              {{ path }}
            </span>
          </div>
        </div>
      </BaseCard>
    </template>

    <BaseCard>
      <h2 class="mb-4 font-display text-xl font-bold">
        Historique des campagnes
      </h2>

      <div v-if="campaignsPending" class="py-6">
        <BaseSpinner />
      </div>

      <p v-else-if="campaignsError" class="py-4 text-sm font-bold text-berry">
        Impossible de charger l'historique des campagnes. Réessaie dans un
        instant.
      </p>

      <p v-else-if="!campaigns?.length" class="py-4 text-sm text-mute">
        Aucune campagne pour l'instant : le disque n'a jamais eu besoin qu'on
        lui fasse de la place.
      </p>

      <table v-else class="w-full text-sm">
        <thead>
          <tr class="border-b-[1.5px] border-line text-left text-xs text-mute">
            <th class="pb-2 pr-3 font-bold">Date</th>
            <th class="pb-2 pr-3 font-bold">Statut</th>
            <th class="hidden pb-2 pr-3 font-bold sm:table-cell">Cible</th>
            <th class="hidden pb-2 pr-3 font-bold sm:table-cell">Libéré</th>
            <th class="hidden pb-2 pr-3 font-bold md:table-cell">Candidats</th>
            <th class="pb-2 text-right font-bold">Détail</th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="summary in campaigns"
            :key="summary.id"
            class="border-b border-line/60 last:border-b-0"
          >
            <td class="py-2.5 pr-3 whitespace-nowrap text-ink-soft">
              {{ formatDate(summary.createdAt) }}
            </td>
            <td class="py-2.5 pr-3">
              <BaseBadge
                :color="campaignStatusPresentation(summary.status).color"
              >
                {{ campaignStatusPresentation(summary.status).label }}
              </BaseBadge>
            </td>
            <td class="hidden py-2.5 pr-3 whitespace-nowrap sm:table-cell">
              {{ formatBytes(summary.targetBytesToFree) }}
            </td>
            <td
              class="hidden py-2.5 pr-3 font-display font-bold whitespace-nowrap sm:table-cell"
            >
              {{ formatBytes(summary.freedBytes) }}
            </td>
            <td class="hidden py-2.5 pr-3 text-xs text-ink-soft md:table-cell">
              {{ summary.candidateCount }} au total ·
              <span class="text-ink">{{ summary.deletedCount }} supprimés</span>
              ·
              <span class="text-sage">{{ summary.protectedCount }} gardés</span>
              <template v-if="summary.skippedCount">
                · {{ summary.skippedCount }} ignorés
              </template>
              <template v-if="summary.failedCount">
                ·
                <span class="text-berry">{{ summary.failedCount }} échecs</span>
              </template>
            </td>
            <td class="py-2.5 text-right">
              <button
                type="button"
                class="rounded-lg p-1.5 text-mute transition-colors hover:bg-cream hover:text-ink"
                :aria-label="
                  selectedCampaignId === summary.id
                    ? 'Replier le détail'
                    : 'Voir le détail'
                "
                @click="toggleDetail(summary.id)"
              >
                <UiIcon
                  name="chevron-right"
                  class="size-4 transition-transform"
                  :class="{ 'rotate-90': selectedCampaignId === summary.id }"
                />
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </BaseCard>

    <BaseCard v-if="detailEnabled">
      <div v-if="detailFetching && !campaignDetail" class="py-6">
        <BaseSpinner />
      </div>

      <p v-else-if="detailError" class="py-4 text-sm font-bold text-berry">
        Impossible de charger le détail de la campagne. Réessaie dans un
        instant.
      </p>

      <template v-else-if="campaignDetail">
        <div class="flex flex-wrap items-center justify-between gap-3">
          <div class="flex items-center gap-3">
            <h2 class="font-display text-xl font-bold">
              Campagne du {{ formatDate(campaignDetail.createdAt) }}
            </h2>
            <BaseBadge
              :color="campaignStatusPresentation(campaignDetail.status).color"
            >
              {{ campaignStatusPresentation(campaignDetail.status).label }}
            </BaseBadge>
          </div>
          <BaseButton
            v-if="campaignDetail.status === 'ANNOUNCED'"
            variant="danger"
            @click="openCancelModal"
          >
            <UiIcon name="x" class="size-4" />
            Annuler la campagne
          </BaseButton>
        </div>

        <p class="mt-1 text-sm text-ink-soft">
          Fin de grâce le {{ formatDate(campaignDetail.graceEndsAt) }} · cible
          {{ formatBytes(campaignDetail.targetBytesToFree) }} · libéré
          {{ formatBytes(campaignDetail.freedBytes) }}
          <template v-if="campaignDetail.executionSummary?.note">
            · {{ campaignDetail.executionSummary.note }}
          </template>
        </p>

        <p v-if="retryError" class="mt-3 text-sm font-bold text-berry">
          {{ retryError }}
        </p>

        <ul class="mt-4 flex flex-col gap-2">
          <li
            v-for="candidate in campaignDetail.candidates"
            :key="candidate.id"
            class="flex flex-wrap items-center gap-3 rounded-card border-[1.5px] border-line bg-white p-3"
          >
            <div class="min-w-0 flex-1">
              <p class="truncate font-display font-bold">
                {{ candidate.displayTitle }}
              </p>
              <p class="text-xs text-mute">
                {{ formatBytes(candidate.sizeBytes) }} · score
                {{ Math.round(candidate.score) }}/100
                <template v-if="candidate.requester">
                  · demandé par {{ candidate.requester }}
                </template>
                <template v-if="candidate.protectedBy">
                  · gardé par {{ candidate.protectedBy }}
                </template>
              </p>
              <p v-if="candidate.failureReason" class="text-xs text-berry">
                {{ candidate.failureReason }}
              </p>
            </div>
            <BaseBadge
              :color="candidateStatusPresentation(candidate.status).color"
            >
              {{ candidateStatusPresentation(candidate.status).label }}
            </BaseBadge>
            <BaseButton
              v-if="candidate.status === 'FAILED'"
              variant="ghost"
              :loading="isRetrying"
              @click="retryCandidate({ id: candidate.id })"
            >
              <UiIcon name="refresh-cw" class="size-4" />
              Réessayer
            </BaseButton>
          </li>
        </ul>
      </template>
    </BaseCard>

    <BaseModal
      v-if="scanConfirmOpen"
      title="Forcer un scan ?"
      @close="scanConfirmOpen = false"
    >
      <p class="text-sm text-ink-soft">
        Le scan évalue tout de suite l'espace disque : s'il en manque, une
        campagne s'ouvre et les membres seront prévenus qu'une période de grâce
        démarre. On y va ?
      </p>
      <p v-if="scanError" class="mt-3 text-sm font-bold text-berry">
        {{ scanError }}
      </p>
      <div class="mt-5 flex justify-end gap-2">
        <BaseButton variant="ghost" @click="scanConfirmOpen = false">
          Pas maintenant
        </BaseButton>
        <BaseButton
          :loading="isScanning"
          @click="forceScan({ data: { targetBytes: null } })"
        >
          <UiIcon name="refresh-cw" class="size-4" />
          Lancer le scan
        </BaseButton>
      </div>
    </BaseModal>

    <BaseModal
      v-if="cancelConfirmOpen && campaignDetail"
      title="Annuler la campagne ?"
      @close="cancelConfirmOpen = false"
    >
      <p class="text-sm text-ink-soft">
        Tous les candidats encore en sursis seront libérés, rien ne sera
        supprimé. La campagne passera en « Annulée ».
      </p>
      <p v-if="cancelError" class="mt-3 text-sm font-bold text-berry">
        {{ cancelError }}
      </p>
      <div class="mt-5 flex justify-end gap-2">
        <BaseButton variant="ghost" @click="cancelConfirmOpen = false">
          Garder la campagne
        </BaseButton>
        <BaseButton
          variant="danger"
          :loading="isCancelling"
          @click="cancelCampaign({ id: campaignDetail.id })"
        >
          Oui, annuler
        </BaseButton>
      </div>
    </BaseModal>
  </div>
</template>
