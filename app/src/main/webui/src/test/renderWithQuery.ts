import { mount, type ComponentMountingOptions } from '@vue/test-utils'
import type { Component } from 'vue'
import { QueryClient, VueQueryPlugin } from '@tanstack/vue-query'

/**
 * Monte un composant avec un vrai QueryClient (retry désactivé pour des tests rapides et
 * déterministes). On exerce ainsi les vrais composables Orval/TanStack — il suffit de mocker
 * l'axios `customInstance` pour contrôler les réponses réseau.
 */
export function renderWithQuery<C extends Component>(
  component: C,
  options: ComponentMountingOptions<C> = {},
) {
  const queryClient = new QueryClient({
    defaultOptions: { queries: { retry: false } },
  })
  const globalOptions = options.global ?? {}
  return mount(component, {
    ...options,
    global: {
      ...globalOptions,
      plugins: [
        [VueQueryPlugin, { queryClient }],
        ...(globalOptions.plugins ?? []),
      ],
    },
  })
}
