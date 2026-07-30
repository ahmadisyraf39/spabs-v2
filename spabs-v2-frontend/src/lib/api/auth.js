import { apiFetch } from './client'

export function login({ email, password }) {
  return apiFetch('/auth/login', { method: 'POST', body: { email, password }, auth: false })
}

export function getMe() {
  return apiFetch('/auth/me')
}

export function forgotPassword({ email }) {
  return apiFetch('/auth/forgot-password', { method: 'POST', body: { email }, auth: false })
}

export function resetPassword({ token, newPassword }) {
  return apiFetch('/auth/reset-password', { method: 'POST', body: { token, newPassword }, auth: false })
}

export function changePassword({ currentPassword, newPassword }) {
  return apiFetch('/auth/change-password', { method: 'PUT', body: { currentPassword, newPassword } })
}
