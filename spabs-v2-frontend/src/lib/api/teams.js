import { apiFetch } from './client'

export function getTeams() {
  return apiFetch('/teams')
}

export function getTeam(id) {
  return apiFetch(`/teams/${id}`)
}

export function createTeam(data) {
  return apiFetch('/teams', { method: 'POST', body: data })
}

export function updateTeam(id, data) {
  return apiFetch(`/teams/${id}`, { method: 'PUT', body: data })
}

export function deleteTeam(id) {
  return apiFetch(`/teams/${id}`, { method: 'DELETE' })
}
