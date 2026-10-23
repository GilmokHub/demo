import { api } from './client'

export function getMe() {
  return api.get('/users/me')
}

export function getDashboard() {
  return api.get('/users/me/dashboard')
}

