import { apiFetch } from './client'

export function getAnnouncements() {
  return apiFetch('/announcements')
}

export function getAnnouncement(id) {
  return apiFetch(`/announcements/${id}`)
}

export function getMyAnnouncements() {
  return apiFetch('/announcements/mine')
}

export function getAnnouncementsByTeam(teamId) {
  return apiFetch(`/announcements?teamId=${teamId}`)
}

export function createAnnouncement(data) {
  return apiFetch('/announcements', { method: 'POST', body: data })
}

export function updateAnnouncement(id, data) {
  return apiFetch(`/announcements/${id}`, { method: 'PUT', body: data })
}

export function deleteAnnouncement(id) {
  return apiFetch(`/announcements/${id}`, { method: 'DELETE' })
}
