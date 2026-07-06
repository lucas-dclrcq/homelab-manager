import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { VueQueryPlugin } from '@tanstack/vue-query'
import './style.css'
import App from './App.vue'
import router from './router'

createApp(App).use(createPinia()).use(router).use(VueQueryPlugin).mount('#app')
