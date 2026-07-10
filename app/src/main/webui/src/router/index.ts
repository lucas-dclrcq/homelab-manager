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
      path: '/finances',
      name: 'finances',
      component: () => import('../pages/FinancesPage.vue'),
    },
    {
      path: '/statistics',
      name: 'statistics',
      component: () => import('../pages/StatisticsPage.vue'),
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
    {
      path: '/admin/finances',
      name: 'admin-finances',
      component: () => import('../pages/AdminFinancesPage.vue'),
      meta: { requiresAdmin: true },
    },
    {
      path: '/admin/members',
      name: 'admin-members',
      component: () => import('../pages/AdminMembersPage.vue'),
      meta: { requiresAdmin: true },
    },
    {
      path: '/admin/statistics',
      name: 'admin-statistics',
      component: () => import('../pages/AdminStatisticsPage.vue'),
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
