<script setup lang="ts">
import { useUserStore } from '../../stores/user'
import SideNav from '../ui/SideNav.vue'
import SideNavItem from '../ui/SideNavItem.vue'

const userStore = useUserStore()
</script>

<template>
  <div class="flex min-h-screen">
    <aside class="w-60 shrink-0 border-e border-slate-800 bg-slate-900/50">
      <SideNav>
        <template #header>
          <div class="flex items-center gap-2 px-2 pt-2">
            <span
              class="bg-gradient-to-r from-brand-400 to-fuchsia-400 bg-clip-text text-lg font-black tracking-tight text-transparent"
            >
              Homelab
            </span>
          </div>
        </template>

        <SideNavItem to="/" label="Dashboard" icon="📊" accent="#8b5cf6" />
        <SideNavItem to="/applications" label="Applications" icon="🚀" accent="#06b6d4" />
        <SideNavItem v-if="userStore.isAdmin" to="/admin" label="Admin" icon="🛠️" accent="#f59e0b" />

        <template #footer>
          <div class="flex items-center justify-between gap-2 rounded-xl bg-slate-800/60 px-3.5 py-2.5">
            <span class="truncate text-sm font-medium text-slate-300">{{ userStore.username }}</span>
            <a
              href="/logout"
              class="text-xs font-medium text-slate-500 transition-colors hover:text-rose-400"
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
