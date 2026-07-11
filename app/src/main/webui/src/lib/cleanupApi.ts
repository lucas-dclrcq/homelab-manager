import { unref, type MaybeRef } from 'vue'
import { useQueryClient, type QueryClient } from '@tanstack/vue-query'
import { isAxiosError } from 'axios'
import {
  getGetApiAdminCleanupCampaignsQueryKey,
  getGetApiCleanupCampaignQueryKey,
  getGetApiCleanupProtectionsQueryKey,
  useDeleteApiAdminCleanupProtectionsId,
  useDeleteApiCleanupProtectionsId,
  useGetApiAdminCleanupCampaigns,
  useGetApiAdminCleanupCampaignsId,
  useGetApiAdminCleanupConfig,
  useGetApiCleanupCampaign,
  useGetApiCleanupProtections,
  usePostApiAdminCleanupCampaignsIdCancel,
  usePostApiAdminCleanupCampaignsScan,
  usePostApiAdminCleanupCandidatesIdRetry,
  usePostApiCleanupCandidatesIdVeto,
  usePostApiCleanupProtections,
} from '../api/service/homelab'

// Façade TanStack du nettoyage : les composants passent par ici, jamais
// directement par les composables Orval, pour garder les invalidations croisées.

/** Invalide la vue user et les vues admin (la clé liste préfixe aussi les détails). */
export function invalidateCampaigns(queryClient: QueryClient) {
  queryClient.invalidateQueries({
    queryKey: getGetApiCleanupCampaignQueryKey(),
  })
  queryClient.invalidateQueries({
    queryKey: getGetApiAdminCleanupCampaignsQueryKey(),
  })
}

export function invalidateProtections(queryClient: QueryClient) {
  queryClient.invalidateQueries({
    queryKey: getGetApiCleanupProtectionsQueryKey(),
  })
}

interface MutationCallbacks<TResult = unknown> {
  onSuccess?: (result: TResult) => void
  onError?: (error: unknown) => void
}

function useCleanupMutationOptions<TResult>(
  callbacks: MutationCallbacks<TResult>,
) {
  const queryClient = useQueryClient()
  return {
    // onSettled et pas onSuccess : un veto 409 ou un retry 502 a pu changer
    // l'état serveur (candidat déjà traité, failureReason mis à jour)
    onSettled: () => {
      invalidateCampaigns(queryClient)
      invalidateProtections(queryClient)
    },
    onSuccess: (result: TResult) => callbacks.onSuccess?.(result),
    onError: (error: unknown) => callbacks.onError?.(error),
  }
}

/** Message d'erreur renvoyé par le backend ({error: "..."}), avec repli générique. */
export function cleanupErrorMessage(error: unknown): string {
  const backendMessage =
    isAxiosError(error) &&
    (error.response?.data as { error?: string } | undefined)?.error
  return backendMessage || 'Une erreur est survenue, réessaie dans un instant.'
}

// --- Côté user ---

export function useCleanupOverview() {
  return useGetApiCleanupCampaign({
    query: {
      // Pendant la période de grâce on garde un œil (vetos des autres, avancement) ;
      // hors campagne, veille lente pour voir apparaître la prochaine sans recharger
      refetchInterval: (query) =>
        query.state.data?.campaign?.status === 'ANNOUNCED'
          ? 30_000
          : query.state.data
            ? 120_000
            : false,
    },
  })
}

export function useVetoMutation(callbacks: MutationCallbacks = {}) {
  return usePostApiCleanupCandidatesIdVeto({
    mutation: useCleanupMutationOptions(callbacks),
  })
}

export function useProtections() {
  return useGetApiCleanupProtections()
}

export function useCreateProtection(callbacks: MutationCallbacks = {}) {
  return usePostApiCleanupProtections({
    mutation: useCleanupMutationOptions(callbacks),
  })
}

export function useDeleteProtection(
  admin: boolean,
  callbacks: MutationCallbacks = {},
) {
  const mutation = useCleanupMutationOptions(callbacks)
  return admin
    ? useDeleteApiAdminCleanupProtectionsId({ mutation })
    : useDeleteApiCleanupProtectionsId({ mutation })
}

// --- Côté admin ---

export function useCleanupConfig() {
  return useGetApiAdminCleanupConfig()
}

export function useCleanupCampaigns(pollWhile?: MaybeRef<boolean>) {
  return useGetApiAdminCleanupCampaigns({
    // Après « Forcer un scan » (202), la campagne naît en arrière-plan : on
    // poll jusqu'à la voir apparaître (pattern JellystatImportCard)
    query: { refetchInterval: () => (unref(pollWhile) ? 3_000 : false) },
  })
}

export function useCleanupCampaignDetails(
  id: MaybeRef<string>,
  enabled: MaybeRef<boolean>,
) {
  return useGetApiAdminCleanupCampaignsId(id, { query: { enabled } })
}

export function useForceScan(callbacks: MutationCallbacks = {}) {
  return usePostApiAdminCleanupCampaignsScan({
    mutation: useCleanupMutationOptions(callbacks),
  })
}

export function useCancelCampaign(callbacks: MutationCallbacks = {}) {
  return usePostApiAdminCleanupCampaignsIdCancel({
    mutation: useCleanupMutationOptions(callbacks),
  })
}

export function useRetryCandidate(callbacks: MutationCallbacks = {}) {
  return usePostApiAdminCleanupCandidatesIdRetry({
    mutation: useCleanupMutationOptions(callbacks),
  })
}
