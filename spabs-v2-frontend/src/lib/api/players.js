import { apiFetch } from './client'

export function getPlayers() {
  return apiFetch('/players')
}

export function getPlayer(id) {
  return apiFetch(`/players/${id}`)
}

export function createPlayer(data) {
  return apiFetch('/players', { method: 'POST', body: data })
}

export function updatePlayer(id, data) {
  return apiFetch(`/players/${id}`, { method: 'PUT', body: data })
}

export function deletePlayer(id) {
  return apiFetch(`/players/${id}`, { method: 'DELETE' })
}
