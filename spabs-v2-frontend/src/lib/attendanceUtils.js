export function attendanceProgressColor(percentage) {
  if (percentage < 25) return 'progress-error'
  if (percentage < 50) return 'progress-warning'
  if (percentage < 75) return 'progress-info'
  return 'progress-success'
}
