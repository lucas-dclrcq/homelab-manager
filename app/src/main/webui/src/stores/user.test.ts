import { beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { customInstance } from '../api/axios-instance'
import { useUserStore } from './user'

// On mocke la couche réseau (axios) : le vrai getApiMe généré est exercé.
vi.mock('../api/axios-instance', () => ({
  customInstance: vi.fn(),
}))

const mockedFetch = vi.mocked(customInstance)

beforeEach(() => {
  setActivePinia(createPinia())
  mockedFetch.mockReset()
})

describe('useUserStore', () => {
  it('a des valeurs par défaut avant chargement', () => {
    const store = useUserStore()
    expect(store.username).toBe('')
    expect(store.isAdmin).toBe(false)
  })

  it('expose username et isAdmin après chargement', async () => {
    mockedFetch.mockResolvedValue({ username: 'zoe', roles: ['admin'] })
    const store = useUserStore()
    await store.ensureLoaded()
    expect(store.username).toBe('zoe')
    expect(store.isAdmin).toBe(true)
  })

  it('isAdmin reste faux sans le rôle admin', async () => {
    mockedFetch.mockResolvedValue({ username: 'bob', roles: ['user'] })
    const store = useUserStore()
    await store.ensureLoaded()
    expect(store.isAdmin).toBe(false)
  })

  it('déduplique les chargements concurrents (un seul appel réseau)', async () => {
    mockedFetch.mockResolvedValue({ username: 'zoe', roles: ['admin'] })
    const store = useUserStore()
    await Promise.all([store.ensureLoaded(), store.ensureLoaded()])
    await store.ensureLoaded()
    expect(mockedFetch).toHaveBeenCalledTimes(1)
  })
})
