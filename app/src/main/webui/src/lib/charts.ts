// Options chart.js communes aux graphes de stats, dans la charte Hoohoot
export const hoohootChartColors = {
  amber: '#e8973a',
  dusk: '#7a6ba8',
  sage: '#5e9e7a',
  sky: '#5b8fb5',
  berry: '#c4574f',
  grid: '#ece4d8',
  tick: '#a79e92',
  legend: '#5c5566',
}

export const hoohootLegend = {
  position: 'top' as const,
  labels: {
    font: { family: 'Nunito, sans-serif', weight: 'bold' as const },
    color: hoohootChartColors.legend,
    boxWidth: 14,
    boxHeight: 14,
    borderRadius: 4,
    useBorderRadius: true,
  },
}

export const hoohootScales = {
  x: {
    grid: { display: false },
    ticks: {
      color: hoohootChartColors.tick,
      font: { family: 'Nunito, sans-serif' },
    },
  },
  y: {
    beginAtZero: true,
    grid: { color: hoohootChartColors.grid },
    ticks: {
      color: hoohootChartColors.tick,
      font: { family: 'Nunito, sans-serif' },
      precision: 0,
    },
  },
}
