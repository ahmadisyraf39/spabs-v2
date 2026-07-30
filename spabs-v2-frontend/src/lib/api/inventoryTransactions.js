import { apiFetch } from './client'

export function getInventoryTransactionsByInventory(inventoryId) {
  return apiFetch(`/inventory-transactions?inventoryId=${inventoryId}`)
}

export function createInventoryTransaction(data) {
  return apiFetch('/inventory-transactions', { method: 'POST', body: data })
}

export function deleteInventoryTransaction(id) {
  return apiFetch(`/inventory-transactions/${id}`, { method: 'DELETE' })
}
