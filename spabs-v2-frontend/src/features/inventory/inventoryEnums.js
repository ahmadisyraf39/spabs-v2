export const INVENTORY_CATEGORIES = ['BALL', 'BIB', 'CONE', 'AGILITY_LADDER', 'OTHER']

export const INVENTORY_TRANSACTION_TYPES = ['INITIAL_STOCK', 'PURCHASE', 'ADJUSTMENT', 'DAMAGE', 'LOST']

const TRANSACTION_TYPE_BADGE_CLASS = {
  INITIAL_STOCK: 'badge-success',
  PURCHASE: 'badge-success',
  ADJUSTMENT: 'badge-info',
  DAMAGE: 'badge-error',
  LOST: 'badge-error',
}

export function transactionTypeBadgeClass(type) {
  return TRANSACTION_TYPE_BADGE_CLASS[type] ?? 'badge-ghost'
}

export function quantityEffectLabel(type, quantity) {
  if (type === 'ADJUSTMENT') return `= ${quantity}`
  if (type === 'DAMAGE' || type === 'LOST') return `−${quantity}`
  return `+${quantity}`
}
