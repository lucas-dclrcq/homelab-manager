import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'dashboard',
      component: () => import('../pages/DashboardPage.vue'),
    },
    {
      path: '/applications',
      name: 'applications',
      component: () => import('../pages/ApplicationsPage.vue'),
    },
    {
      path: '/admin',
      name: 'admin',
      component: () => import('../pages/AdminPage.vue'),
      meta: { requiresAdmin: true },
    },
  ],
})

router.beforeEach(async (to) => {
  const userStore = useUserStore()
  await userStore.ensureLoaded()
  if (to.meta.requiresAdmin && !userStore.isAdmin) {
    return { name: 'dashboard' }
  }
})

export default router
