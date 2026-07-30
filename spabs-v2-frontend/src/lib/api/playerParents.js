import { apiFetch } from './client'

export function getPlayerParents() {
  return apiFetch('/player-parents')
}

export function getPlayerParentsByPlayer(playerId) {
  return apiFetch(`/player-parents?playerId=${playerId}`)
}

export function getPlayerParentsByParent(parentId) {
  return apiFetch(`/player-parents?parentId=${parentId}`)
}

export function createPlayerParent(data) {
  return apiFetch('/player-parents', { method: 'POST', body: data })
}

export function deletePlayerParent(id) {
  return apiFetch(`/player-parents/${id}`, { method: 'DELETE' })
}
