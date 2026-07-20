export const periodOptions = [
  { value: 'TODAY', label: "Aujourd'hui" },
  { value: 'THIS_WEEK', label: 'Cette semaine' },
  { value: 'THIS_MONTH', label: 'Ce mois-ci' },
  { value: 'THIS_YEAR', label: 'Cette année' },
  { value: 'ALL_TIME', label: 'Depuis toujours' },
] as const

export type StatsPeriodValue = (typeof periodOptions)[number]['value']

export type TopUsersMetric = 'time' | 'plays'

export const weekdayLabels = ['Lun', 'Mar', 'Mer', 'Jeu', 'Ven', 'Sam', 'Dim']

export const platformLabels: Record<string, string> = {
  ANDROID_TV: 'Android TV',
  ANDROID: 'Android',
  WEB: 'Web',
  IOS: 'iOS',
  CHROMECAST: 'Chromecast',
  DESKTOP: 'Desktop',
  KODI: 'Kodi',
  OTHER: 'Autre',
}

export function platformLabel(platform: string): string {
  return platformLabels[platform] ?? platform
}

export const playbackMethodLabels: Record<string, string> = {
  DIRECT: 'Lecture directe',
  TRANSCODE: 'Transcodage',
}

export function playbackMethodLabel(method: string): string {
  return playbackMethodLabels[method] ?? method
}

// Type d'événement de la timeline de téléchargements (feature Bibliothèque)
export const downloadEventLabels: Record<string, string> = {
  movie_downloaded: 'Film',
  episode_downloaded: 'Épisode',
  album_downloaded: 'Album',
  subtitles_downloaded: 'Sous-titres',
}

export function downloadEventLabel(eventType: string): string {
  return downloadEventLabels[eventType] ?? eventType
}

export function formatWatchTime(seconds: number): string {
  if (seconds < 60) return '< 1 min'
  const hours = Math.floor(seconds / 3600)
  const minutes = Math.round((seconds % 3600) / 60)
  if (hours === 0) return `${minutes} min`
  if (minutes === 0) return `${hours} h`
  return `${hours} h ${String(minutes).padStart(2, '0')}`
}

export function formatHours(seconds: number): string {
  const hours = seconds / 3600
  return `${hours.toLocaleString('fr-FR', { maximumFractionDigits: 1 })} h`
}

export function formatEpisodeRef(
  seasonNumber?: number | null,
  episodeNumber?: number | null,
): string {
  if (seasonNumber == null || episodeNumber == null) return ''
  const pad = (value: number) => String(value).padStart(2, '0')
  return `S${pad(seasonNumber)}E${pad(episodeNumber)}`
}

export function formatSince(startedAt: string): string {
  const elapsedMs = Date.now() - new Date(`${startedAt}Z`).getTime()
  return formatWatchTime(Math.max(0, Math.round(elapsedMs / 1000)))
}
