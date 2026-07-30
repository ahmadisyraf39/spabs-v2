import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { getMyAnnouncements } from '../../lib/api/announcements'
import { getTeams } from '../../lib/api/teams'

const DASHBOARD_LIMIT = 5

export default function MyAnnouncements({ announcementPath }) {
  const [announcements, setAnnouncements] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    let cancelled = false

    async function load() {
      try {
        const [announcementList, teams] = await Promise.all([getMyAnnouncements(), getTeams()])
        if (cancelled) return
        const teamsById = new Map(teams.map((t) => [t.id, t]))
        setAnnouncements(
          announcementList
            .map((a) => ({
              ...a,
              teamName: a.teamId ? (teamsById.get(a.teamId)?.name ?? `Team #${a.teamId}`) : null,
            }))
            .sort((a, b) => b.createdAt.localeCompare(a.createdAt))
            .slice(0, DASHBOARD_LIMIT),
        )
      } catch (err) {
        if (!cancelled) setError(err.message ?? 'Unable to load announcements.')
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
        <h2 className="card-title text-lg">Recent announcements</h2>

        {error && (
          <div role="alert" className="alert alert-error text-sm">
            <span>{error}</span>
          </div>
        )}

        {loading ? (
          <div className="flex justify-center py-6">
            <span className="loading loading-spinner text-primary" />
          </div>
        ) : announcements.length === 0 ? (
          <p className="text-base-content/60 text-sm">No announcements yet.</p>
        ) : (
          <div className="flex flex-col divide-y">
            {announcements.map((a) => (
              <div key={a.id} className="py-2 first:pt-0 last:pb-0">
                <div className="flex items-center justify-between gap-2">
                  {announcementPath ? (
                    <Link to={announcementPath(a)} className="link link-primary text-sm font-medium">
                      {a.title}
                    </Link>
                  ) : (
                    <span className="font-medium">{a.title}</span>
                  )}
                  <span className="badge badge-ghost badge-xs shrink-0">{a.teamName ?? 'Academy-wide'}</span>
                </div>
                <span className="text-base-content/60 text-xs">
                  {new Date(a.createdAt).toLocaleDateString()}
                </span>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  )
}
