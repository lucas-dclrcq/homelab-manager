<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useQueryClient } from '@tanstack/vue-query'
import {
  getGetApiAdminFinancesRulesQueryKey,
  useDeleteApiAdminFinancesEntriesId,
  useDeleteApiAdminFinancesRulesId,
  useGetApiAdminFinancesRules,
  useGetApiAdminFinancesSettings,
  useGetApiMembers,
  usePutApiAdminFinancesSettings,
} from '../api/service/homelab'
import type { FinanceEntryDto, RecurringRuleDto } from '../api/model'
import BaseCard from '../components/ui/BaseCard.vue'
import BaseBadge from '../components/ui/BaseBadge.vue'
import BaseButton from '../components/ui/BaseButton.vue'
import BaseInput from '../components/ui/BaseInput.vue'
import BaseModal from '../components/ui/BaseModal.vue'
import BaseSpinner from '../components/ui/BaseSpinner.vue'
import UiIcon from '../components/ui/UiIcon.vue'
import FinanceEntriesTable from '../components/finances/FinanceEntriesTable.vue'
import FinanceEntryForm from '../components/finances/FinanceEntryForm.vue'
import RecurringRuleForm from '../components/finances/RecurringRuleForm.vue'
import { formatDate, formatEuros } from '../lib/format'
import { typePresentation } from '../lib/finances'

type Tab = 'entries' | 'rules' | 'settings'

const queryClient = useQueryClient()

const tab = ref<Tab>('entries')
const tabs: { value: Tab; label: string }[] = [
  { value: 'entries', label: 'Écritures' },
  { value: 'rules', label: 'Récurrences' },
  { value: 'settings', label: 'Réglages' },
]

// --- Écritures ---
const FIRST_YEAR = 2024
const currentYear = new Date().getFullYear()
const year = ref(currentYear)
const years = Array.from(
  { length: currentYear - FIRST_YEAR + 1 },
  (_, index) => currentYear - index,
)

const entryFormOpen = ref(false)
const editingEntry = ref<FinanceEntryDto | null>(null)
const deletingEntry = ref<FinanceEntryDto | null>(null)

function openCreateEntry() {
  editingEntry.value = null
  entryFormOpen.value = true
}

function openEditEntry(entry: FinanceEntryDto) {
  editingEntry.value = entry
  entryFormOpen.value = true
}

const { mutate: deleteEntry, isPending: isDeletingEntry } =
  useDeleteApiAdminFinancesEntriesId({
    mutation: {
      onSuccess: () => {
        queryClient.invalidateQueries({ queryKey: ['api', 'finances'] })
        deletingEntry.value = null
      },
    },
  })

// --- Récurrences ---
const {
  data: rules,
  isPending: rulesPending,
  isError: rulesError,
} = useGetApiAdminFinancesRules()

const ruleFormOpen = ref(false)
const editingRule = ref<RecurringRuleDto | null>(null)
const deletingRule = ref<RecurringRuleDto | null>(null)

function openCreateRule() {
  editingRule.value = null
  ruleFormOpen.value = true
}

function openEditRule(rule: RecurringRuleDto) {
  editingRule.value = rule
  ruleFormOpen.value = true
}

const { mutate: deleteRule, isPending: isDeletingRule } =
  useDeleteApiAdminFinancesRulesId({
    mutation: {
      onSuccess: () => {
        queryClient.invalidateQueries({
          queryKey: getGetApiAdminFinancesRulesQueryKey(),
        })
        deletingRule.value = null
      },
    },
  })

const { data: members } = useGetApiMembers()
const memberNames = computed(() => {
  const names = new Map<string, string>()
  for (const member of members.value ?? []) {
    names.set(member.id, member.displayName)
  }
  return names
})

function ruleScheduleLabel(rule: RecurringRuleDto): string {
  const until = rule.endDate ? ` jusqu'au ${formatDate(rule.endDate)}` : ''
  return `le ${rule.dayOfMonth} du mois, depuis le ${formatDate(rule.startDate)}${until}`
}

// --- Réglages ---
const { data: settings } = useGetApiAdminFinancesSettings()
const kwhPrice = ref('')
watch(
  settings,
  (value) => {
    kwhPrice.value = value?.kwhPrice != null ? String(value.kwhPrice) : ''
  },
  { immediate: true },
)

