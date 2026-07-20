import { afterEach, describe, expect, it, vi } from 'vitest'
import {
  downloadEventLabel,
  formatEpisodeRef,
  formatHours,
  formatSince,
  formatWatchTime,
  platformLabel,
  playbackMethodLabel,
} from './statistics'

const norm = (s: string) => s.replace(/\s+/g, ' ').trim()

describe('formatWatchTime', () => {
  it('affiche "< 1 min" sous la minute', () => {
    expect(formatWatchTime(0)).toBe('< 1 min')
    expect(formatWatchTime(59)).toBe('< 1 min')
  })

  it('affiche les minutes seules sous une heure', () => {
    expect(formatWatchTime(60)).toBe('1 min')
    expect(formatWatchTime(1500)).toBe('25 min')
  })

  it('affiche les heures rondes sans minutes', () => {
    expect(formatWatchTime(3600)).toBe('1 h')
  })

  it('affiche heures + minutes paddées', () => {
    expect(formatWatchTime(3660)).toBe('1 h 01')
    expect(formatWatchTime(7380)).toBe('2 h 03')
  })
})

describe('formatHours', () => {
  it('convertit les secondes en heures décimales', () => {
    expect(norm(formatHours(3600))).toBe('1 h')
    expect(norm(formatHours(5400))).toBe('1,5 h')
  })
})

describe('formatEpisodeRef', () => {
  it('formate saison/épisode paddés', () => {
    expect(formatEpisodeRef(1, 2)).toBe('S01E02')
    expect(formatEpisodeRef(10, 5)).toBe('S10E05')
  })

  it('renvoie une chaîne vide si une valeur manque', () => {
    expect(formatEpisodeRef(null, 3)).toBe('')
    expect(formatEpisodeRef(1, undefined)).toBe('')
    expect(formatEpisodeRef(undefined, undefined)).toBe('')
  })
})

describe('formatSince', () => {
  afterEach(() => {
    vi.useRealTimers()
  })

  it('mesure le temps écoulé depuis un instant passé', () => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2024-01-15T12:00:00Z'))
    expect(formatSince('2024-01-15T11:00:00')).toBe('1 h')
  })

  it('borne à zéro un instant futur', () => {
    vi.useFakeTimers()
    vi.setSystemTime(new Date('2024-01-15T12:00:00Z'))
    expect(formatSince('2024-01-15T13:00:00')).toBe('< 1 min')
  })
})

describe('platformLabel', () => {
  it('mappe les plateformes connues', () => {
    expect(platformLabel('WEB')).toBe('Web')
    expect(platformLabel('ANDROID_TV')).toBe('Android TV')
  })

  it('retombe sur la valeur brute si inconnue', () => {
    expect(platformLabel('PLAYSTATION')).toBe('PLAYSTATION')
  })
})

describe('playbackMethodLabel', () => {
  it('mappe les méthodes de lecture connues', () => {
    expect(playbackMethodLabel('DIRECT')).toBe('Lecture directe')
    expect(playbackMethodLabel('TRANSCODE')).toBe('Transcodage')
  })

  it('retombe sur la valeur brute si inconnue', () => {
    expect(playbackMethodLabel('Inconnu')).toBe('Inconnu')
  })
})

describe('downloadEventLabel', () => {
  it('mappe les types de téléchargement', () => {
    expect(downloadEventLabel('movie_downloaded')).toBe('Film')
    expect(downloadEventLabel('subtitles_downloaded')).toBe('Sous-titres')
  })

  it('retombe sur la valeur brute si inconnue', () => {
    expect(downloadEventLabel('unknown_event')).toBe('unknown_event')
  })
})
