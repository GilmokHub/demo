import { api } from './client'

export function getEvents() {
  return api.get('/admin/events')
}

export function getEvent(eventId) {
  return api.get(`/admin/events/${eventId}`)
}

export function createEvent(body) {
  return api.post('/admin/events', body)
}

export function openEvent(eventId) {
  return api.post(`/admin/events/${eventId}/open`, {})
}

export function closeEvent(eventId) {
  return api.post(`/admin/events/${eventId}/close`, {})
}

// 사용자 예매용 API
export function getOpenEvents() {
  return api.get('/events')
}

export function getEventSeats(eventId) {
  return api.get(`/events/${eventId}/seats`)
}

// 관리자 좌석 관리 API
export function createEventSeat(eventId, body) {
  return api.post(`/admin/events/${eventId}/seats`, body)
}

export function initEventSeatsRedis(eventId) {
  return api.post(`/admin/events/${eventId}/seats/init-redis`, {})
}
