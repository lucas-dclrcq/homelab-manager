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
      path: '/corrector',
      name: 'corrector',
      component: () => import('../pages/CorrectorPage.vue'),
    },
    {
      path: '/corrector/:id',
      name: 'corrector-workflow',
      component: () => import('../pages/CorrectorWorkflowPage.vue'),
    },
    {
      path: '/admin',
      redirect: { name: 'admin-applications' },
    },
    {
      path: '/admin/applications',
      name: 'admin-applications',
      component: () => import('../pages/AdminApplicationsPage.vue'),
      meta: { requiresAdmin: true },
    },
    {
      path: '/admin/jobs',
      name: 'admin-jobs',
      component: () => import('../pages/AdminJobsPage.vue'),
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
