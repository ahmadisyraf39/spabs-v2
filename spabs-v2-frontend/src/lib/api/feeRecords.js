import { apiFetch } from './client'

export function getFeeRecordsByTeam(teamId) {
  return apiFetch(`/fee-records?teamId=${teamId}`)
}

export function getFeeRecordsByPlayer(playerId) {
  return apiFetch(`/fee-records?playerId=${playerId}`)
}

export function createFeeRecord(data) {
  return apiFetch('/fee-records', { method: 'POST', body: data })
}

export function payFeeRecord(id) {
  return apiFetch(`/fee-records/${id}/pay`, { method: 'POST' })
}

export function getFeeSummaryForTeam(teamId) {
  return apiFetch(`/fee-records/team/${teamId}/summary`)
}

export function deleteFeeRecord(id) {
  return apiFetch(`/fee-records/${id}`, { method: 'DELETE' })
}
