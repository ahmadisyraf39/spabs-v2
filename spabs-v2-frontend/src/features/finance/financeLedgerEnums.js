export const COACH_PAYMENT_TYPES = ['SALARY', 'PER_SESSION', 'BONUS']

export const FINANCE_TYPES = ['INCOME', 'EXPENSE']

export const INCOME_CATEGORIES = ['MONTHLY_FEE', 'REGISTRATION_FEE', 'SPONSORSHIP', 'PRIZE_MONEY']
export const EXPENSE_CATEGORIES = [
  'INVENTORY_PURCHASE',
  'FIELD_RENTAL',
  'REFEREE',
  'TOURNAMENT_REGISTRATION',
  'LEAGUE_MATCH_FEE',
  'OTHER',
  'COACH_SALARY',
  'COACH_PER_SESSION',
  'COACH_BONUS',
]
export const FINANCE_CATEGORIES = [...INCOME_CATEGORIES, ...EXPENSE_CATEGORIES]

export function categoriesForType(financeType) {
  return financeType === 'INCOME' ? INCOME_CATEGORIES : EXPENSE_CATEGORIES
}

export function financeTypeBadgeClass(type) {
  return type === 'INCOME' ? 'badge-soft badge-success' : 'badge-soft badge-error'
}

export function coachPaymentTypeBadgeClass() {
  return 'badge-ghost'
}

export function paymentStatusBadgeClass(status) {
  return status === 'PAID' ? 'badge-soft badge-success' : 'badge-soft badge-warning'
}

const REFERENCE_TYPE_LABEL = {
  ACTIVITY: 'Activity',
  COACH_PAYMENT: 'Coach payment',
  FEE_RECORD: 'Fee record',
  INVENTORY: 'Inventory item',
  SPONSORSHIP: 'Sponsorship',
}

export function financeReferenceLabel(referenceType, referenceId) {
  if (!referenceType) return null
  return `${REFERENCE_TYPE_LABEL[referenceType] ?? referenceType} #${referenceId}`
}
