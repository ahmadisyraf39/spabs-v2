import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { createAnnouncement, getAnnouncement, updateAnnouncement } from '../../lib/api/announcements'
import { getTeams } from '../../lib/api/teams'

export default function AnnouncementFormPage() {
  const { id } = useParams()
  const isEdit = Boolean(id)
  const navigate = useNavigate()

  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState(null)

  const [teams, setTeams] = useState([])
  const [teamId, setTeamId] = useState('')
  const [title, setTitle] = useState('')
  const [content, setContent] = useState('')

  useEffect(() => {
    let cancelled = false

    async function load() {
      try {
        const [teamList, announcement] = await Promise.all([
          getTeams(),
          isEdit ? getAnnouncement(id) : null,
        ])
        if (cancelled) return
        setTeams(teamList)
        if (announcement) {
          setTeamId(announcement.teamId ? String(announcement.teamId) : '')
          setTitle(announcement.title)
          setContent(announcement.content)
        }
      } catch (err) {
        if (!cancelled) setError(err.message ?? 'Unable to load form data.')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    load()
    return () => {
      cancelled = true
    }
  }, [id, isEdit])

  async function handleSubmit(event) {
    event.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      const payload = { teamId: teamId ? Number(teamId) : null, title, content }
      if (isEdit) {
        await updateAnnouncement(id, payload)
        navigate(`/announcements/${id}`)
      } else {
        const created = await createAnnouncement(payload)
        navigate(`/announcements/${created.id}`)
      }
    } catch (err) {
      setError(err.message ?? 'Unable to save announcement.')
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) {
    return (
      <div className="flex justify-center py-10">
        <span className="loading loading-spinner loading-lg text-primary" />
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-lg">
      <h1 className="mb-4 text-2xl font-semibold">
        {isEdit ? 'Edit announcement' : 'New announcement'}
      </h1>

      <form className="card bg-base-100 flex flex-col gap-4 p-6 shadow-md" onSubmit={handleSubmit}>
        {error && (
          <div role="alert" className="alert alert-error text-sm">
            <span>{error}</span>
          </div>
        )}

        <label className="form-control">
          <span className="label-text mb-1">Team</span>
          <select
            className="select select-bordered w-full"
            value={teamId}
            onChange={(e) => setTeamId(e.target.value)}
          >
            <option value="">Academy-wide</option>
            {teams.map((team) => (
              <option key={team.id} value={team.id}>
                {team.name}
              </option>
            ))}
          </select>
        </label>

        <label className="form-control">
          <span className="label-text mb-1">Title</span>
          <input
            type="text"
            className="input input-bordered w-full"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            required
          />
        </label>

        <label className="form-control">
          <span className="label-text mb-1">Content</span>
          <textarea
            className="textarea textarea-bordered w-full"
            value={content}
            onChange={(e) => setContent(e.target.value)}
            rows={6}
            required
          />
        </label>

        <div className="mt-2 flex justify-end gap-3">
          <button
            type="button"
            className="btn btn-ghost"
            onClick={() => navigate(isEdit ? `/announcements/${id}` : '/announcements')}
          >
            Cancel
          </button>
          <button type="submit" className="btn btn-primary" disabled={submitting}>
            {submitting ? <span className="loading loading-spinner loading-sm" /> : 'Save'}
          </button>
        </div>
      </form>
    </div>
  )
}
