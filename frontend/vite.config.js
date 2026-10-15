import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'
import { fileURLToPath } from 'url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))

export default defineConfig(({ mode }) => {
    const env = loadEnv(mode, process.cwd(), '')

    const bypassHtml = (req) => {
        if (req.headers.accept?.includes('text/html')) return '/index.html'
    }

    // 고객사(demo) 백엔드 - 포트 8081 (context-path: /gilmok-demo)
    const backendProxy = {
        target: env.VITE_API_BASE_URL || 'http://localhost:8081',
        changeOrigin: true,
        bypass: bypassHtml,
    }

    // 플랫폼(platform) 대기열 백엔드 - 포트 8082 (context-path: /gilmok-platform)
    const queueProxy = {
        target: env.VITE_QUEUE_SERVER_URL || 'http://localhost:8082',
        changeOrigin: true,
        bypass: bypassHtml,
    }

    return {
        plugins: [react()],
        resolve: {
            alias: {
                '@gilmok/sdk': path.resolve(__dirname, '../../platform/sdk/src'),
            },
        },
        server: {
            port: 3030,
            proxy: {
                '/gilmok-demo': backendProxy,
                '/gilmok-platform': queueProxy,
            },
        },
    }
})
