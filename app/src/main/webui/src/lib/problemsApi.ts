import type { MaybeRef } from 'vue'
import { useQueryClient, type QueryClient } from '@tanstack/vue-query'
import {
  getGetApiAdminProblemsWorkflowsIdQueryKey,
  getGetApiAdminProblemsWorkflowsQueryKey,
  getGetApiProblemsWorkflowsIdQueryKey,
  getGetApiProblemsWorkflowsQueryKey,
  useGetApiAdminProblemsWorkflowsId,
  useGetApiAdminProblemsWorkflowsIdReleases,
  useGetApiProblemsWorkflowsId,
  useGetApiProblemsWorkflowsIdReleases,
  usePostApiAdminProblemsWorkflowsIdAbandon,
  usePostApiAdminProblemsWorkflowsIdGrab,
  usePostApiAdminProblemsWorkflowsIdMovie,
  usePostApiAdminProblemsWorkflowsIdProblem,
  usePostApiAdminProblemsWorkflowsIdResolve,
  usePostApiAdminProblemsWorkflowsIdSeries,
  usePostApiProblemsWorkflowsIdAbandon,
  usePostApiProblemsWorkflowsIdGrab,
  usePostApiProblemsWorkflowsIdMovie,
  usePostApiProblemsWorkflowsIdProblem,
  usePostApiProblemsWorkflowsIdResolve,
  usePostApiProblemsWorkflowsIdSeries,
} from '../api/service/homelab'
import type { ProblemWorkflowDto } from '../api/model'

// Un seul wizard pour les deux mondes : les composants passent par cette façade,
// qui choisit le composable Orval user ou admin (reprise d'un problème par un admin).
export type ProblemWorkflowView = ProblemWorkflowDto & { reportedBy?: string }

export function invalidateWorkflows(queryClient: QueryClient, id?: string) {
  queryClient.invalidateQueries({
    queryKey: getGetApiProblemsWorkflowsQueryKey(),
  })
  queryClient.invalidateQueries({
    queryKey: getGetApiAdminProblemsWorkflowsQueryKey(),
  })
  if (id) {
    queryClient.invalidateQueries({
      queryKey: getGetApiProblemsWorkflowsIdQueryKey(id),
    })
    queryClient.invalidateQueries({
      queryKey: getGetApiAdminProblemsWorkflowsIdQueryKey(id),
    })
  }
}

export function useWorkflowQuery(admin: boolean, id: MaybeRef<string>) {
  if (admin) {
    return useGetApiAdminProblemsWorkflowsId(id, {
      query: {
        select: (dto): ProblemWorkflowView => ({
          ...dto.workflow,
          reportedBy: dto.username,
        }),
        // Tant qu'on attend l'import Radarr, on vérifie régulièrement si c'est réglé
        refetchInterval: (query) =>
          query.state.data?.workflow.status === 'AWAITING_IMPORT'
            ? 30_000
            : false,
      },
    })
  }
  return useGetApiProblemsWorkflowsId(id, {
    query: {
      select: (dto): ProblemWorkflowView => dto,
      refetchInterval: (query) =>
        query.state.data?.status === 'AWAITING_IMPORT' ? 30_000 : false,
    },
  })
}

export function useReleasesQuery(admin: boolean, id: string) {
  const options = { query: { staleTime: 60_000, retry: false } }
  return admin
    ? useGetApiAdminProblemsWorkflowsIdReleases(id, options)
    : useGetApiProblemsWorkflowsIdReleases(id, options)
}

interface MutationCallbacks {
  onSuccess?: (workflow: ProblemWorkflowDto) => void
  onError?: (error: unknown) => void
}

function useWorkflowMutationOptions(callbacks: MutationCallbacks) {
  const queryClient = useQueryClient()
  return {
    onSuccess: (workflow: ProblemWorkflowDto) => {
      invalidateWorkflows(queryClient, workflow.id)
      callbacks.onSuccess?.(workflow)
    },
    onError: (error: unknown) => callbacks.onError?.(error),
  }
}

export function useSelectMovieMutation(
  admin: boolean,
  callbacks: MutationCallbacks = {},
) {
  const mutation = useWorkflowMutationOptions(callbacks)
  return admin
    ? usePostApiAdminProblemsWorkflowsIdMovie({ mutation })
    : usePostApiProblemsWorkflowsIdMovie({ mutation })
}

export function useSelectSeriesMutation(
  admin: boolean,
  callbacks: MutationCallbacks = {},
) {
  const mutation = useWorkflowMutationOptions(callbacks)
  return admin
    ? usePostApiAdminProblemsWorkflowsIdSeries({ mutation })
    : usePostApiProblemsWorkflowsIdSeries({ mutation })
}

export function useSelectProblemMutation(
  admin: boolean,
  callbacks: MutationCallbacks = {},
) {
  const mutation = useWorkflowMutationOptions(callbacks)
  return admin
    ? usePostApiAdminProblemsWorkflowsIdProblem({ mutation })
    : usePostApiProblemsWorkflowsIdProblem({ mutation })
}

export function useGrabMutation(
  admin: boolean,
  callbacks: MutationCallbacks = {},
) {
  const mutation = useWorkflowMutationOptions(callbacks)
  return admin
    ? usePostApiAdminProblemsWorkflowsIdGrab({ mutation })
    : usePostApiProblemsWorkflowsIdGrab({ mutation })
}

export function useResolveMutation(
  admin: boolean,
  callbacks: MutationCallbacks = {},
) {
  const mutation = useWorkflowMutationOptions(callbacks)
  return admin
    ? usePostApiAdminProblemsWorkflowsIdResolve({ mutation })
    : usePostApiProblemsWorkflowsIdResolve({ mutation })
}

export function useAbandonMutation(
  admin: boolean,
  callbacks: MutationCallbacks = {},
) {
  const mutation = useWorkflowMutationOptions(callbacks)
  return admin
    ? usePostApiAdminProblemsWorkflowsIdAbandon({ mutation })
    : usePostApiProblemsWorkflowsIdAbandon({ mutation })
}
