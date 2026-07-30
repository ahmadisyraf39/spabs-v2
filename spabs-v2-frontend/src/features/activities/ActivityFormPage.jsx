import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { createActivity, getActivity, updateActivity } from '../../lib/api/activities'
import { getTeams } from '../../lib/api/teams'
import { getActivityTimeframe, toDateTimeLocalValue } from '../../lib/dateTimeUtils'
import { ACTIVITY_TYPES } from './activityEnums'

export default function ActivityFormPage() {
  const { id } = useParams()
  const isEdit = Boolean(id)
  const navigate = useNavigate()

  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState(null)
  const [locked, setLocked] = useState(false)

  const [teams, setTeams] = useState([])
  const [teamId, setTeamId] = useState('')
  const [type, setType] = useState(ACTIVITY_TYPES[0])
  const [title, setTitle] = useState('')
  const [startAt, setStartAt] = useState('')
  const [endAt, setEndAt] = useState('')
  const [location, setLocation] = useState('')
  const [description, setDescription] = useState('')

  useEffect(() => {
    let cancelled = false

    async function load() {
      try {
        const [teamList, activity] = await Promise.all([
          getTeams(),
          isEdit ? getActivity(id) : Promise.resolve(null),
        ])
        if (cancelled) return
        setTeams(teamList)
        if (activity) {
          if (getActivityTimeframe(activity.startAt) === 'past') {
            setLocked(true)
            return
          }
          setTeamId(String(activity.teamId))
          setType(activity.type)
          setTitle(activity.title ?? '')
          setStartAt(toDateTimeLocalValue(activity.startAt))
          setEndAt(toDateTimeLocalValue(activity.endAt))
          setLocation(activity.location ?? '')
          setDescription(activity.description ?? '')
        } else {
          setTeamId((prev) => prev || String(teamList[0]?.id ?? ''))
        }
      } catch (err) {
        if (!cancelled) setError(err.message ?? 'Unable to load activity.')
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
      const payload = {
        teamId: Number(teamId),
        type,
        title: title || null,
        startAt,
        endAt: endAt || null,
        location: location || null,
        description: description || null,
      }
      const activity = isEdit ? await updateActivity(id, payload) : await createActivity(payload)
      navigate(`/activities/${activity.id}`)
    } catch (err) {
      setError(err.message ?? 'Unable to save activity.')
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

  if (locked) {
    return (
      <div className="mx-auto max-w-lg">
        <h1 className="mb-4 text-2xl font-semibold">Edit activity</h1>
        <div className="card bg-base-100 flex flex-col gap-4 p-6 shadow-md">
          <div role="alert" className="alert alert-warning text-sm">
            <span>This activity has already happened and can no longer be edited.</span>
          </div>
          <div className="flex justify-end">
            <Link to={`/activities/${id}`} className="btn btn-ghost">
              Back to details
            </Link>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="mx-auto max-w-lg">
      <h1 className="mb-4 text-2xl font-semibold">{isEdit ? 'Edit activity' : 'New activity'}</h1>

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
            required
          >
            {teams.length === 0 && <option value="">No teams found</option>}
            {teams.map((team) => (
              <option key={team.id} value={team.id}>
                {team.name}
              </option>
            ))}
          </select>
        </label>

        <label className="form-control">
          <span className="label-text mb-1">Type</span>
          <select
            className="select select-bordered w-full"
            value={type}
            onChange={(e) => setType(e.target.value)}
          >
            {ACTIVITY_TYPES.map((value) => (
              <option key={value} value={value}>
                {value}
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
            placeholder="Optional — falls back to the type if left blank"
          />
        </label>

        <label className="form-control">
          <span className="label-text mb-1">Start</span>
          <input
            type="datetime-local"
            className="input input-bordered w-full"
            value={startAt}
            onChange={(e) => setStartAt(e.target.value)}
            required
          />
        </label>

        <label className="form-control">
          <span className="label-text mb-1">End</span>
          <input
            type="datetime-local"
            className="input input-bordered w-full"
            value={endAt}
            onChange={(e) => setEndAt(e.target.value)}
          />
        </label>

        <label className="form-control">
          <span className="label-text mb-1">Location</span>
          <input
            type="text"
            className="input input-bordered w-full"
            value={location}
            onChange={(e) => setLocation(e.target.value)}
          />
        </label>

        <label className="form-control">
          <span className="label-text mb-1">Description</span>
          <textarea
            className="textarea textarea-bordered w-full"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            rows={3}
          />
        </label>

        <div className="mt-2 flex justify-end gap-3">
          <button type="button" className="btn btn-ghost" onClick={() => navigate('/activities')}>
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
