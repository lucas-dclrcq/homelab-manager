<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useQueryClient } from '@tanstack/vue-query'
import {
  getGetApiApplicationsQueryKey,
  useGetApiApplications,
  usePostApiApplications,
} from '../../api/service/homelab'
import BaseCard from '../ui/BaseCard.vue'
import BaseInput from '../ui/BaseInput.vue'
import BaseTextarea from '../ui/BaseTextarea.vue'
import BaseToggle from '../ui/BaseToggle.vue'
import BaseFileInput from '../ui/BaseFileInput.vue'
import BaseButton from '../ui/BaseButton.vue'
import UiIcon from '../ui/UiIcon.vue'

const queryClient = useQueryClient()

const { data: applications } = useGetApiApplications()
const existingCategories = computed(() =>
  [...new Set((applications.value ?? []).map((app) => app.category))].sort(),
)

const form = reactive({
  name: '',
  category: '',
  description: '',
  url: '',
  requiresVpn: false,
})
const logo = ref<File | null>(null)
const logoInput = ref<InstanceType<typeof BaseFileInput>>()
const successMessage = ref('')

const { mutate, isPending, isError } = usePostApiApplications({
  mutation: {
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: getGetApiApplicationsQueryKey(),
      })
      successMessage.value = `« ${form.name} » a rejoint la maison.`
      form.name = ''
      form.category = ''
      form.description = ''
      form.url = ''
      form.requiresVpn = false
      logoInput.value?.clear()
    },
  },
})

function submit() {
  successMessage.value = ''
  mutate({
    data: {
      name: form.name,
      category: form.category,
      description: form.description,
      url: form.url,
      requiresVpn: form.requiresVpn,
      logo: logo.value,
    },
  })
}
</script>

<template>
  <BaseCard>
    <h2 class="mb-1 font-display text-[22px] font-bold">Ajouter une appli</h2>
    <p class="mb-5 text-sm text-ink-soft">
      Un nouveau service dans la maison ? Présente-le ici.
    </p>

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
          ref="logoInput"
          v-model="logo"
          label="Logo (png, jpeg, svg, webp — 1 Mo max)"
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
          <UiIcon name="plus" class="size-4" />
          Ajouter
        </BaseButton>
        <p v-if="successMessage" class="text-sm font-bold text-sage">
          {{ successMessage }}
        </p>
        <p v-if="isError" class="text-sm font-bold text-berry">
          L'ajout a échoué — vérifie les champs et le logo.
        </p>
      </div>
    </form>
  </BaseCard>
</template>
