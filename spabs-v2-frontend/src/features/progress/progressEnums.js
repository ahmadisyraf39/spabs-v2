export const MODULE_PROGRESS_STATUSES = [
  'NOT_STARTED',
  'STARTED',
  'IN_PROGRESS',
  'ALMOST_COMPLETE',
  'COMPLETED',
]

const STATUS_PERCENTAGE = {
  NOT_STARTED: 0,
  STARTED: 25,
  IN_PROGRESS: 50,
  ALMOST_COMPLETE: 75,
  COMPLETED: 100,
}

export function percentageForStatus(status) {
  return STATUS_PERCENTAGE[status] ?? 0
}

export function moduleProgressColor(percentage) {
  if (percentage < 25) return 'progress-error'
  if (percentage < 50) return 'progress-warning'
  if (percentage < 75) return 'progress-info'
  return 'progress-success'
}

const MODULE_STATUS_SELECT_CLASS = {
  NOT_STARTED: 'select-error bg-error/10',
  STARTED: 'select-warning bg-warning/10',
  IN_PROGRESS: 'select-info bg-info/10',
  ALMOST_COMPLETE: 'select-success bg-success/10',
  COMPLETED: 'select-success bg-success/10',
}

export function moduleStatusSelectClass(status) {
  return MODULE_STATUS_SELECT_CLASS[status] ?? ''
}

const MODULE_STATUS_BADGE_CLASS = {
  NOT_STARTED: 'badge-soft badge-error',
  STARTED: 'badge-soft badge-warning',
  IN_PROGRESS: 'badge-soft badge-info',
  ALMOST_COMPLETE: 'badge-soft badge-success',
  COMPLETED: 'badge-soft badge-success',
}

export function moduleStatusBadgeClass(status) {
  return MODULE_STATUS_BADGE_CLASS[status] ?? ''
}
