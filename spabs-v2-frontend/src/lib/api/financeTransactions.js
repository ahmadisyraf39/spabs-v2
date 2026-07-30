import { apiFetch } from './client'

export function getFinanceTransactions() {
  return apiFetch('/finance-transactions')
}

export function getFinanceTransaction(id) {
  return apiFetch(`/finance-transactions/${id}`)
}

export function createFinanceTransaction(data) {
  return apiFetch('/finance-transactions', { method: 'POST', body: data })
}

export function createActivityFinanceEntry(data) {
  return apiFetch('/finance-transactions/activity-entry', { method: 'POST', body: data })
}

export function updateFinanceTransaction(id, data) {
  return apiFetch(`/finance-transactions/${id}`, { method: 'PUT', body: data })
}

export function deleteFinanceTransaction(id) {
  return apiFetch(`/finance-transactions/${id}`, { method: 'DELETE' })
}

export function getFinanceSummary(startDate, endDate) {
  const params = new URLSearchParams()
  if (startDate) params.set('startDate', startDate)
  if (endDate) params.set('endDate', endDate)
  const query = params.toString()
  return apiFetch(`/finance-transactions/summary${query ? `?${query}` : ''}`)
}
