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
      path: '/problems',
      name: 'problems',
      component: () => import('../pages/ProblemsPage.vue'),
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
      path: '/problems/:id',
      name: 'problem-workflow',
      component: () => import('../pages/ProblemWorkflowPage.vue'),
    },
    {
      path: '/cleanup',
      name: 'cleanup',
      component: () => import('../pages/CleanupPage.vue'),
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
    {
      path: '/admin/problems',
      name: 'admin-problems',
      component: () => import('../pages/AdminProblemsPage.vue'),
      meta: { requiresAdmin: true },
    },
    {
      path: '/admin/problems/:id',
      name: 'admin-problem-workflow',
      component: () => import('../pages/ProblemWorkflowPage.vue'),
      meta: { requiresAdmin: true },
    },
    {
      path: '/admin/cleanup',
      name: 'admin-cleanup',
      component: () => import('../pages/AdminCleanupPage.vue'),
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
