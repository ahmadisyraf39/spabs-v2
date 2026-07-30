import { apiFetch } from './client'

export function getActivities() {
  return apiFetch('/activities')
}

export function getActivitiesByTeam(teamId) {
  return apiFetch(`/activities?teamId=${teamId}`)
}

export function getActivity(id) {
  return apiFetch(`/activities/${id}`)
}

export function getMyUpcomingActivities() {
  return apiFetch('/activities/my-upcoming')
}

export function createActivity(data) {
  return apiFetch('/activities', { method: 'POST', body: data })
}

export function createRecurringActivities(data) {
  return apiFetch('/activities/recurring', { method: 'POST', body: data })
}

export function updateActivity(id, data) {
  return apiFetch(`/activities/${id}`, { method: 'PUT', body: data })
}

export function deleteActivity(id) {
  return apiFetch(`/activities/${id}`, { method: 'DELETE' })
}
