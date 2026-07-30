export const SPONSORSHIP_TYPES = ['CASH', 'EQUIPMENT', 'JERSEY', 'FOOD', 'SERVICE', 'OTHER']

export function sponsorshipTypeBadgeClass() {
  return 'badge-ghost'
}

export function sponsorshipStatus(record) {
  const today = new Date().toISOString().slice(0, 10)
  if (today < record.startDate) return 'UPCOMING'
  if (today > record.endDate) return 'EXPIRED'
  return 'ACTIVE'
}

const SPONSORSHIP_STATUS_BADGE_CLASS = {
  ACTIVE: 'badge-soft badge-success',
  UPCOMING: 'badge-soft badge-info',
  EXPIRED: 'badge-ghost',
}

export function sponsorshipStatusBadgeClass(status) {
  return SPONSORSHIP_STATUS_BADGE_CLASS[status] ?? 'badge-ghost'
}
