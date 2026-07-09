import type { EntrySource, EntryType } from '../api/model'

type BadgeColor = 'sage' | 'berry' | 'sky' | 'amber' | 'dusk' | 'neutral'

export const typePresentation: Record<
  EntryType,
  { label: string; color: BadgeColor }
> = {
  CONTRIBUTION: { label: 'Cotisation', color: 'sage' },
  EXPENSE: { label: 'Dépense', color: 'berry' },
}

export const sourcePresentation: Record<
  EntrySource,
  { label: string; color: BadgeColor }
> = {
  MANUAL: { label: 'Manuel', color: 'neutral' },
  RECURRING: { label: 'Récurrent', color: 'dusk' },
  ENERGY: { label: 'Énergie', color: 'sky' },
}

export const monthLabels = [
  'janv.',
  'févr.',
  'mars',
  'avr.',
  'mai',
  'juin',
  'juil.',
  'août',
  'sept.',
  'oct.',
  'nov.',
  'déc.',
]
