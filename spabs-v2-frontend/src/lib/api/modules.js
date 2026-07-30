import { apiFetch } from './client'

export function getModules() {
  return apiFetch('/modules')
}

export function getModulesBySkill(skillId) {
  return apiFetch(`/modules?skillId=${skillId}`)
}

export function getModule(id) {
  return apiFetch(`/modules/${id}`)
}

export function createModule(data) {
  return apiFetch('/modules', { method: 'POST', body: data })
}

export function updateModule(id, data) {
  return apiFetch(`/modules/${id}`, { method: 'PUT', body: data })
}

export function deleteModule(id) {
  return apiFetch(`/modules/${id}`, { method: 'DELETE' })
}
