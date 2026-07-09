<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { useGetApiFinancesEntries } from '../../api/service/homelab'
import type { EntryType, FinanceEntryDto } from '../../api/model'
import BaseCard from '../ui/BaseCard.vue'
import BaseBadge from '../ui/BaseBadge.vue'
import BaseSpinner from '../ui/BaseSpinner.vue'
import UiIcon from '../ui/UiIcon.vue'
import { formatDate, formatEuros } from '../../lib/format'
import { sourcePresentation } from '../../lib/finances'

const PAGE_SIZE = 15

const props = defineProps<{ year: number; editable?: boolean }>()

const emit = defineEmits<{
  edit: [entry: FinanceEntryDto]
  delete: [entry: FinanceEntryDto]
}>()

const typeFilter = ref<EntryType | 'ALL'>('ALL')
const page = ref(0)

watch([() => props.year, typeFilter], () => {
  page.value = 0
})

const params = computed(() => ({
  year: props.year,
  type: typeFilter.value === 'ALL' ? null : typeFilter.value,
  page: page.value,
  pageSize: PAGE_SIZE,
}))

const { data, isPending, isError } = useGetApiFinancesEntries(params)

const totalPages = computed(() =>
  data.value ? Math.ceil(data.value.total / PAGE_SIZE) : 0,
)

const filters: { value: EntryType | 'ALL'; label: string }[] = [
  { value: 'ALL', label: 'Tout' },
  { value: 'CONTRIBUTION', label: 'Cotisations' },
  { value: 'EXPENSE', label: 'Dépenses' },
]

function amountLabel(entry: FinanceEntryDto): string {
  const sign = entry.type === 'CONTRIBUTION' ? '+' : '−'
  return `${sign} ${formatEuros(entry.amountCents)}`
}

function whoLabel(entry: FinanceEntryDto): string {
  return entry.memberDisplayName ?? entry.vendor ?? '—'
}
</script>

<template>
  <BaseCard>
    <div class="mb-4 flex flex-wrap items-center justify-between gap-3">
      <h2 class="font-display text-xl font-bold">Historique</h2>
      <div class="flex gap-1.5">
        <button
          v-for="filter in filters"
          :key="filter.value"
          type="button"
          class="rounded-full px-3.5 py-1.5 text-xs font-bold transition-colors"
          :class="
            typeFilter === filter.value
              ? 'bg-ink text-cream'
              : 'bg-line/60 text-ink-soft hover:bg-line'
          "
          @click="typeFilter = filter.value"
        >
          {{ filter.label }}
        </button>
      </div>
    </div>

    <div v-if="isPending" class="flex justify-center py-10">
      <BaseSpinner />
    </div>
    <p v-else-if="isError" class="py-6 text-sm font-bold text-berry">
      Impossible de récupérer l'historique des finances.
    </p>
    <p
      v-else-if="!data || data.items.length === 0"
      class="py-6 text-sm text-mute"
    >
      Aucune écriture pour cette période.
    </p>

    <template v-else>
      <table class="w-full text-sm">
        <thead>
          <tr class="border-b-[1.5px] border-line text-left text-xs text-mute">
            <th class="pb-2 pr-3 font-bold">Date</th>
            <th class="pb-2 pr-3 font-bold">Libellé</th>
            <th class="hidden pb-2 pr-3 font-bold sm:table-cell">
              Membre / Fournisseur
            </th>
            <th class="hidden pb-2 pr-3 font-bold sm:table-cell">Source</th>
            <th class="pb-2 text-right font-bold">Montant</th>
            <th v-if="editable" class="pb-2 pl-3 text-right font-bold">
              Actions
            </th>
          </tr>
        </thead>
        <tbody>
          <tr
            v-for="entry in data.items"
            :key="entry.id"
            class="border-b border-line/60 last:border-b-0"
          >
            <td class="py-2.5 pr-3 whitespace-nowrap text-ink-soft">
              {{ formatDate(entry.entryDate) }}
            </td>
            <td class="py-2.5 pr-3 font-bold">
              {{ entry.label }}
              <p
                v-if="entry.notes"
                class="mt-0.5 text-xs font-normal text-mute"
              >
                {{ entry.notes }}
              </p>
            </td>
            <td class="hidden py-2.5 pr-3 text-ink-soft sm:table-cell">
              {{ whoLabel(entry) }}
            </td>
            <td class="hidden py-2.5 pr-3 sm:table-cell">
              <BaseBadge :color="sourcePresentation[entry.source].color">
                {{ sourcePresentation[entry.source].label }}
              </BaseBadge>
            </td>
            <td
              class="py-2.5 text-right font-display font-bold whitespace-nowrap"
              :class="
                entry.type === 'CONTRIBUTION' ? 'text-sage' : 'text-berry'
              "
            >
              {{ amountLabel(entry) }}
            </td>
            <td
              v-if="editable"
              class="py-2.5 pl-3 text-right whitespace-nowrap"
            >
              <button
                type="button"
                class="rounded-lg p-1.5 text-mute transition-colors hover:bg-cream hover:text-ink"
                aria-label="Modifier"
                @click="emit('edit', entry)"
              >
                <UiIcon name="pencil" class="size-4" />
              </button>
              <button
                type="button"
                class="rounded-lg p-1.5 text-mute transition-colors hover:bg-berry-soft hover:text-berry"
                aria-label="Supprimer"
                :disabled="entry.source === 'RECURRING'"
                :class="{
                  'cursor-not-allowed opacity-30': entry.source === 'RECURRING',
                }"
                :title="
                  entry.source === 'RECURRING'
                    ? 'Générée par une règle récurrente : modifie-la ou désactive la règle'
                    : undefined
                "
                @click="emit('delete', entry)"
              >
                <UiIcon name="trash-2" class="size-4" />
              </button>
            </td>
          </tr>
        </tbody>
      </table>

      <div
        v-if="totalPages > 1"
        class="mt-4 flex items-center justify-between text-xs text-mute"
      >
        <span>{{ data.total }} écritures</span>
        <div class="flex items-center gap-2">
          <button
            type="button"
            class="rounded-full bg-line/60 px-3 py-1.5 font-bold text-ink-soft transition-colors hover:bg-line disabled:opacity-40"
            :disabled="page === 0"
            @click="page--"
          >
            Précédent
          </button>
          <span>{{ page + 1 }} / {{ totalPages }}</span>
          <button
            type="button"
            class="rounded-full bg-line/60 px-3 py-1.5 font-bold text-ink-soft transition-colors hover:bg-line disabled:opacity-40"
            :disabled="page + 1 >= totalPages"
            @click="page++"
          >
            Suivant
          </button>
        </div>
      </div>
    </template>
  </BaseCard>
</template>
