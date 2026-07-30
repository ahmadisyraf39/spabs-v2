import { apiFetch } from './client'

export function getAdmins() {
  return apiFetch('/admins')
}
export function createAdmin(data) {
  return apiFetch('/admins', { method: 'POST', body: data })
}
export function updateAdmin(id, data) {
  return apiFetch(`/admins/${id}`, { method: 'PUT', body: data })
}
export function deleteAdmin(id) {
  return apiFetch(`/admins/${id}`, { method: 'DELETE' })
}

export function getCoaches() {
  return apiFetch('/coaches')
}
export function getMyCoachProfile() {
  return apiFetch('/coaches/me')
}
export function createCoach(data) {
  return apiFetch('/coaches', { method: 'POST', body: data })
}
export function updateCoach(id, data) {
  return apiFetch(`/coaches/${id}`, { method: 'PUT', body: data })
}
export function deleteCoach(id) {
  return apiFetch(`/coaches/${id}`, { method: 'DELETE' })
}

export function getParents() {
  return apiFetch('/parents')
}
export function getMyParentProfile() {
  return apiFetch('/parents/me')
}
export function createParent(data) {
  return apiFetch('/parents', { method: 'POST', body: data })
}
export function updateParent(id, data) {
  return apiFetch(`/parents/${id}`, { method: 'PUT', body: data })
}
export function updateMyParentProfile(data) {
  return apiFetch('/parents/me', { method: 'PUT', body: data })
}
export function deleteParent(id) {
  return apiFetch(`/parents/${id}`, { method: 'DELETE' })
}
