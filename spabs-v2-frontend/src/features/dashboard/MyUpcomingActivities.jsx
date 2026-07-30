import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getMyUpcomingActivities } from '../../lib/api/activities'
import { getTeams } from '../../lib/api/teams'
import { formatDateTime } from '../../lib/dateTimeUtils'

export default function MyUpcomingActivities({ renderAction, activityPath, playerNames }) {
  const [activities, setActivities] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    let cancelled = false

    async function load() {
      try {
        const [activityList, teams] = await Promise.all([getMyUpcomingActivities(), getTeams()])
        if (cancelled) return
        const teamsById = new Map(teams.map((t) => [t.id, t]))
        setActivities(
          activityList
            .map((a) => ({
              ...a,
              displayTitle: a.title?.trim() ? a.title : a.type,
              teamName: teamsById.get(a.teamId)?.name ?? `Team #${a.teamId}`,
            }))
            .sort((a, b) => a.startAt.localeCompare(b.startAt)),
        )
      } catch (err) {
        if (!cancelled) setError(err.message ?? 'Unable to load upcoming activities.')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    load()
    return () => {
      cancelled = true
    }
  }, [])

  return (
    <div className="card bg-base-100 shadow-md">
      <div className="card-body">
        <h2 className="card-title text-lg">Upcoming activities</h2>

        {error && (
          <div role="alert" className="alert alert-error text-sm">
            <span>{error}</span>
          </div>
        )}

        {loading ? (
          <div className="flex justify-center py-6">
            <span className="loading loading-spinner text-primary" />
          </div>
        ) : activities.length === 0 ? (
          <p className="text-base-content/60 text-sm">Nothing scheduled.</p>
        ) : (
          <div className="flex flex-col divide-y">
            {activities.map((a) => (
              <div key={a.id} className="flex items-center justify-between gap-3 py-3 first:pt-0 last:pb-0">
                <div>
                  <div className="flex items-center gap-2">
                    {activityPath ? (
                      <Link to={activityPath(a)} className="link link-primary font-medium">
                        {a.displayTitle}
                      </Link>
                    ) : (
                      <span className="font-medium">{a.displayTitle}</span>
                    )}
                    <span className="badge badge-ghost badge-sm">{a.teamName}</span>
                    {playerNames?.(a) && (
                      <span className="badge badge-ghost badge-sm">{playerNames(a)}</span>
                    )}
                  </div>
                  <span className="text-base-content/60 text-sm">
                    {formatDateTime(a.startAt)}
                    {a.location && ` · ${a.location}`}
                  </span>
                </div>
                {renderAction?.(a)}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
