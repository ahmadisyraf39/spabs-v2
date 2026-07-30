import { apiFetch } from './client'

export function getUsers() {
  return apiFetch('/users')
}

export function getUser(id) {
  return apiFetch(`/users/${id}`)
}

export function createUser(data) {
  return apiFetch('/users', { method: 'POST', body: data })
}

export function updateUser(id, data) {
  return apiFetch(`/users/${id}`, { method: 'PUT', body: data })
}

export function updateMyProfile(data) {
  return apiFetch('/users/me', { method: 'PUT', body: data })
}

export function deleteUser(id) {
  return apiFetch(`/users/${id}`, { method: 'DELETE' })
}

export function resetUserPassword(id, newPassword) {
  return apiFetch(`/users/${id}/reset-password`, {
    method: 'POST',
    body: newPassword ? { newPassword } : undefined,
  })
}
