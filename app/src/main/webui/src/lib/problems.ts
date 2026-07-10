import type { CorrectorWorkflowDto } from '../api/model'

type BadgeColor = 'sage' | 'berry' | 'sky' | 'amber' | 'dusk' | 'neutral'

export const statusPresentation: Record<
  string,
  { label: string; color: BadgeColor }
> = {
  IN_PROGRESS: { label: 'En cours', color: 'amber' },
  AWAITING_IMPORT: { label: "En attente d'import", color: 'dusk' },
  COMPLETED: { label: 'Terminé', color: 'sage' },
  ABANDONED: { label: 'Abandonné', color: 'neutral' },
}

export const problemLabels: Record<string, string> = {
  vo_should_be_french: 'Film en VO qui devrait être en VF/MULTI',
}

export const wizardSteps = [
  { key: 'SELECT_MOVIE', label: 'Le film' },
  { key: 'SELECT_PROBLEM', label: 'Le souci' },
  { key: 'SELECT_RELEASE', label: 'La solution' },
  { key: 'AWAITING_IMPORT', label: 'On surveille' },
] as const

export function stepIndex(workflow: CorrectorWorkflowDto): number {
  if (workflow.currentStep === 'COMPLETED') return wizardSteps.length
  const index = wizardSteps.findIndex((s) => s.key === workflow.currentStep)
  return index === -1 ? 0 : index
}

export function isActive(workflow: CorrectorWorkflowDto): boolean {
  return (
    workflow.status === 'IN_PROGRESS' || workflow.status === 'AWAITING_IMPORT'
  )
}
