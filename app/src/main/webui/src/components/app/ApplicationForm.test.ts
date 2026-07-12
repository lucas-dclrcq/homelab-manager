import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises } from '@vue/test-utils'
import type { ApplicationDto } from '../../api/model'
import { customInstance } from '../../api/axios-instance'
import { renderWithQuery } from '../../test/renderWithQuery'
import ApplicationForm from './ApplicationForm.vue'

vi.mock('../../api/axios-instance', () => ({
  customInstance: vi.fn(),
}))

const mockedFetch = vi.mocked(customInstance)

const existing: ApplicationDto = {
  id: 'abc-123',
  name: 'Jellyfin',
  category: 'Médias',
  description: 'Serveur média',
  url: 'https://jellyfin.example.org',
  requiresVpn: false,
  hasLogo: true,
} as ApplicationDto

function callWithMethod(method: string) {
  return mockedFetch.mock.calls.find((c) => c[0]?.method === method)?.[0]
}

beforeEach(() => {
  mockedFetch.mockReset()
  mockedFetch.mockResolvedValue([]) // sert le GET des catégories et les mutations
})

describe('ApplicationForm', () => {
  it('mode création : libellé "Ajouter" et POST au submit', async () => {
    const wrapper = renderWithQuery(ApplicationForm)
    await flushPromises()

    expect(wrapper.get('button[type="submit"]').text()).toContain('Ajouter')

    await wrapper.get('form').trigger('submit')
    await flushPromises()

    const post = callWithMethod('POST')
    expect(post?.url).toBe('/api/applications')
    expect(wrapper.emitted('saved')).toBeTruthy()
  })

  it('mode édition : pré-remplit, libellé "Enregistrer" et PUT ciblé', async () => {
    const wrapper = renderWithQuery(ApplicationForm, {
      props: { application: existing },
    })
    await flushPromises()

    expect(wrapper.findAll('input')[0].element.value).toBe('Jellyfin')
    expect(wrapper.get('button[type="submit"]').text()).toContain('Enregistrer')

    await wrapper.get('form').trigger('submit')
    await flushPromises()

    const put = callWithMethod('PUT')
    expect(put?.url).toContain('abc-123')
    expect(wrapper.emitted('saved')).toBeTruthy()
  })
})
