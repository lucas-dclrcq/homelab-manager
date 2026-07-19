import { unref, type MaybeRef } from 'vue'
import { useQueryClient, type QueryClient } from '@tanstack/vue-query'
import { isAxiosError } from 'axios'
import type { GetApiCleanupProtectionsParams } from '../api/model'
import {
  getGetApiAdminCleanupCampaignsQueryKey,
  getGetApiCleanupCampaignQueryKey,
  getGetApiCleanupProtectionsQueryKey,
  getGetApiCleanupSuggestionsQueryKey,
  useDeleteApiAdminCleanupProtectionsId,
  useDeleteApiCleanupProtectionsId,
  useGetApiAdminCleanupCampaigns,
  useGetApiAdminCleanupCampaignsId,
  useGetApiAdminCleanupConfig,
  useGetApiCleanupCampaign,
  useGetApiCleanupProtections,
  useGetApiCleanupSuggestions,
  usePostApiAdminCleanupCampaignsIdCancel,
  usePostApiAdminCleanupCampaignsScan,
  usePostApiAdminCleanupCandidatesIdRetry,
  usePostApiCleanupCandidatesIdVeto,
  usePostApiCleanupProtections,
  usePostApiCleanupSuggestions,
} from '../api/service/homelab'

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

export function invalidateSuggestions(queryClient: QueryClient) {
  queryClient.invalidateQueries({
    queryKey: getGetApiCleanupSuggestionsQueryKey(),
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
      invalidateSuggestions(queryClient)
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

export function useProtections(
  params: MaybeRef<GetApiCleanupProtectionsParams>,
) {
  return useGetApiCleanupProtections(params, {
    query: { placeholderData: (previous) => previous },
  })
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

export function useSuggestions() {
  return useGetApiCleanupSuggestions({
    query: { refetchInterval: 60_000 },
  })
}

export function useCreateSuggestion(callbacks: MutationCallbacks = {}) {
  return usePostApiCleanupSuggestions({
    mutation: useCleanupMutationOptions(callbacks),
  })
}

// --- Côté admin ---

export function useCleanupConfig() {
  return useGetApiAdminCleanupConfig()
}

export function useCleanupCampaigns(pollWhile?: MaybeRef<boolean>) {
  return useGetApiAdminCleanupCampaigns({
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
