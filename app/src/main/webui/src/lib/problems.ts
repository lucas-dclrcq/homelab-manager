import type { ProblemWorkflowDto } from '../api/model'

type BadgeColor = 'sage' | 'berry' | 'sky' | 'amber' | 'dusk' | 'neutral'

export const statusPresentation: Record<
  string,
  { label: string; color: BadgeColor }
> = {
  IN_PROGRESS: { label: 'En cours', color: 'amber' },
  AWAITING_IMPORT: { label: "En attente d'import", color: 'dusk' },
  REPORTED: { label: 'Signalé', color: 'berry' },
  COMPLETED: { label: 'Terminé', color: 'sage' },
  RESOLVED: { label: 'Résolu', color: 'sage' },
  ABANDONED: { label: 'Abandonné', color: 'neutral' },
}

export const problemLabels: Record<string, string> = {
  vo_should_be_french: "En VO alors qu'on le voulait en VF/MULTI",
  other: 'Autre problème',
}

export interface WizardStep {
  key: string
  label: string
}

// Le parcours dépend du problème : VF = grab + surveillance, autre = simple signalement
export function wizardStepsFor(workflow: ProblemWorkflowDto): WizardStep[] {
  const media = {
    key: 'SELECT_MEDIA',
    label: workflow.mediaType === 'tv' ? 'La série' : 'Le film',
  }
  const problem = { key: 'SELECT_PROBLEM', label: 'Le souci' }
  if (workflow.mediaType === 'tv' || workflow.problemType === 'other') {
    return [media, problem, { key: 'REPORTED', label: 'Signalé' }]
  }
  return [
    media,
    problem,
    { key: 'SELECT_RELEASE', label: 'La solution' },
    { key: 'AWAITING_IMPORT', label: 'On surveille' },
  ]
}

export function stepIndex(workflow: ProblemWorkflowDto): number {
  const steps = wizardStepsFor(workflow)
  if (
    workflow.currentStep === 'COMPLETED' ||
    workflow.currentStep === 'RESOLVED'
  ) {
    return steps.length
  }
  const index = steps.findIndex((s) => s.key === workflow.currentStep)
  return index === -1 ? 0 : index
}

export function isActive(workflow: ProblemWorkflowDto): boolean {
  return (
    workflow.status === 'IN_PROGRESS' ||
    workflow.status === 'AWAITING_IMPORT' ||
    workflow.status === 'REPORTED'
  )
}
