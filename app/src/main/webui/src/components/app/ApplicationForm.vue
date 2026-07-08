<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useQueryClient } from '@tanstack/vue-query'
import {
  getGetApiApplicationsQueryKey,
  useGetApiApplications,
  usePostApiApplications,
  usePutApiApplicationsId,
} from '../../api/service/homelab'
import type { ApplicationDto } from '../../api/model'
import BaseInput from '../ui/BaseInput.vue'
import BaseTextarea from '../ui/BaseTextarea.vue'
import BaseToggle from '../ui/BaseToggle.vue'
import BaseFileInput from '../ui/BaseFileInput.vue'
import BaseButton from '../ui/BaseButton.vue'
import UiIcon from '../ui/UiIcon.vue'

const props = defineProps<{ application?: ApplicationDto | null }>()
const emit = defineEmits<{ saved: [] }>()

const queryClient = useQueryClient()

const { data: applications } = useGetApiApplications()
const existingCategories = computed(() =>
  [...new Set((applications.value ?? []).map((app) => app.category))].sort(),
)

const form = reactive({
  name: props.application?.name ?? '',
  category: props.application?.category ?? '',
  description: props.application?.description ?? '',
  url: props.application?.url ?? '',
  requiresVpn: props.application?.requiresVpn ?? false,
})
const logo = ref<File | null>(null)

function onSaved() {
  queryClient.invalidateQueries({
    queryKey: getGetApiApplicationsQueryKey(),
  })
  emit('saved')
}

const create = usePostApiApplications({ mutation: { onSuccess: onSaved } })
const update = usePutApiApplicationsId({ mutation: { onSuccess: onSaved } })

const isPending = computed(
  () => create.isPending.value || update.isPending.value,
)
const isError = computed(() => create.isError.value || update.isError.value)

function submit() {
  const data = {
    name: form.name,
    category: form.category,
    description: form.description,
    url: form.url,
    requiresVpn: form.requiresVpn,
    // Jamais envoyés depuis l'admin : le backend conserve les valeurs posées par l'opérateur
    managedBy: null,
    externalId: null,
    logo: logo.value,
  }
  if (props.application) {
    update.mutate({ id: props.application.id, data })
  } else {
    create.mutate({ data })
  }
}
</script>

<template>
  <form class="flex flex-col gap-4" @submit.prevent="submit">
    <div class="grid gap-4 sm:grid-cols-2">
      <BaseInput
        v-model="form.name"
        label="Nom"
        placeholder="Jellyfin"
        required
      />
      <BaseInput
        v-model="form.category"
        label="Catégorie"
        placeholder="Médias"
        list="existing-categories"
        required
      />
      <datalist id="existing-categories">
        <option
          v-for="category in existingCategories"
          :key="category"
          :value="category"
        />
      </datalist>
    </div>

    <BaseTextarea
      v-model="form.description"
      label="Description"
      placeholder="À quoi sert cette appli ?"
      required
    />

    <BaseInput
      v-model="form.url"
      label="URL"
      type="url"
      placeholder="https://jellyfin.mon-homelab.org"
      required
    />

    <div class="grid gap-4 sm:grid-cols-2">
      <BaseFileInput
        v-model="logo"
        :label="
          application && application.hasLogo
            ? 'Nouveau logo (vide = on garde l\'actuel)'
            : 'Logo (png, jpeg, svg, webp — 1 Mo max)'
        "
        accept="image/png,image/jpeg,image/svg+xml,image/webp"
      />
      <div class="flex items-end pb-1">
        <BaseToggle
          v-model="form.requiresVpn"
          label="Accessible uniquement via VPN"
        />
      </div>
    </div>

    <div class="flex items-center gap-4">
      <BaseButton type="submit" :loading="isPending">
        <UiIcon :name="application ? 'pencil' : 'plus'" class="size-4" />
        {{ application ? 'Enregistrer' : 'Ajouter' }}
      </BaseButton>
      <p v-if="isError" class="text-sm font-bold text-berry">
        {{
          application
            ? 'La modification a échoué — vérifie les champs et le logo.'
            : "L'ajout a échoué — vérifie les champs et le logo."
        }}
      </p>
    </div>
  </form>
</template>
