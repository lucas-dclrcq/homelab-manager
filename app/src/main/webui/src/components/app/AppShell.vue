<script setup lang="ts">
import { useUserStore } from '../../stores/user'
import SideNav from '../ui/SideNav.vue'
import SideNavItem from '../ui/SideNavItem.vue'

const userStore = useUserStore()
</script>

<template>
  <div class="flex min-h-screen">
    <aside class="w-60 shrink-0 border-e-2 border-dashed border-stone-300 bg-paper">
      <SideNav>
        <template #header>
          <div class="flex items-center gap-2 px-2 pt-2">
            <span class="font-display text-3xl font-bold tracking-tight text-brand-700">
              Homelab
            </span>
          </div>
        </template>

        <SideNavItem to="/" label="Dashboard" icon="📊" accent="#5f854a" />
        <SideNavItem to="/applications" label="Applications" icon="🚀" accent="#c1663f" />
        <SideNavItem v-if="userStore.isAdmin" to="/admin" label="Admin" icon="🛠️" accent="#c99a2e" />

        <template #footer>
          <div
            class="sketchy-sm flex items-center justify-between gap-2 border border-stone-300 bg-card px-3.5 py-2.5"
          >
            <span class="truncate text-sm font-medium text-stone-600">{{ userStore.username }}</span>
            <a
              href="/logout"
              class="text-xs font-medium text-stone-400 transition-colors hover:text-rose-500"
            >
              Déconnexion
            </a>
          </div>
        </template>
      </SideNav>
    </aside>

    <main class="min-w-0 flex-1 px-8 py-8">
      <slot />
    </main>
  </div>
</template>
