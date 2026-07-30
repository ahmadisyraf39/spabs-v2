import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getActivity } from '../../lib/api/activities'
import { getTeam } from '../../lib/api/teams'
import { getActivityTimeframe, formatDateTime } from '../../lib/dateTimeUtils'
import AttendanceSection from '../activities/AttendanceSection'

export default function CoachAttendancePage() {
  const { id } = useParams()
  const [activity, setActivity] = useState(null)
  const [team, setTeam] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    let cancelled = false

    async function load() {
      setLoading(true)
      setError(null)
      try {
        const a = await getActivity(id)
        if (cancelled) return
        const t = await getTeam(a.teamId)
        if (cancelled) return
        setActivity(a)
        setTeam(t)
      } catch (err) {
        if (!cancelled) setError(err.message ?? 'Unable to load this activity.')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    load()
    return () => {
      cancelled = true
    }
  }, [id])

  if (loading) {
    return (
      <div className="flex justify-center py-10">
        <span className="loading loading-spinner loading-lg text-primary" />
      </div>
    )
  }

  if (error) {
    return (
      <div className="mx-auto max-w-lg">
        <div role="alert" className="alert alert-error text-sm">
          <span>{error}</span>
        </div>
      </div>
    )
  }

  const displayTitle = activity.title?.trim() ? activity.title : activity.type
  const timeframe = getActivityTimeframe(activity.startAt)

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">{displayTitle}</h1>
        <Link to="/dashboard/coach/activities" className="btn btn-ghost btn-sm">
          Back
        </Link>
      </div>

      <div className="card bg-base-100 shadow-md">
        <div className="card-body">
          <h2 className="card-title text-lg">Activity info</h2>
          <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-3 text-sm">
            <dt className="text-base-content/60">Team</dt>
            <dd>
              <Link to={`/dashboard/coach/teams/${team.id}`} className="link link-primary text-sm">
                {team.name}
              </Link>
            </dd>

            <dt className="text-base-content/60">Type</dt>
            <dd>
              <span className="badge badge-ghost">{activity.type}</span>
            </dd>

            <dt className="text-base-content/60">Start</dt>
            <dd>{formatDateTime(activity.startAt)}</dd>

            <dt className="text-base-content/60">End</dt>
            <dd>{activity.endAt ? formatDateTime(activity.endAt) : '—'}</dd>

            <dt className="text-base-content/60">Location</dt>
            <dd>{activity.location ?? '—'}</dd>

            <dt className="text-base-content/60">Description</dt>
            <dd>{activity.description ?? '—'}</dd>
          </dl>
        </div>
      </div>

      <AttendanceSection
        activityId={activity.id}
        teamId={activity.teamId}
        activityDate={activity.startAt.slice(0, 10)}
        editable={timeframe !== 'future'}
      />
    </div>
  )
}
