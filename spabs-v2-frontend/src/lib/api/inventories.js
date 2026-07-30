import { apiFetch } from './client'

export function getInventories() {
  return apiFetch('/inventories')
}

export function getInventory(id) {
  return apiFetch(`/inventories/${id}`)
}

export function createInventory(data) {
  return apiFetch('/inventories', { method: 'POST', body: data })
}

export function updateInventory(id, data) {
  return apiFetch(`/inventories/${id}`, { method: 'PUT', body: data })
}

export function deleteInventory(id) {
  return apiFetch(`/inventories/${id}`, { method: 'DELETE' })
}
