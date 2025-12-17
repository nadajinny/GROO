import { createRouter, createWebHistory } from 'vue-router'
import type { NavigationGuardNext, RouteLocationNormalized } from 'vue-router'

import { useAuthStore } from '../stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('../views/HomeView.vue')
    },
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
          path: '',
          redirect: '/app/dashboard'
        },
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
          path: 'features',
          name: 'features',
          component: () => import('../views/FeaturesView.vue')
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
    {
      path: '/features',
      name: 'features-public',
      component: () => import('../views/FeaturesView.vue')
    },
    { path: '/:pathMatch(.*)*', redirect: '/' }
  ]
})

router.beforeEach(async (to, _from, next) => {
  const authStore = useAuthStore()
  if (!authStore.initialized) {
    await authStore.waitForReady()
  }

  const isLoggedIn = Boolean(authStore.user)
  const publicPaths = ['/', '/login', '/features']

  if (!isLoggedIn && !publicPaths.includes(to.path)) {
    next({ path: '/', query: { redirect: to.fullPath } })
    return
  }
  if (isLoggedIn && publicPaths.includes(to.path)) {
    next('/app/dashboard')
    return
  }
  next()
})

export default router
