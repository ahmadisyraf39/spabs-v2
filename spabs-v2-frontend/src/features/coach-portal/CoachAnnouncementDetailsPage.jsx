import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getAnnouncement } from '../../lib/api/announcements'
import { getTeam } from '../../lib/api/teams'
import { getUsers } from '../../lib/api/users'

export default function CoachAnnouncementDetailsPage() {
  const { id } = useParams()
  const [announcement, setAnnouncement] = useState(null)
  const [teamName, setTeamName] = useState(null)
  const [authorName, setAuthorName] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    let cancelled = false

    async function load() {
      setLoading(true)
      setError(null)
      try {
        const a = await getAnnouncement(id)
        if (cancelled) return
        setAnnouncement(a)
        const [team, users] = await Promise.all([a.teamId ? getTeam(a.teamId) : null, getUsers()])
        if (cancelled) return
        setTeamName(team?.name ?? null)
        setAuthorName(
          users.find((u) => u.id === a.createdByUserId)?.fullName ?? `User #${a.createdByUserId}`,
        )
      } catch (err) {
        if (!cancelled) setError(err.message ?? 'Unable to load announcement.')
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

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">{announcement.title}</h1>
        <Link to="/dashboard/coach/announcements" className="btn btn-ghost btn-sm">
          Back
        </Link>
      </div>

      <div className="card bg-base-100 shadow-md">
        <div className="card-body">
          <h2 className="card-title text-lg">Announcement info</h2>
          <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-3 text-sm">
            <dt className="text-base-content/60">Team</dt>
            <dd>
              {announcement.teamId ? (
                <Link to={`/dashboard/coach/teams/${announcement.teamId}`} className="link link-primary">
                  {teamName}
                </Link>
              ) : (
                <span className="badge badge-ghost">Academy-wide</span>
              )}
            </dd>

            <dt className="text-base-content/60">Posted by</dt>
            <dd>{authorName}</dd>

            <dt className="text-base-content/60">Posted</dt>
            <dd>{new Date(announcement.createdAt).toLocaleString()}</dd>

            <dt className="text-base-content/60">Last updated</dt>
            <dd>{new Date(announcement.updatedAt).toLocaleString()}</dd>
          </dl>
        </div>
      </div>

      <div className="card bg-base-100 shadow-md">
        <div className="card-body">
          <h2 className="card-title text-lg">Content</h2>
          <p className="whitespace-pre-wrap text-sm">{announcement.content}</p>
        </div>
      </div>
    </div>
  )
}
