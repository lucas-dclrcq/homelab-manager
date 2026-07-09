<script setup lang="ts">
import { computed, reactive } from 'vue'
import { useQueryClient } from '@tanstack/vue-query'
import {
  useGetApiMembers,
  usePostApiAdminFinancesRules,
  usePutApiAdminFinancesRulesId,
} from '../../api/service/homelab'
import type { EntryType, RecurringRuleDto } from '../../api/model'
import BaseInput from '../ui/BaseInput.vue'
import BaseSelect from '../ui/BaseSelect.vue'
import BaseToggle from '../ui/BaseToggle.vue'
import BaseButton from '../ui/BaseButton.vue'
import UiIcon from '../ui/UiIcon.vue'

const props = defineProps<{ rule?: RecurringRuleDto | null }>()
const emit = defineEmits<{ saved: [] }>()

const queryClient = useQueryClient()

const { data: members } = useGetApiMembers()
const selectableMembers = computed(() =>
  (members.value ?? []).filter(
    (member) => member.active || member.id === props.rule?.memberId,
  ),
)

const form = reactive({
  type: (props.rule?.type ?? 'CONTRIBUTION') as EntryType,
  label: props.rule?.label ?? '',
  amountEuros:
    props.rule != null ? (props.rule.amountCents / 100).toFixed(2) : '',
  dayOfMonth: String(props.rule?.dayOfMonth ?? 5),
  memberId: props.rule?.memberId ?? '',
  vendor: props.rule?.vendor ?? '',
  active: props.rule?.active ?? true,
  startDate: props.rule?.startDate ?? new Date().toISOString().slice(0, 10),
  endDate: props.rule?.endDate ?? '',
})

function onSaved() {
  queryClient.invalidateQueries({ queryKey: ['api', 'admin', 'finances'] })
  queryClient.invalidateQueries({ queryKey: ['api', 'finances'] })
  emit('saved')
}

const create = usePostApiAdminFinancesRules({
  mutation: { onSuccess: onSaved },
})
const update = usePutApiAdminFinancesRulesId({
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
    dayOfMonth: Number(form.dayOfMonth),
    memberId: isContribution ? form.memberId || null : null,
    vendor: isContribution ? null : form.vendor || null,
    active: form.active,
    startDate: form.startDate,
    endDate: form.endDate || null,
  }
  if (props.rule) {
    update.mutate({ id: props.rule.id, data })
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
      <BaseInput
        v-model="form.dayOfMonth"
        label="Jour du mois (1 à 28)"
        type="number"
        required
      />
    </div>

    <BaseInput
      v-model="form.label"
      label="Libellé"
      :placeholder="
        form.type === 'CONTRIBUTION'
          ? 'Cotisation mensuelle de Paul'
          : 'Abonnement fibre'
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
        placeholder="Free"
      />
    </div>

    <div class="grid gap-4 sm:grid-cols-2">
      <BaseInput v-model="form.startDate" label="Début" type="date" required />
      <BaseInput
        v-model="form.endDate"
        label="Fin (vide = sans fin)"
        type="date"
      />
    </div>

    <BaseToggle v-model="form.active" label="Règle active" />

    <div class="flex items-center gap-4">
      <BaseButton type="submit" :loading="isPending">
        <UiIcon :name="rule ? 'pencil' : 'plus'" class="size-4" />
        {{ rule ? 'Enregistrer' : 'Ajouter' }}
      </BaseButton>
      <p v-if="isError" class="text-sm font-bold text-berry">
        {{
          rule
            ? 'La modification a échoué — vérifie les champs.'
            : "L'ajout a échoué — vérifie les champs."
        }}
      </p>
    </div>
  </form>
</template>
