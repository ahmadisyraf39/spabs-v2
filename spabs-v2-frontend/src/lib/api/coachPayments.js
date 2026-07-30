import { apiFetch } from './client'

export function getCoachPayments() {
  return apiFetch('/coach-payments')
}

export function getCoachPayment(id) {
  return apiFetch(`/coach-payments/${id}`)
}

export function getCoachPaymentsByCoach(coachId) {
  return apiFetch(`/coach-payments?coachId=${coachId}`)
}

export function createCoachPayment(data) {
  return apiFetch('/coach-payments', { method: 'POST', body: data })
}

export function updateCoachPayment(id, data) {
  return apiFetch(`/coach-payments/${id}`, { method: 'PUT', body: data })
}

export function payCoachPayment(id) {
  return apiFetch(`/coach-payments/${id}/pay`, { method: 'POST' })
}

export function deleteCoachPayment(id) {
  return apiFetch(`/coach-payments/${id}`, { method: 'DELETE' })
}
