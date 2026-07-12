import { describe, expect, it } from 'vitest'
import type { ProblemWorkflowDto } from '../api/model'
import {
  isActive,
  statusPresentation,
  stepIndex,
  wizardStepsFor,
} from './problems'

// Fabrique un workflow minimal : les fonctions ne lisent que mediaType/problemType/
// currentStep/status. ProblemWorkflowDto est assignable à ce littéral, le cast est sûr.
function workflow(overrides: Partial<ProblemWorkflowDto>): ProblemWorkflowDto {
  return {
    mediaType: 'movie',
    status: 'IN_PROGRESS',
    currentStep: 'SELECT_MEDIA',
    problemType: null,
    ...overrides,
  } as ProblemWorkflowDto
}

describe('wizardStepsFor', () => {
  it('série : 3 étapes se terminant par le signalement', () => {
    const steps = wizardStepsFor(workflow({ mediaType: 'tv' }))
    expect(steps.map((s) => s.key)).toEqual([
      'SELECT_MEDIA',
      'SELECT_PROBLEM',
      'REPORTED',
    ])
    expect(steps[0].label).toBe('La série')
  })

  it('film + problème "other" : parcours court (signalement)', () => {
    const steps = wizardStepsFor(
      workflow({ mediaType: 'movie', problemType: 'other' }),
    )
    expect(steps.map((s) => s.key)).toEqual([
      'SELECT_MEDIA',
      'SELECT_PROBLEM',
      'REPORTED',
    ])
    expect(steps[0].label).toBe('Le film')
  })

  it('film + VF : parcours long (release + surveillance)', () => {
    const steps = wizardStepsFor(
      workflow({ mediaType: 'movie', problemType: 'vo_should_be_french' }),
    )
    expect(steps.map((s) => s.key)).toEqual([
      'SELECT_MEDIA',
      'SELECT_PROBLEM',
      'SELECT_RELEASE',
      'AWAITING_IMPORT',
    ])
  })
})

describe('stepIndex', () => {
  it("renvoie l'index de l'étape courante", () => {
    expect(
      stepIndex(workflow({ mediaType: 'tv', currentStep: 'SELECT_PROBLEM' })),
    ).toBe(1)
  })

  it('renvoie la longueur totale quand terminé/résolu', () => {
    const wf = workflow({ mediaType: 'tv', currentStep: 'COMPLETED' })
    expect(stepIndex(wf)).toBe(wizardStepsFor(wf).length)
    expect(
      stepIndex(workflow({ mediaType: 'tv', currentStep: 'RESOLVED' })),
    ).toBe(3)
  })

  it('retombe à 0 sur une étape inconnue', () => {
    expect(
      stepIndex(workflow({ mediaType: 'tv', currentStep: 'MYSTERY' })),
    ).toBe(0)
  })
})

describe('isActive', () => {
  it('actif pour les statuts en cours', () => {
    expect(isActive(workflow({ status: 'IN_PROGRESS' }))).toBe(true)
    expect(isActive(workflow({ status: 'AWAITING_IMPORT' }))).toBe(true)
    expect(isActive(workflow({ status: 'REPORTED' }))).toBe(true)
  })

  it('inactif pour les statuts terminaux', () => {
    expect(isActive(workflow({ status: 'COMPLETED' }))).toBe(false)
    expect(isActive(workflow({ status: 'ABANDONED' }))).toBe(false)
  })
})

describe('statusPresentation', () => {
  it('mappe les statuts vers libellé + couleur', () => {
    expect(statusPresentation.IN_PROGRESS).toEqual({
      label: 'En cours',
      color: 'amber',
    })
    expect(statusPresentation.RESOLVED.color).toBe('sage')
  })
})
