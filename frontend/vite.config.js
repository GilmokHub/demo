import { defineConfig, loadEnv } from 'vite'
import react from '@vitejs/plugin-react'

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

    return {
        plugins: [react()],
        server: {
            port: 3030,
            proxy: {
                // auth 모듈이 backend로 흡수되었으므로 모두 8081로 전송
                '/auth': backendProxy,
                '/admin': backendProxy,
                '/users': backendProxy,
                '/events': backendProxy,
                '/reservations': backendProxy,
                '/api': backendProxy,
                // /queue 경로는 SDK에서 직접 8082로 통신하므로 프록시에서 제거
            },
        },
    }
})
