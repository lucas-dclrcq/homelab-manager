import { afterEach, describe, expect, it, vi } from 'vitest'
import {
  campaignStatusPresentation,
  candidateStatusPresentation,
  daysUntil,
  protectionSourcePresentation,
} from './cleanup'

describe('présentation des statuts', () => {
  it('mappe les statuts de candidat connus', () => {
    expect(candidateStatusPresentation('PROPOSED')).toEqual({
      label: 'En sursis',
      color: 'amber',
    })
    expect(candidateStatusPresentation('FAILED').color).toBe('berry')
  })

  it('retombe en neutral sur un statut inconnu (label = statut brut)', () => {
    expect(candidateStatusPresentation('WAT')).toEqual({
      label: 'WAT',
      color: 'neutral',
    })
    expect(campaignStatusPresentation('WAT').color).toBe('neutral')
    expect(protectionSourcePresentation('WAT').color).toBe('neutral')
  })

  it('mappe campagnes et sources de protection connues', () => {
    expect(campaignStatusPresentation('COMPLETED')).toEqual({
      label: 'Terminée',
      color: 'sage',
    })
    expect(protectionSourcePresentation('VETO')).toEqual({
      label: 'Veto',
      color: 'amber',
    })
  })
})

describe('daysUntil', () => {
  afterEach(() => {
    vi.useRealTimers()
  })

  it('arrondit au jour supérieur (échéance future)', () => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2024-01-15T12:00:00Z'))
    expect(daysUntil('2024-01-20T12:00:00Z')).toBe(5)
    expect(daysUntil('2024-01-16T00:00:00Z')).toBe(1)
  })

  it('renvoie un nombre négatif pour une échéance passée', () => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2024-01-15T12:00:00Z'))
    expect(daysUntil('2024-01-14T12:00:00Z')).toBe(-1)
  })
})
