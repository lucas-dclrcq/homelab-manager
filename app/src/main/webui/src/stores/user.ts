import { defineStore } from 'pinia'
import { getApiMe } from '../api/service/homelab'
import type { MeDto } from '../api/model'

/**
 * Identité de l'utilisateur connecté (quasi-statique, chargée une fois).
 * L'état serveur (applications, stats, timeline) vit dans le cache TanStack Query.
 */
export const useUserStore = defineStore('user', {
  state: () => ({
    me: null as MeDto | null,
    pending: null as Promise<void> | null,
  }),
  getters: {
    username: (state) => state.me?.username ?? '',
    isAdmin: (state) => state.me?.roles.includes('admin') ?? false,
  },
  actions: {
    async ensureLoaded() {
      if (this.me) return
      this.pending ??= getApiMe().then((me) => {
        this.me = me
      })
      await this.pending
    },
  },
})
