import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import router from './router'
import '@/assets/icon.css'
import { createPinia } from 'pinia'
import { autoLogin } from './utils/index.ts'

const pinia = createPinia()
const app = createApp(App)

app.use(router)
app.use(pinia)

autoLogin()

app.mount('#app')
