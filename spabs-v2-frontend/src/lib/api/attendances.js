import { apiFetch } from './client'

export function getAttendancesByActivity(activityId) {
  return apiFetch(`/attendances?activityId=${activityId}`)
}

export function getAttendancesByPlayer(playerId) {
  return apiFetch(`/attendances?playerId=${playerId}`)
}

export function getAttendance(id) {
  return apiFetch(`/attendances/${id}`)
}

export function createAttendance(data) {
  return apiFetch('/attendances', { method: 'POST', body: data })
}

export function saveBulkAttendance(data) {
  return apiFetch('/attendances/bulk', { method: 'POST', body: data })
}

export function updateAttendance(id, data) {
  return apiFetch(`/attendances/${id}`, { method: 'PUT', body: data })
}

export function deleteAttendance(id) {
  return apiFetch(`/attendances/${id}`, { method: 'DELETE' })
}

export function getAttendanceSummary(playerId, teamId) {
  return apiFetch(`/attendances/summary?playerId=${playerId}&teamId=${teamId}`)
}
