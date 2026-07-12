import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises } from '@vue/test-utils'
import type { ApplicationDto } from '../../api/model'
import { customInstance } from '../../api/axios-instance'
import { renderWithQuery } from '../../test/renderWithQuery'
import ApplicationsByCategory from './ApplicationsByCategory.vue'
import BaseSpinner from '../ui/BaseSpinner.vue'

// On monte le vrai composable Orval/TanStack ; seul l'axios est mocké → un upgrade qui
// casserait le composable serait attrapé ici (pas juste un mock qui suit).
vi.mock('../../api/axios-instance', () => ({
  customInstance: vi.fn(),
}))

const mockedFetch = vi.mocked(customInstance)

function app(overrides: Partial<ApplicationDto>): ApplicationDto {
  return {
    id: 'id',
    name: 'App',
    category: 'Divers',
    description: '',
    url: 'https://example.org',
    requiresVpn: false,
    hasLogo: false,
    ...overrides,
  } as ApplicationDto
}

beforeEach(() => {
  mockedFetch.mockReset()
})

describe('ApplicationsByCategory', () => {
  it('affiche un spinner pendant le chargement', () => {
    mockedFetch.mockReturnValue(new Promise(() => {})) // jamais résolue
    const wrapper = renderWithQuery(ApplicationsByCategory)
    expect(wrapper.findComponent(BaseSpinner).exists()).toBe(true)
  })

  it('groupe les applis par catégorie', async () => {
    mockedFetch.mockResolvedValue([
      app({ id: '1', name: 'Jellyfin', category: 'Médias' }),
      app({ id: '2', name: 'Immich', category: 'Médias' }),
      app({ id: '3', name: 'Gitea', category: 'Outils' }),
    ])
    const wrapper = renderWithQuery(ApplicationsByCategory)
    await flushPromises()

    const sections = wrapper.findAll('section')
    expect(sections).toHaveLength(2)
    expect(sections.map((s) => s.attributes('aria-label'))).toEqual([
      'Médias',
      'Outils',
    ])
    expect(sections[0].findAll('a')).toHaveLength(2)
  })

  it('affiche un état vide quand aucune appli', async () => {
    mockedFetch.mockResolvedValue([])
    const wrapper = renderWithQuery(ApplicationsByCategory)
    await flushPromises()
    expect(wrapper.text()).toContain('Aucune appli')
  })

  it('affiche un message en cas d’erreur', async () => {
    mockedFetch.mockRejectedValue(new Error('boom'))
    const wrapper = renderWithQuery(ApplicationsByCategory)
    await flushPromises()
    expect(wrapper.text()).toContain('la sieste')
  })
})
