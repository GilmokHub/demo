import { useState, useEffect, useCallback } from 'react'
import { useParams, Link } from 'react-router-dom'
import { getEvent, openEvent, closeEvent } from '../../api/events.js'

const quickLinks = [
  { to: 'seats', title: '좌석 관리', desc: '이벤트 좌석 구역을 추가하고 잔여 수를 관리합니다.', icon: 'seat' },
  { to: 'reservations', title: '예약 현황', desc: '예약 통계와 예약 내역을 확인합니다.', icon: 'list' },
]

function toDateStr(ldt) {
  if (!ldt) return '-'
  return new Date(ldt).toISOString().slice(0, 10)
}

export default function EventDetail() {
  const { eventId } = useParams()
  const [event, setEvent] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [actioning, setActioning] = useState(false)

  const loadEvent = useCallback(async () => {
    if (!eventId) return
    setLoading(true)
    setError(null)
    try {
      const data = await getEvent(eventId)
      setEvent(data)
    } catch (e) {
      setError(e.message || '이벤트를 불러오지 못했습니다.')
      setEvent(null)
    } finally {
      setLoading(false)
    }
  }, [eventId])

  useEffect(() => {
    loadEvent()
  }, [loadEvent])

  const handleOpen = async () => {
    setActioning(true)
    setError(null)
    try {
      const data = await openEvent(eventId)
      if (data) setEvent(data)
    } catch (e) {
      setError(e.message || '오픈 처리에 실패했습니다.')
    } finally {
      setActioning(false)
    }
  }

  const handleClose = async () => {
    setActioning(true)
    setError(null)
    try {
      const data = await closeEvent(eventId)
      if (data) setEvent(data)
    } catch (e) {
      setError(e.message || '종료 처리에 실패했습니다.')
    } finally {
      setActioning(false)
    }
  }

  if (loading) {
    return (
      <>
        <h2 className="h5 fw-semibold mb-1">이벤트 정보</h2>
        <p className="text-muted mb-4">불러오는 중...</p>
      </>
    )
  }

  if (error && !event) {
    return (
      <>
        <h2 className="h5 fw-semibold mb-1">이벤트 정보</h2>
        <p className="text-muted mb-4">이벤트를 열기/종료하고 각 메뉴로 이동할 수 있습니다.</p>
        <div className="alert alert-danger">{error}</div>
      </>
    )
  }

  const ev = event || { eventId: eventId, name: '-', description: '-', createdAt: null, status: 'DRAFT' }

  return (
    <>
      <h2 className="h5 fw-semibold mb-1">이벤트 정보</h2>
      <p className="text-muted mb-4">이벤트를 열기/종료하고 각 메뉴로 이동할 수 있습니다.</p>
      {error && (
        <div className="alert alert-danger alert-dismissible fade show" role="alert">
          {error}
          <button type="button" className="btn-close" onClick={() => setError(null)} aria-label="닫기" />
        </div>
      )}

      <div className="card border rounded-3 mb-4">
        <div className="card-body">
          <div className="d-flex justify-content-between align-items-start flex-wrap gap-2">
            <div>
              <h2 className="h4 mb-2">{ev.name}</h2>
              <p className="text-muted small mb-1">이벤트 ID: {ev.eventId}</p>
              <p className="mb-2">{ev.description}</p>
              <p className="text-muted small mb-0">생성일: {toDateStr(ev.createdAt)}</p>
            </div>
            <div className="d-flex align-items-center gap-2">
              <span className="badge bg-light text-dark border">{ev.status}</span>
              <button
                type="button"
                className="btn btn-primary btn-sm"
                onClick={handleOpen}
                disabled={actioning || ev.status === 'OPEN'}
              >
                이벤트 오픈
              </button>
              <button
                type="button"
                className="btn btn-outline-secondary btn-sm"
                onClick={handleClose}
                disabled={actioning || ev.status === 'CLOSED'}
              >
                이벤트 종료
              </button>
            </div>
          </div>
        </div>
      </div>

      <h2 className="h5 fw-semibold mb-3">이벤트 설정</h2>
      <div className="row g-3">
        {quickLinks.map((link) => (
          <div key={link.to} className="col-md-6">
            <Link
              to={`/admin/events/${eventId}/${link.to}`}
              className="card quick-link-card p-3 text-decoration-none text-body"
            >
              <div className="d-flex align-items-start gap-2">
                <span className="text-primary">
                  {link.icon === 'seat' && (
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor"><path d="M4 18v3c0 .55.45 1 1 1h14c.55 0 1-.45 1-1v-3c0-.55-.45-1-1-1H5c-.55 0-1 .45-1 1zm15-11V6c0-1.1-.9-2-2-2H7c-1.1 0-2 .9-2 2v1c-1.1 0-2 .9-2 2v3c0 1.1.9 2 2 2h10c1.1 0 2-.9 2-2v-3c0-1.1-.9-2-2-2zM7 6h10v1H7V6z"/></svg>
                  )}
                  {link.icon === 'list' && (
                    <svg width="24" height="24" viewBox="0 0 24 24" fill="currentColor"><path d="M3 13h2v-2H3v2zm0 4h2v-2H3v2zm0-8h2V7H3v2zm4 4h14v-2H7v2zm0 4h14v-2H7v2zM7 7v2h14V7H7z"/></svg>
                  )}
                </span>
                <div>
                  <h3 className="h6 mb-1">{link.title}</h3>
                  <p className="text-muted small mb-0">{link.desc}</p>
                </div>
              </div>
            </Link>
          </div>
        ))}
      </div>

      <div className="card border-primary border-opacity-25 bg-primary bg-opacity-10 mt-4">
        <div className="card-body d-flex align-items-center justify-content-between flex-wrap gap-3">
          <div>
            <h3 className="h6 fw-bold text-primary mb-1">🚦 길목 대기열 플랫폼 콘솔 연동</h3>
            <p className="small text-muted mb-0">
              실시간 트래픽 모니터링, 동적 RPS 정책 설정, AI 부하 분석 및 추천 기능은 대기열 플랫폼 콘솔(포트 3031)에서 관리합니다.
            </p>
          </div>
          <a
            href={`http://localhost:3031/events/${eventId}/policy`}
            className="btn btn-primary btn-sm d-flex align-items-center gap-1 text-nowrap"
          >
            <span>대기열/정책 콘솔 이동</span>
            <span>→</span>
          </a>
        </div>
      </div>
    </>
  )
}
