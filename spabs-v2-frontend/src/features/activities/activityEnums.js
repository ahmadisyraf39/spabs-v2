export const ACTIVITY_TYPES = ['TRAINING', 'MATCH', 'TOURNAMENT', 'OTHER']

export const ATTENDANCE_STATUSES = ['PRESENT', 'ABSENT', 'LATE', 'EXCUSED']

const ATTENDANCE_STATUS_BADGE_CLASS = {
  PRESENT: 'badge-soft badge-success',
  LATE: 'badge-soft badge-warning',
  ABSENT: 'badge-soft badge-error',
  EXCUSED: 'badge-soft badge-info',
}

export function attendanceStatusBadgeClass(status) {
  return ATTENDANCE_STATUS_BADGE_CLASS[status] ?? 'badge-ghost'
}

const ATTENDANCE_STATUS_SELECT_CLASS = {
  PRESENT: 'select-success bg-success/10',
  LATE: 'select-warning bg-warning/10',
  ABSENT: 'select-error bg-error/10',
  EXCUSED: 'select-info bg-info/10',
}

export function attendanceStatusSelectClass(status) {
  return ATTENDANCE_STATUS_SELECT_CLASS[status] ?? ''
}

export const DAYS_OF_WEEK = [
  'MONDAY',
  'TUESDAY',
  'WEDNESDAY',
  'THURSDAY',
  'FRIDAY',
  'SATURDAY',
  'SUNDAY',
]
