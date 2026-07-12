import { defineConfig } from 'vitest/config'
import vue from '@vitejs/plugin-vue'

// Config de test séparée de vite.config.ts : on garde le plugin Vue (SFC) mais on laisse
// Tailwind de côté (inutile en test). TZ figé → les fonctions de date restent déterministes.
export default defineConfig({
  plugins: [vue()],
  test: {
    environment: 'jsdom',
    globals: false,
    setupFiles: ['src/test/setup.ts'],
    env: { TZ: 'UTC' },
  },
})
