# Stratégie de test du frontend

Objectif : bloquer les régressions **fonctionnelles** et les régressions **techniques**
(montées de version des libs) avec un socle **simple et peu coûteux à maintenir**.

## Les 3 couches (toutes derrière `mvn verify`)

| Couche         | Commande                                               | Attrape                                                 |
| -------------- | ------------------------------------------------------ | ------------------------------------------------------- |
| **Type-check** | `npm run type-check` (`vue-tsc -b`, déjà dans `build`) | ruptures de typage Vue / TanStack Query / Orval / Pinia |
| **Lint**       | `npm run lint` (ESLint)                                | patterns dépréciés, changements de règles de plugins    |
| **Tests**      | `npm run test` (Vitest + @vue/test-utils)              | régressions de comportement                             |

En CI, Quinoa exécute `npm run build` (orval + type-check + bundle) puis `npm run test:ci`
(`eslint . && vitest run`) pendant `mvn verify` — via `quarkus.quinoa.run-tests=true`. Aucun
job GitHub Actions dédié : le job Maven existant fait office de gate sur chaque PR (y compris
les PR Renovate).

## Deux tiers de tests

- **Tier 1 — fonctions pures (`src/lib/*.test.ts`).** La colonne vertébrale : rapides, sans
  mock, très haut ROI. On teste toutes les fonctions de `format`, `statistics`, `cleanup`,
  `problems`, `finances`.
- **Tier 2 — un exemple par pattern difficile.** `stores/user` (store Pinia + dédup),
  `ui/BaseButton` (primitive), `app/ApplicationsByCategory` (query Orval + états), et
  `app/ApplicationForm` (mutations create/edit). Ils servent de patron réutilisable.

## Règles pour des tests non fragiles

- **Assertions sémantiques** : texte, rôles, `aria-label`, attributs — jamais les classes CSS
  (résilient au churn Tailwind).
- **Locale** : les fonctions `toLocaleString('fr-FR')` produisent des espaces insécables dont
  le glyphe change avec ICU/Node. On normalise (`s.replace(/\s+/g, ' ')`) et on verrouille la
  valeur métier, pas le caractère exact.
- **Temps** : pour les fonctions basées sur l'horloge (`formatSince`, `daysUntil`), utiliser
  `vi.useFakeTimers()` + `vi.setSystemTime(...)`. Le fuseau est figé (`TZ=UTC`) dans
  `vitest.config.ts`.
- **Réseau/données** : monter avec un **vrai `QueryClient`** (`src/test/renderWithQuery.ts`) et
  mocker uniquement l'axios `customInstance` (`vi.mock('../../api/axios-instance')`). On exerce
  ainsi les vrais composables générés — un upgrade qui les casse est attrapé.

## Volontairement hors scope (pour rester simple)

Composants Chart.js (rendu canvas), code généré `src/api/**`, tests E2E (Playwright/Cypress),
snapshots, et la couverture exhaustive composant par composant.
