import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { createTeam, getTeam, updateTeam } from '../../lib/api/teams'
import { AGE_GROUPS } from '../../lib/ageUtils'
import CoachSection from './CoachSection'
import RosterSection from './RosterSection'
import { TEAM_CATEGORIES } from './teamEnums'

export default function TeamFormPage() {
  const { id } = useParams()
  const isEdit = Boolean(id)
  const navigate = useNavigate()

  const [loading, setLoading] = useState(isEdit)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState(null)

  const [name, setName] = useState('')
  const [category, setCategory] = useState(TEAM_CATEGORIES[0])
  const [ageGroup, setAgeGroup] = useState(AGE_GROUPS[0])

  useEffect(() => {
    if (!isEdit) return
    let cancelled = false

    async function load() {
      try {
        const team = await getTeam(id)
        if (cancelled) return
        setName(team.name)
        setCategory(team.category)
        setAgeGroup(team.ageGroup)
      } catch (err) {
        if (!cancelled) setError(err.message ?? 'Unable to load team.')
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
      if (isEdit) {
        await updateTeam(id, { name, category, ageGroup })
      } else {
        const team = await createTeam({ name, category, ageGroup })
        navigate(`/teams/${team.id}/edit`, { replace: true })
        return
      }
      navigate('/teams')
    } catch (err) {
      setError(err.message ?? 'Unable to save team.')
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
    <div className="flex flex-col gap-6">
      <h1 className="text-2xl font-semibold">{isEdit ? 'Edit team' : 'New team'}</h1>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        <form className="card bg-base-100 flex flex-col gap-4 p-6 shadow-md" onSubmit={handleSubmit}>
          {error && (
            <div role="alert" className="alert alert-error text-sm">
              <span>{error}</span>
            </div>
          )}

          <label className="form-control">
            <span className="label-text mb-1">Name</span>
            <input
              type="text"
              className="input input-bordered w-full"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />
          </label>

          <label className="form-control">
            <span className="label-text mb-1">Category</span>
            <select
              className="select select-bordered w-full"
              value={category}
              onChange={(e) => setCategory(e.target.value)}
            >
              {TEAM_CATEGORIES.map((value) => (
                <option key={value} value={value}>
                  {value}
                </option>
              ))}
            </select>
          </label>

          <label className="form-control">
            <span className="label-text mb-1">Age group</span>
            <select
              className="select select-bordered w-full"
              value={ageGroup}
              onChange={(e) => setAgeGroup(e.target.value)}
            >
              {AGE_GROUPS.map((value) => (
                <option key={value} value={value}>
                  {value}
                </option>
              ))}
            </select>
          </label>

          <div className="mt-2 flex justify-end gap-3">
            <button type="button" className="btn btn-ghost" onClick={() => navigate('/teams')}>
              Cancel
            </button>
            <button type="submit" className="btn btn-primary" disabled={submitting}>
              {submitting ? <span className="loading loading-spinner loading-sm" /> : 'Save'}
            </button>
          </div>
        </form>

        {isEdit && (
          <div className="lg:col-span-2">
            <CoachSection teamId={id} />
          </div>
        )}
      </div>

      {isEdit && <RosterSection teamId={id} />}
    </div>
  )
}
