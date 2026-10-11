import { api } from './client'

/**
 * 입장 토큰(X-Admission-Token) 헤더 빌더
 */
function buildAdmissionHeader(admissionToken) {
  if (!admissionToken) return {}
  const token = admissionToken.startsWith('Bearer ') ? admissionToken : `Bearer ${admissionToken}`
  return { 'X-Admission-Token': token }
}

/**
 * 좌석 선점 및 예약 생성
 */
export function createReservation({ eventId, seatId, quantity }, admissionToken) {
  return api.post(
    '/reservations',
    { eventId: Number(eventId), seatId: Number(seatId), quantity: Number(quantity) },
    { headers: buildAdmissionHeader(admissionToken) }
  )
}

/**
 * 결제 완료 후 예약 확정
 */
export function confirmReservation(reservationCode, admissionToken) {
  return api.post(
    `/reservations/${reservationCode}/confirm`,
    undefined,
    { headers: buildAdmissionHeader(admissionToken) }
  )
}

/**
 * 예약 취소 (선점 취소 또는 확정 후 취소)
 */
export function cancelReservation(reservationCode, admissionToken) {
  return api.delete(
    `/reservations/${reservationCode}`,
    { headers: buildAdmissionHeader(admissionToken) }
  )
}

/**
 * 예약 상세 단건 조회
 */
export function getReservation(reservationCode) {
  return api.get(`/reservations/${reservationCode}`)
}

/**
 * 로그인한 사용자의 예약 내역 목록 조회
 */
export function getMyReservations() {
  return api.get('/reservations/my')
}
