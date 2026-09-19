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

    // 고객사(demo) 단일 통합 백엔드 - 포트 8081
    const backendProxy = {
        target: env.VITE_API_BASE_URL || 'http://localhost:8081',
        changeOrigin: true,
        bypass: bypassHtml,
    }

    // 플랫폼(platform) 대기열 백엔드 - 포트 8082
    const queueProxy = {
        target: env.VITE_QUEUE_SERVER_URL || 'http://localhost:8082',
        changeOrigin: true,
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
                // auth 모듈이 backend로 흡수되었으므로 모두 8081로 전송
                '/auth': backendProxy,
                '/admin': backendProxy,
                '/users': backendProxy,
                '/events': backendProxy,
                '/reservations': backendProxy,
                '/api/v1/queue': queueProxy,
                '/api': backendProxy,
            },
        },
    }
})
