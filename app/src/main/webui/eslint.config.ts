import pluginVue from 'eslint-plugin-vue'
import {
  defineConfigWithVueTs,
  vueTsConfigs,
} from '@vue/eslint-config-typescript'
import skipFormatting from '@vue/eslint-config-prettier/skip-formatting'

// Lint minimal et cohérent : correctness (vue-essential + ts-recommended), pas de règles
// de style (Prettier possède le formatage via skip-formatting). Pas de lint « type-aware »
// (redondant avec le TS strict + `npm run type-check`).
export default defineConfigWithVueTs(
  { files: ['**/*.{ts,mts,tsx,vue}'] },
  // Code généré par Orval + build : jamais lintés.
  { ignores: ['dist/**', 'src/api/**'] },
  pluginVue.configs['flat/essential'],
  vueTsConfigs.recommended,
  skipFormatting,
  {
    rules: {
      // App.vue et les pages/écrans à un seul mot sont volontaires.
      'vue/multi-word-component-names': 'off',
    },
  },
)
