<script setup lang="ts">
import { computed, reactive } from 'vue'
import { useQueryClient } from '@tanstack/vue-query'
import {
  useGetApiMembers,
  usePostApiAdminFinancesEntries,
  usePutApiAdminFinancesEntriesId,
} from '../../api/service/homelab'
import type { EntryType, FinanceEntryDto } from '../../api/model'
import BaseInput from '../ui/BaseInput.vue'
import BaseSelect from '../ui/BaseSelect.vue'
import BaseTextarea from '../ui/BaseTextarea.vue'
import BaseButton from '../ui/BaseButton.vue'
import UiIcon from '../ui/UiIcon.vue'

const props = defineProps<{ entry?: FinanceEntryDto | null }>()
const emit = defineEmits<{ saved: [] }>()

const queryClient = useQueryClient()

const { data: members } = useGetApiMembers()
const selectableMembers = computed(() =>
  (members.value ?? []).filter(
    (member) => member.active || member.id === props.entry?.memberId,
  ),
)

const form = reactive({
  type: (props.entry?.type ?? 'CONTRIBUTION') as EntryType,
  label: props.entry?.label ?? '',
  amountEuros:
    props.entry != null ? (props.entry.amountCents / 100).toFixed(2) : '',
  entryDate: props.entry?.entryDate ?? new Date().toISOString().slice(0, 10),
  memberId: props.entry?.memberId ?? '',
  vendor: props.entry?.vendor ?? '',
  notes: props.entry?.notes ?? '',
})

function onSaved() {
  queryClient.invalidateQueries({ queryKey: ['api', 'finances'] })
  emit('saved')
}

const create = usePostApiAdminFinancesEntries({
  mutation: { onSuccess: onSaved },
})
const update = usePutApiAdminFinancesEntriesId({
  mutation: { onSuccess: onSaved },
})

const isPending = computed(
  () => create.isPending.value || update.isPending.value,
)
const isError = computed(() => create.isError.value || update.isError.value)

function submit() {
  const isContribution = form.type === 'CONTRIBUTION'
  const data = {
    type: form.type,
    label: form.label,
    // v-model sur un input number renvoie un number ; une string ne survit qu'au champ vide
    amountCents: Math.round(
      Number(String(form.amountEuros).replace(',', '.')) * 100,
    ),
    entryDate: form.entryDate,
    memberId: isContribution ? form.memberId || null : null,
    vendor: isContribution ? null : form.vendor || null,
    notes: form.notes || null,
  }
  if (props.entry) {
    update.mutate({ id: props.entry.id, data })
  } else {
    create.mutate({ data })
  }
}
</script>

<template>
  <form class="flex flex-col gap-4" @submit.prevent="submit">
    <div class="grid gap-4 sm:grid-cols-2">
      <BaseSelect v-model="form.type" label="Type" required>
        <option value="CONTRIBUTION">Cotisation</option>
        <option value="EXPENSE">Dépense</option>
      </BaseSelect>
      <BaseInput v-model="form.entryDate" label="Date" type="date" required />
    </div>

    <BaseInput
      v-model="form.label"
      label="Libellé"
      :placeholder="
        form.type === 'CONTRIBUTION' ? 'Cotisation de Paul' : 'Disque dur 8 To'
      "
      required
    />

    <div class="grid gap-4 sm:grid-cols-2">
      <BaseInput
        v-model="form.amountEuros"
        label="Montant (€)"
        type="number"
        step="0.01"
        placeholder="15.00"
        required
      />
      <BaseSelect
        v-if="form.type === 'CONTRIBUTION'"
        v-model="form.memberId"
        label="Membre"
        required
      >
        <option value="" disabled>Choisir un membre…</option>
        <option
          v-for="member in selectableMembers"
          :key="member.id"
          :value="member.id"
        >
          {{ member.displayName }}
        </option>
      </BaseSelect>
      <BaseInput
        v-else
        v-model="form.vendor"
        label="Fournisseur (optionnel)"
        placeholder="server part deals"
      />
    </div>

    <BaseTextarea
      v-model="form.notes"
      label="Notes (optionnel)"
      placeholder="Un détail à retenir ?"
    />

    <div class="flex items-center gap-4">
      <BaseButton type="submit" :loading="isPending">
        <UiIcon :name="entry ? 'pencil' : 'plus'" class="size-4" />
        {{ entry ? 'Enregistrer' : 'Ajouter' }}
      </BaseButton>
      <p v-if="isError" class="text-sm font-bold text-berry">
        {{
          entry
            ? 'La modification a échoué — vérifie les champs.'
            : "L'ajout a échoué — vérifie les champs."
        }}
      </p>
    </div>
  </form>
</template>
