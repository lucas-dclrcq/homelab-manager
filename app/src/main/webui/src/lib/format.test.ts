import { describe, expect, it } from 'vitest'
import { formatBytes, formatDate, formatDateTime, formatEuros } from './format'

// Les fonctions locale (toLocaleString 'fr-FR') produisent des espaces insécables dont le
// glyphe change entre versions d'ICU/Node. On normalise les espaces avant d'asserter :
// on verrouille la valeur métier, pas le caractère exact — ce qui reste stable aux upgrades.
const norm = (s: string) => s.replace(/\s+/g, ' ').trim()

describe('formatBytes', () => {
  it('renvoie "0 o" pour zéro ou négatif', () => {
    expect(formatBytes(0)).toBe('0 o')
    expect(formatBytes(-5)).toBe('0 o')
  })

  it('choisit la bonne unité', () => {
    expect(norm(formatBytes(512))).toBe('512 o')
    expect(norm(formatBytes(1024))).toBe('1 Ko')
    expect(norm(formatBytes(5 * 1024 * 1024))).toBe('5 Mo')
  })

  it('garde une décimale', () => {
    expect(norm(formatBytes(1536))).toBe('1,5 Ko')
  })
})

describe('formatEuros', () => {
  it('convertit des centimes en euros avec deux décimales', () => {
    expect(norm(formatEuros(1000))).toBe('10,00 €')
    expect(norm(formatEuros(150))).toBe('1,50 €')
    expect(norm(formatEuros(0))).toBe('0,00 €')
  })

  it('gère les montants négatifs', () => {
    const s = norm(formatEuros(-500))
    expect(s).toContain('5,00')
    expect(s).toContain('€')
    expect(s).toContain('-')
  })
})

describe('formatDateTime', () => {
  it('rend jour, mois court et heure (TZ figé en UTC)', () => {
    const s = norm(formatDateTime('2024-01-15T09:05:00Z')).toLowerCase()
    expect(s).toContain('15')
    expect(s).toContain('janv')
    expect(s).toContain('09:05')
  })
})

describe('formatDate', () => {
  it('rend jour, mois court et année', () => {
    const s = norm(formatDate('2024-01-15T09:05:00Z')).toLowerCase()
    expect(s).toContain('15')
    expect(s).toContain('janv')
    expect(s).toContain('2024')
  })
})
