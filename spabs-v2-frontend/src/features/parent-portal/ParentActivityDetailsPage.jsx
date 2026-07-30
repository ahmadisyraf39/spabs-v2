import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getActivity } from '../../lib/api/activities'
import { getAttendancesByActivity } from '../../lib/api/attendances'
import { getMyParentProfile } from '../../lib/api/profiles'
import { getTeam } from '../../lib/api/teams'
import { formatDateTime } from '../../lib/dateTimeUtils'
import { getPlayersForParent } from '../../lib/playerRelations'
import { getTeamHistoryForPlayer } from '../../lib/teamRelations'
import { attendanceStatusBadgeClass } from '../activities/activityEnums'

export default function ParentActivityDetailsPage() {
  const { id } = useParams()
  const [activity, setActivity] = useState(null)
  const [team, setTeam] = useState(null)
  const [attendanceEntries, setAttendanceEntries] = useState([])
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
        const [t, parent] = await Promise.all([getTeam(a.teamId), getMyParentProfile()])
        if (cancelled) return
        const kids = await getPlayersForParent(parent.id)
        if (cancelled) return
        const [teamHistories, records] = await Promise.all([
          Promise.all(kids.map((k) => getTeamHistoryForPlayer(k.id))),
          getAttendancesByActivity(id),
        ])
        if (cancelled) return

        const matchedChildren = kids.filter((k, i) =>
          teamHistories[i].some((th) => th.id === a.teamId && th.status === 'ACTIVE'),
        )
        const recordByPlayerId = new Map(records.map((r) => [r.playerId, r]))
        setAttendanceEntries(
          matchedChildren.map((k) => ({
            name: k.fullName,
            status: recordByPlayerId.get(k.id)?.status ?? null,
            notes: recordByPlayerId.get(k.id)?.notes ?? null,
          })),
        )
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

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">{displayTitle}</h1>
        <Link to="/dashboard/parent/activities" className="btn btn-ghost btn-sm">
          Back
        </Link>
      </div>

      <div className="card bg-base-100 shadow-md">
        <div className="card-body">
          <h2 className="card-title text-lg">Activity info</h2>
          <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-3 text-sm">
            <dt className="text-base-content/60">Team</dt>
            <dd>{team.name}</dd>

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

      <div className="card bg-base-100 shadow-md">
        <div className="card-body">
          <h2 className="card-title text-lg">Attendance</h2>
          {attendanceEntries.length === 0 ? (
            <p className="text-base-content/60 text-sm">No child of yours is on this team.</p>
          ) : (
            <ul className="flex flex-col divide-y">
              {attendanceEntries.map((e) => (
                <li key={e.name} className="flex items-center justify-between gap-2 py-2 first:pt-0 last:pb-0">
                  <span className="font-medium">{e.name}</span>
                  {e.status ? (
                    <span className={`badge badge-sm ${attendanceStatusBadgeClass(e.status)}`}>
                      {e.status}
                    </span>
                  ) : (
                    <span className="text-base-content/60 text-xs">Not recorded</span>
                  )}
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>
    </div>
  )
}
