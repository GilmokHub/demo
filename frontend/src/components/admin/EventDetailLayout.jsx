import { Outlet, useParams, NavLink, useLocation, Link } from 'react-router-dom'
import { useEffect, useMemo, useState } from 'react'
import { getEvent } from '../../api/events.js'

const eventTabs = [
  { to: 'seats', label: '좌석 관리' },
  { to: 'reservations', label: '예약 현황' },
]

export default function EventDetailLayout() {
  const { eventId } = useParams()
  const location = useLocation()
  const [eventName, setEventName] = useState('')

  useEffect(() => {
    if (!eventId) return
    let cancelled = false
    setEventName('')
    getEvent(eventId)
      .then((res) => {
        if (cancelled) return
        const ev = res?.data ?? res
        setEventName(ev?.name ?? '')
      })
      .catch(() => {
        if (!cancelled) setEventName('')
      })
    return () => {
      cancelled = true
    }
  }, [eventId])

  const currentMenuLabel = useMemo(() => {
    if (!eventId) return ''
    const prefix = `/admin/events/${eventId}/`
    const rest = location.pathname.startsWith(prefix) ? location.pathname.slice(prefix.length) : ''
    const segment = (rest.split('/')[0] || '').trim()
    const found = eventTabs.find((t) => t.to === segment)
    return found?.label ?? ''
  }, [eventId, location.pathname])

  return (
    <>
      <div className="d-flex align-items-start justify-content-between flex-wrap gap-2 mb-3">
        <div>
          <h1 className="h4 fw-bold mb-0">{eventName || '-'}</h1>
        </div>
        <div className="d-flex align-items-center gap-2">
          <a
            href={`http://localhost:3031/events/${eventId}/policy`}
            className="btn btn-outline-primary btn-sm d-flex align-items-center gap-1"
          >
            <span>🚦 대기열/정책 관리 (3031)</span>
            <span className="small">→</span>
          </a>
          {currentMenuLabel && (
            <Link to={`/admin/events/${eventId}`} className="btn btn-outline-secondary btn-sm">
              돌아가기
            </Link>
          )}
        </div>
      </div>
      <nav className="nav nav-tabs mb-3 flex-nowrap overflow-auto">
        {eventTabs.map(({ to, label }) => (
          <NavLink
            key={to}
            to={`/admin/events/${eventId}/${to}`}
            className={({ isActive }) =>
              `nav-link ${isActive ? 'active' : ''} text-nowrap`
            }
          >
            {label}
          </NavLink>
        ))}
      </nav>
      <Outlet />
    </>
  )
}
