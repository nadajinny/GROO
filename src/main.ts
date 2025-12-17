import './assets/main.css'

import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { useAuthStore } from './stores/auth'
import { useGroupStore } from './stores/group'
import { useSocialStore } from './stores/social'
import { useThemeStore } from './stores/theme'

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)

const themeStore = useThemeStore(pinia)
themeStore.init()

const authStore = useAuthStore(pinia)
authStore.init()

const groupStore = useGroupStore(pinia)
groupStore.init(authStore)

const socialStore = useSocialStore(pinia)
socialStore.init()

app.use(router)

router.isReady().then(() => {
  app.mount('#app')
})
