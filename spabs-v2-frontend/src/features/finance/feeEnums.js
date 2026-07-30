export const FEE_TYPES = ['REGISTRATION', 'MONTHLY', 'APPAREL', 'OTHER']

export const PAYMENT_STATUSES = ['PAID', 'UNPAID']

export function feeStatusBadgeClass(record) {
  if (record.status === 'PAID') return 'badge-soft badge-success'
  if (record.overdue) return 'badge-soft badge-error'
  return 'badge-soft badge-warning'
}
