// Today's date as YYYY-MM-DD in the user's local timezone.
// Don't use toISOString() for this: it returns the UTC date, which can be a day
// off from the local date (e.g. before 08:00 in UTC+8).
export function todayIso() {
  const d = new Date()
  const pad = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

export function formatDateTime(value) {
  if (!value) return null
  return new Date(value).toLocaleString(undefined, { dateStyle: 'medium', timeStyle: 'short' })
}

export function toDateTimeLocalValue(value) {
  return value ? value.slice(0, 16) : ''
}

export function getActivityTimeframe(startAt) {
  const activityDate = startAt.slice(0, 10)
  const today = todayIso()
  if (activityDate < today) return 'past'
  if (activityDate > today) return 'future'
  return 'today'
}
