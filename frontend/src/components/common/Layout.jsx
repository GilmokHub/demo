import { Outlet, Link } from 'react-router-dom'

export default function Layout() {
  return (
    <div className="admin-layout">
      <div className="admin-main">
        <header className="admin-header">
          <Link to="/admin/events" className="d-flex align-items-center gap-2 text-decoration-none text-dark fw-semibold">
            <img src="/logo.png" alt="gilmok" width={28} height={28} />
            gilmok
          </Link>
          <div className="ms-auto d-flex align-items-center gap-2">
            <a
              href="http://localhost:3031"
              className="btn btn-primary btn-sm d-flex align-items-center gap-1"
            >
              <span>🚦 길목 대기열 콘솔 (3031)</span>
              <span className="small">→</span>
            </a>
            <Link to="/" className="btn btn-outline-secondary btn-sm">
              메인 페이지로 이동
            </Link>
          </div>
        </header>
        <main className="admin-content">
          <Outlet />
        </main>
        <footer className="admin-footer">
          <span>© 2026 gilmok. All rights reserved.</span>
        </footer>
      </div>
    </div>
  )
}
