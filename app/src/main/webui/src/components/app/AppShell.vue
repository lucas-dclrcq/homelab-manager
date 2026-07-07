<script setup lang="ts">
import { computed } from 'vue'
import { useUserStore } from '../../stores/user'
import SideNav from '../ui/SideNav.vue'
import SideNavItem from '../ui/SideNavItem.vue'
import UiIcon from '../ui/UiIcon.vue'
import owlMono from '../../assets/hoohoot-owl-mono.svg'

const userStore = useUserStore()

const initials = computed(() => userStore.username.slice(0, 2).toUpperCase())
</script>

<template>
  <div class="flex min-h-screen flex-col lg:flex-row">
    <!-- La nuit que la chouette surveille : sidebar encre nuit-prune -->
    <aside
      class="w-full shrink-0 bg-ink text-cream lg:sticky lg:top-0 lg:h-screen lg:w-62"
    >
      <SideNav>
        <template #header>
          <div class="flex items-center gap-3 px-2 pt-1">
            <img :src="owlMono" alt="" class="h-10" />
            <span class="font-display text-[22px] font-extrabold">Hoohoot</span>
          </div>
        </template>

        <div
          class="px-3 pt-2 pb-1 text-[11px] font-bold tracking-[0.1em] text-[#8f8798] uppercase"
        >
          La maison
        </div>
        <SideNavItem to="/" label="Accueil" icon="house" />
        <SideNavItem to="/applications" label="Tes applis" icon="layout-grid" />

        <template v-if="userStore.isAdmin">
          <div
            class="px-3 pt-4 pb-1 text-[11px] font-bold tracking-[0.1em] text-[#8f8798] uppercase"
          >
            Le coin technique
          </div>
          <SideNavItem
            to="/admin/applications"
            label="Applications"
            icon="package"
          />
          <SideNavItem to="/admin/jobs" label="Tâches" icon="clock" />
        </template>

        <template #footer>
          <div class="flex items-center gap-3 rounded-xl bg-white/5 p-2.5">
            <span
              class="flex size-9 shrink-0 items-center justify-center rounded-full bg-dusk font-display text-sm font-bold text-white"
              aria-hidden="true"
            >
              {{ initials }}
            </span>
            <div class="min-w-0 flex-1">
              <p class="truncate text-sm font-bold text-white">
                {{ userStore.username }}
              </p>
              <a
                href="/logout"
                class="inline-flex items-center gap-1 text-xs text-[#9b93a4] transition-colors hover:text-berry"
              >
                <UiIcon name="log-out" class="size-3" />
                Déconnexion
              </a>
            </div>
          </div>
        </template>
      </SideNav>
    </aside>

    <main class="min-w-0 flex-1 px-6 py-7 lg:px-9">
      <slot />
    </main>
  </div>
</template>
