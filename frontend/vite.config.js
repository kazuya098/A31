import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  server: {
    proxy: {
      '/api': {
        target: 'https://honouredly-supranatural-drucilla.ngrok-free.dev',
        changeOrigin: true,
        headers: {
          "ngrok-skip-browser-warning": "69420" // 强制跳过 ngrok 警告页
        }
      }
    }
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
})
