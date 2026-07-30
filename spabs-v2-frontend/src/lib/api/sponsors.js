import { apiFetch } from './client'

export function getSponsors() {
  return apiFetch('/sponsors')
}

export function getSponsor(id) {
  return apiFetch(`/sponsors/${id}`)
}

export function createSponsor(data) {
  return apiFetch('/sponsors', { method: 'POST', body: data })
}

export function updateSponsor(id, data) {
  return apiFetch(`/sponsors/${id}`, { method: 'PUT', body: data })
}

export function deleteSponsor(id) {
  return apiFetch(`/sponsors/${id}`, { method: 'DELETE' })
}