const {
  mutate: saveSettings,
  isPending: isSavingSettings,
  isSuccess: settingsSaved,
  isError: settingsError,
} = usePutApiAdminFinancesSettings({
  mutation: {
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['api'] })
    },
  },
})

function submitSettings() {
  // v-model sur un input number renvoie un number ; une string ne survit qu'au champ vide
  const price = String(kwhPrice.value).replace(',', '.').trim()
  saveSettings({ data: { kwhPrice: price ? Number(price) : null } })
}
</script>

<template>
  <div class="flex max-w-5xl flex-col gap-8">
    <div class="flex flex-wrap gap-1.5">
      <button
        v-for="item in tabs"
        :key="item.value"
        type="button"
        class="rounded-full px-4 py-2 text-sm font-bold transition-colors"
        :class="
          tab === item.value
            ? 'bg-ink text-cream'
            : 'bg-line/60 text-ink-soft hover:bg-line'
        "
        @click="tab = item.value"
      >
        {{ item.label }}
      </button>
    </div>

    <!-- Écritures -->
    <template v-if="tab === 'entries'">
      <div class="flex flex-wrap items-end justify-between gap-4">
        <label class="block">
          <span class="mb-1.5 block text-xs font-bold text-ink-soft"
            >Année</span
          >
          <select
            v-model="year"
            class="rounded-xl border-[1.5px] border-line bg-white px-3.5 py-2.5 text-[15px] font-bold text-ink focus:border-amber focus:ring-[3px] focus:ring-amber/45 focus:outline-none"
          >
            <option v-for="option in years" :key="option" :value="option">
              {{ option }}
            </option>
          </select>
        </label>
        <BaseButton @click="openCreateEntry">
          <UiIcon name="plus" class="size-4" />
          Ajouter une écriture
        </BaseButton>
      </div>

      <FinanceEntriesTable
        :year="year"
        editable
        @edit="openEditEntry"
        @delete="deletingEntry = $event"
      />
    </template>

    <!-- Récurrences -->
    <template v-else-if="tab === 'rules'">
      <div class="flex justify-end">
        <BaseButton @click="openCreateRule">
          <UiIcon name="plus" class="size-4" />
          Ajouter une récurrence
        </BaseButton>
      </div>

      <BaseCard>
        <BaseSpinner v-if="rulesPending" />
        <p v-else-if="rulesError" class="py-6 text-sm font-bold text-berry">
          Impossible de récupérer les règles récurrentes.
        </p>
        <p
          v-else-if="!rules || rules.length === 0"
          class="py-6 text-sm text-mute"
        >
          Aucune règle récurrente pour l'instant.
        </p>
        <table v-else class="w-full text-sm">
          <thead>
            <tr
              class="border-b-[1.5px] border-line text-left text-xs text-mute"
            >
              <th class="pb-2 pr-3 font-bold">Libellé</th>
              <th class="pb-2 pr-3 font-bold">Type</th>
              <th class="hidden pb-2 pr-3 font-bold sm:table-cell">
                Membre / Fournisseur
              </th>
              <th class="hidden pb-2 pr-3 font-bold md:table-cell">Échéance</th>
              <th class="pb-2 pr-3 text-right font-bold">Montant</th>
              <th class="pb-2 pl-3 text-right font-bold">Actions</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="rule in rules"
              :key="rule.id"
              class="border-b border-line/60 last:border-b-0"
              :class="{ 'opacity-50': !rule.active }"
            >
              <td class="py-2.5 pr-3 font-bold">
                {{ rule.label }}
                <BaseBadge v-if="!rule.active" class="ml-1.5">
                  En pause
                </BaseBadge>
              </td>
              <td class="py-2.5 pr-3">
                <BaseBadge :color="typePresentation[rule.type].color">
                  {{ typePresentation[rule.type].label }}
                </BaseBadge>
              </td>
              <td class="hidden py-2.5 pr-3 text-ink-soft sm:table-cell">
                {{
                  (rule.memberId && memberNames.get(rule.memberId)) ||
                  rule.vendor ||
                  '—'
                }}
              </td>
              <td class="hidden py-2.5 pr-3 text-xs text-mute md:table-cell">
                {{ ruleScheduleLabel(rule) }}
              </td>
              <td
                class="py-2.5 pr-3 text-right font-display font-bold whitespace-nowrap"
              >
                {{ formatEuros(rule.amountCents) }}
              </td>
              <td class="py-2.5 pl-3 text-right whitespace-nowrap">
                <button
                  type="button"
                  class="rounded-lg p-1.5 text-mute transition-colors hover:bg-cream hover:text-ink"
                  aria-label="Modifier"
                  @click="openEditRule(rule)"
                >
                  <UiIcon name="pencil" class="size-4" />
                </button>
                <button
                  type="button"
                  class="rounded-lg p-1.5 text-mute transition-colors hover:bg-berry-soft hover:text-berry"
                  aria-label="Supprimer"
                  @click="deletingRule = rule"
                >
                  <UiIcon name="trash-2" class="size-4" />
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </BaseCard>
    </template>

    <!-- Réglages -->
    <template v-else>
      <BaseCard>
        <form
          class="flex max-w-md flex-col gap-4"
          @submit.prevent="submitSettings"
        >
          <h2 class="font-display text-xl font-bold">Énergie</h2>
          <p class="text-sm text-ink-soft">
            Le prix du kWh sert à transformer la consommation mesurée par
            l'onduleur en dépense mensuelle, ajoutée automatiquement le 1er du
            mois.
          </p>
          <p
            v-if="settings && settings.kwhPrice == null"
            class="rounded-xl bg-amber-soft px-3.5 py-2.5 text-sm font-bold text-amber-deep"
          >
            Prix non configuré : la dépense énergétique mensuelle ne sera pas
            créée.
          </p>
          <BaseInput
            v-model="kwhPrice"
            label="Prix du kWh (€)"
            type="number"
            step="0.00001"
            placeholder="0.2016"
          />
          <div class="flex items-center gap-4">
            <BaseButton type="submit" :loading="isSavingSettings">
              <UiIcon name="check" class="size-4" />
              Enregistrer
            </BaseButton>
            <p v-if="settingsSaved" class="text-sm font-bold text-sage">
              Réglages enregistrés.
            </p>
            <p v-if="settingsError" class="text-sm font-bold text-berry">
              L'enregistrement a échoué — vérifie le prix saisi.
            </p>
          </div>
        </form>
      </BaseCard>
    </template>

    <!-- Modales écritures -->
    <BaseModal
      v-if="entryFormOpen"
      :title="editingEntry ? 'Modifier l\'écriture' : 'Ajouter une écriture'"
      @close="entryFormOpen = false"
    >
      <FinanceEntryForm :entry="editingEntry" @saved="entryFormOpen = false" />
    </BaseModal>

    <BaseModal
      v-if="deletingEntry"
      :title="`Supprimer « ${deletingEntry.label} » ?`"
      @close="deletingEntry = null"
    >
      <p class="mb-5 text-sm text-ink-soft">
        L'écriture de
        {{ formatEuros(deletingEntry.amountCents) }} du
        {{ formatDate(deletingEntry.entryDate) }} disparaîtra des totaux. Pas de
        retour en arrière possible.
      </p>
      <div class="flex gap-3">
        <BaseButton
          variant="danger"
          :loading="isDeletingEntry"
          @click="deleteEntry({ id: deletingEntry.id })"
        >
          <UiIcon name="trash-2" class="size-4" />
          Supprimer
        </BaseButton>
        <BaseButton variant="ghost" @click="deletingEntry = null">
          Annuler
        </BaseButton>
      </div>
    </BaseModal>

    <!-- Modales récurrences -->
    <BaseModal
      v-if="ruleFormOpen"
      :title="editingRule ? 'Modifier la récurrence' : 'Ajouter une récurrence'"
      @close="ruleFormOpen = false"
    >
      <RecurringRuleForm :rule="editingRule" @saved="ruleFormOpen = false" />
    </BaseModal>

    <BaseModal
      v-if="deletingRule"
      :title="`Supprimer « ${deletingRule.label} » ?`"
      @close="deletingRule = null"
    >
      <p class="mb-5 text-sm text-ink-soft">
        Les écritures déjà générées restent dans l'historique, mais plus aucune
        nouvelle occurrence ne sera créée. Pour une pause temporaire, désactive
        plutôt la règle.
      </p>
      <div class="flex gap-3">
        <BaseButton
          variant="danger"
          :loading="isDeletingRule"
          @click="deleteRule({ id: deletingRule.id })"
        >
          <UiIcon name="trash-2" class="size-4" />
          Supprimer
        </BaseButton>
        <BaseButton variant="ghost" @click="deletingRule = null">
          Annuler
        </BaseButton>
      </div>
    </BaseModal>
  </div>
</template>
