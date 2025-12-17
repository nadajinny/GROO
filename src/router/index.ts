import { createRouter, createWebHistory } from 'vue-router'
import type { NavigationGuardNext, RouteLocationNormalized } from 'vue-router'

import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: '/login' },
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue')
    },
    {
      path: '/app',
      component: () => import('../layouts/AppLayout.vue'),
      children: [
        {
          path: 'dashboard',
          name: 'dashboard',
          component: () => import('../views/DashboardView.vue')
        },
        {
          path: 'groups',
          name: 'groups',
          component: () => import('../views/GroupsView.vue')
        },
        {
          path: 'groups/:groupId',
          name: 'group-detail',
          component: () => import('../views/GroupDetailView.vue')
        },
        {
          path: 'projects',
          name: 'projects',
          component: () => import('../views/ProjectsView.vue')
        },
        {
          path: 'messages',
          name: 'messages',
          component: () => import('../views/MessagesView.vue')
        },
        {
          path: 'profile',
          name: 'profile',
          component: () => import('../views/ProfileView.vue')
        },
        {
          path: 'tasks/:taskId',
          name: 'task-detail',
          component: () => import('../views/TaskDetailView.vue'),
          props: true
        }
      ]
    },
    { path: '/:pathMatch(.*)*', redirect: '/login' }
  ]
})

router.beforeEach(async (to, _from, next) => {
  const authStore = useAuthStore()
  if (!authStore.initialized) {
    await authStore.waitForReady()
  }

  const isLoggedIn = Boolean(authStore.user)
  if (!isLoggedIn && to.path !== '/login') {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }
  if (isLoggedIn && to.path === '/login') {
    next('/app/dashboard')
    return
  }
  next()
})

export default router
