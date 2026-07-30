export function formatDateTime(value) {
  if (!value) return null
  return new Date(value).toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'short' })
}

export function toDateTimeLocalValue(value) {
  return value ? value.slice(0, 16) : ''
}

export function getActivityTimeframe(startAt) {
  const activityDate = startAt.slice(0, 10)
  const today = new Date().toISOString().slice(0, 10)
  if (activityDate < today) return 'past'
  if (activityDate > today) return 'future'
  return 'today'
}
