import { apiFetch } from './client'

export function getClubSponsorships() {
  return apiFetch('/club-sponsorships')
}

export function getClubSponsorship(id) {
  return apiFetch(`/club-sponsorships/${id}`)
}

export function getClubSponsorshipsBySponsor(sponsorId) {
  return apiFetch(`/club-sponsorships?sponsorId=${sponsorId}`)
}

export function createClubSponsorship(data) {
  return apiFetch('/club-sponsorships', { method: 'POST', body: data })
}

export function updateClubSponsorship(id, data) {
  return apiFetch(`/club-sponsorships/${id}`, { method: 'PUT', body: data })
}

export function deleteClubSponsorship(id) {
  return apiFetch(`/club-sponsorships/${id}`, { method: 'DELETE' })
}
