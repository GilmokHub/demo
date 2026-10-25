import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig(({ mode }) => {
    const env = loadEnv(mode, process.cwd(), '')

    const bypassHtml = (req) => {
        if (req.headers.accept?.includes('text/html')) return '/index.html'
    }

    // 고객사(demo) 백엔드 로컬 프록시 (포트 8081, context-path: /gilmok-demo)
    const backendProxy = {
        target: env.VITE_BACKEND_PROXY_TARGET,
        changeOrigin: true,
        bypass: bypassHtml,
    }

    // 플랫폼(platform) 대기열 백엔드 로컬 프록시 (포트 8082, context-path: /gilmok-platform)
    const queueProxy = {
        target: env.VITE_QUEUE_PROXY_TARGET,
        changeOrigin: true,
        bypass: bypassHtml,
    }

    return {
        plugins: [react()],
        server: {
            port: 3030,
            proxy: {
                '/gilmok-demo': backendProxy,
                '/gilmok-platform': queueProxy,
            },
        },
    }
})
