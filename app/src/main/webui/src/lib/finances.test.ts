import { describe, expect, it } from 'vitest'
import { monthLabels, sourcePresentation, typePresentation } from './finances'

describe('présentation des finances', () => {
  it('mappe les types de mouvement', () => {
    expect(typePresentation.CONTRIBUTION).toEqual({
      label: 'Cotisation',
      color: 'sage',
    })
    expect(typePresentation.EXPENSE).toEqual({
      label: 'Dépense',
      color: 'berry',
    })
  })

  it('mappe les sources de mouvement', () => {
    expect(sourcePresentation.MANUAL.color).toBe('neutral')
    expect(sourcePresentation.RECURRING.label).toBe('Récurrent')
    expect(sourcePresentation.ENERGY.color).toBe('sky')
  })

  it('expose 12 libellés de mois abrégés', () => {
    expect(monthLabels).toHaveLength(12)
    expect(monthLabels[0]).toBe('janv.')
    expect(monthLabels[11]).toBe('déc.')
  })
})
