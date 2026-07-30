import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), tailwindcss()],
  server: {
    // Pinned because the backend's CORS config and password-reset email links both
    // assume http://localhost:5173 (app.frontend.base-url in application.yaml).
    port: 5173,
    strictPort: true,
  },
})
