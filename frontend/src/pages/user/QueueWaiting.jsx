import { useEffect } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import { useGilmokQueue } from '@gilmok/sdk'

export default function QueueWaiting() {
  const { eventId } = useParams()
  const navigate = useNavigate()

  const { queueStatus, enterQueue, error } = useGilmokQueue({
    clientKey: 'demo-client',
    onAdmitted: ({ token, queueKey }) => {
      navigate(`/events/${eventId}/seats`, { state: { token, queueKey }, replace: true })
    }
  })

  useEffect(() => {
    if (eventId) {
      enterQueue(eventId)
    }
  }, [eventId, enterQueue])

  if (error) {
    return (
      <div className="container py-5">
        <div className="alert alert-danger text-center">
          {error}
          <div className="mt-3">
            <button className="btn btn-outline-danger btn-sm" onClick={() => navigate(`/events/${eventId}`)}>
              이벤트 페이지로 돌아가기
            </button>
          </div>
        </div>
      </div>
    )
  }

  const rank = queueStatus?.rank ?? queueStatus?.position ?? 0
  const total = queueStatus?.total ?? 0
  const eta = queueStatus?.expectedWaitSeconds ?? queueStatus?.etaSeconds ?? 0

  return (
    <div className="text-center py-5">
      <h2 className="h4 fw-bold mb-3">대기열</h2>
      <div className="spinner-border text-primary mb-3" />
      <div className="mb-2">
        <span className="badge bg-primary fs-5 px-4 py-2">
          현재 순번: {rank > 0 ? `${rank}번째` : '확인 중...'}
        </span>
      </div>
      {total > 0 && (
        <p className="text-muted small mb-1">
          전체 대기: {total.toLocaleString()}명
        </p>
      )}
      <p className="text-muted">
        예상 대기 시간: {eta > 0 ? `약 ${eta}초` : '곧 입장'}
      </p>
      <div className="alert alert-danger mt-3" style={{ maxWidth: '400px', margin: '0 auto' }}>
        <strong>주의:</strong> 페이지를 새로고침하면 대기 순번이 초기화될 수 있습니다.
      </div>
      <p className="text-muted small mt-2">
        순번이 완료되면 자동으로 좌석 선택 화면으로 이동합니다.
      </p>
    </div>
  )
}
