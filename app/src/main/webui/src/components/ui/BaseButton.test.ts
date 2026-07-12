import { describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import BaseButton from './BaseButton.vue'

describe('BaseButton', () => {
  it('rend le contenu du slot', () => {
    const wrapper = mount(BaseButton, { slots: { default: 'Enregistrer' } })
    expect(wrapper.text()).toContain('Enregistrer')
  })

  it('est de type "button" par défaut', () => {
    const wrapper = mount(BaseButton)
    expect(wrapper.get('button').attributes('type')).toBe('button')
  })

  it('propage le type "submit"', () => {
    const wrapper = mount(BaseButton, { props: { type: 'submit' } })
    expect(wrapper.get('button').attributes('type')).toBe('submit')
  })

  it('désactive le bouton quand disabled', () => {
    const wrapper = mount(BaseButton, { props: { disabled: true } })
    expect(wrapper.get('button').attributes('disabled')).toBeDefined()
  })

  it('désactive et affiche un spinner quand loading', () => {
    const wrapper = mount(BaseButton, { props: { loading: true } })
    expect(wrapper.get('button').attributes('disabled')).toBeDefined()
    expect(wrapper.find('span[aria-hidden="true"]').exists()).toBe(true)
  })

  it('émet le clic', async () => {
    const onClick = vi.fn()
    const wrapper = mount(BaseButton, {
      attrs: { onClick },
      slots: { default: 'X' },
    })
    await wrapper.get('button').trigger('click')
    expect(onClick).toHaveBeenCalledOnce()
  })
})
