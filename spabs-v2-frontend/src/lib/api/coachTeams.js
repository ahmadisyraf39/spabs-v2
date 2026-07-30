import { apiFetch } from './client'

export function getCoachTeams() {
  return apiFetch('/coach-teams')
}

export function getCoachTeamsByCoach(coachId) {
  return apiFetch(`/coach-teams?coachId=${coachId}`)
}

export function getCoachTeamsByTeam(teamId) {
  return apiFetch(`/coach-teams?teamId=${teamId}`)
}

export function createCoachTeam(data) {
  return apiFetch('/coach-teams', { method: 'POST', body: data })
}

export function updateCoachTeam(id, data) {
  return apiFetch(`/coach-teams/${id}`, { method: 'PUT', body: data })
}

export function deleteCoachTeam(id) {
  return apiFetch(`/coach-teams/${id}`, { method: 'DELETE' })
}
