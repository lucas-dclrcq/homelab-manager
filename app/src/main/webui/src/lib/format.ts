export function formatBytes(bytes: number): string {
  if (bytes <= 0) return '0 o'
  const units = ['o', 'Ko', 'Mo', 'Go', 'To', 'Po']
  const exponent = Math.min(Math.floor(Math.log(bytes) / Math.log(1024)), units.length - 1)
  const value = bytes / 1024 ** exponent
  return `${value.toLocaleString('fr-FR', { maximumFractionDigits: 1 })} ${units[exponent]}`
}

export function formatDateTime(isoDate: string): string {
  return new Date(isoDate).toLocaleString('fr-FR', {
    day: 'numeric',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
  })
}

/** Couleur stable dérivée du nom, pour les fallbacks de logo et les accents de catégorie */
export function colorFromName(name: string): string {
  // Teintes naturelles : sauge, terracotta, ocre, vieux rose, bleu gris, olive, mauve
  const palette = ['#5f854a', '#c1663f', '#c99a2e', '#ad5c6d', '#5d84a6', '#7d7440', '#8a6ea3']
  let hash = 0
  for (const char of name) {
    hash = (hash * 31 + char.codePointAt(0)!) >>> 0
  }
  return palette[hash % palette.length]
}
