import { apiFetch } from './client'

export function getPlayerTeams() {
  return apiFetch('/player-teams')
}

export function getPlayerTeamsByPlayer(playerId) {
  return apiFetch(`/player-teams?playerId=${playerId}`)
}

export function getPlayerTeamsByTeam(teamId) {
  return apiFetch(`/player-teams?teamId=${teamId}`)
}

export function createPlayerTeam(data) {
  return apiFetch('/player-teams', { method: 'POST', body: data })
}

export function updatePlayerTeam(id, data) {
  return apiFetch(`/player-teams/${id}`, { method: 'PUT', body: data })
}

export function deletePlayerTeam(id) {
  return apiFetch(`/player-teams/${id}`, { method: 'DELETE' })
}
