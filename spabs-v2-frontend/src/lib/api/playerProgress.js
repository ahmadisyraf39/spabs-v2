import { apiFetch } from './client'

export function createPlayerModuleProgress(data) {
  return apiFetch('/player-progress', { method: 'POST', body: data })
}

export function saveBulkPlayerModuleProgress(data) {
  return apiFetch('/player-progress/bulk', { method: 'POST', body: data })
}

export function getPlayerTeamProgress(playerId, teamId) {
  return apiFetch(`/player-progress/summary?playerId=${playerId}&teamId=${teamId}`)
}

export function deletePlayerModuleProgress(id) {
  return apiFetch(`/player-progress/${id}`, { method: 'DELETE' })
}
