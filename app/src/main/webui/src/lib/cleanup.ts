// Présentation des statuts du nettoyage : libellés chaleureux, couleurs de la charte.
export type BadgeColor = 'sage' | 'berry' | 'sky' | 'amber' | 'dusk' | 'neutral'

export interface StatusPresentation {
  label: string
  color: BadgeColor
}

const candidateStatuses: Record<string, StatusPresentation> = {
  PROPOSED: { label: 'En sursis', color: 'amber' },
  PROTECTED: { label: 'Gardé', color: 'sage' },
  DELETED: { label: 'Supprimé', color: 'neutral' },
  FAILED: { label: 'Échec', color: 'berry' },
  SKIPPED: { label: 'Ignoré', color: 'sky' },
  CANCELLED: { label: 'Annulé', color: 'neutral' },
}

const campaignStatuses: Record<string, StatusPresentation> = {
  ANNOUNCED: { label: 'En cours', color: 'amber' },
  COMPLETED: { label: 'Terminée', color: 'sage' },
  CANCELLED: { label: 'Annulée', color: 'neutral' },
}

const suggestionStatuses: Record<string, StatusPresentation> = {
  PENDING: { label: 'En sursis', color: 'amber' },
  VETOED: { label: 'Veto', color: 'sage' },
  DELETED: { label: 'Supprimé', color: 'neutral' },
  FAILED: { label: 'Échec', color: 'berry' },
  SKIPPED: { label: 'Annulée', color: 'sky' },
}

const protectionSources: Record<string, StatusPresentation> = {
  VETO: { label: 'Veto', color: 'amber' },
  PROACTIVE: { label: 'Préventive', color: 'dusk' },
}

export function candidateStatusPresentation(
  status: string,
): StatusPresentation {
  return candidateStatuses[status] ?? { label: status, color: 'neutral' }
}

export function campaignStatusPresentation(status: string): StatusPresentation {
  return campaignStatuses[status] ?? { label: status, color: 'neutral' }
}

export function protectionSourcePresentation(
  source: string,
): StatusPresentation {
  return protectionSources[source] ?? { label: source, color: 'neutral' }
}

export function suggestionStatusPresentation(
  status: string,
): StatusPresentation {
  return suggestionStatuses[status] ?? { label: status, color: 'neutral' }
}

/** Jours restants (arrondis au supérieur) avant une échéance ISO locale. */
export function daysUntil(isoDate: string): number {
  return Math.ceil((new Date(isoDate).getTime() - Date.now()) / 86_400_000)
}
