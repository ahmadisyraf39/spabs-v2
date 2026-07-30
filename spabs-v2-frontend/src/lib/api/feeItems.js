import { apiFetch } from './client'

export function getFeeItems() {
  return apiFetch('/fee-items')
}

export function getFeeItem(id) {
  return apiFetch(`/fee-items/${id}`)
}

export function createFeeItem(data) {
  return apiFetch('/fee-items', { method: 'POST', body: data })
}

export function updateFeeItem(id, data) {
  return apiFetch(`/fee-items/${id}`, { method: 'PUT', body: data })
}

export function deleteFeeItem(id) {
  return apiFetch(`/fee-items/${id}`, { method: 'DELETE' })
}
