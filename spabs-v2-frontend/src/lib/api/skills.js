import { apiFetch } from './client'

export function getSkills() {
  return apiFetch('/skills')
}

export function getSkillsByBand(ageGroup, category) {
  return apiFetch(`/skills?ageGroup=${ageGroup}&category=${category}`)
}

export function getSkill(id) {
  return apiFetch(`/skills/${id}`)
}

export function createSkill(data) {
  return apiFetch('/skills', { method: 'POST', body: data })
}

export function updateSkill(id, data) {
  return apiFetch(`/skills/${id}`, { method: 'PUT', body: data })
}

export function deleteSkill(id) {
  return apiFetch(`/skills/${id}`, { method: 'DELETE' })
}
