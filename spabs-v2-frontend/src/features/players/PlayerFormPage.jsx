import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { createPlayer, getPlayer, updatePlayer } from '../../lib/api/players'
import GuardianSection from './GuardianSection'
import { GENDERS } from './playerEnums'

export default function PlayerFormPage() {
  const { id } = useParams()
  const isEdit = Boolean(id)
  const navigate = useNavigate()

  const [loading, setLoading] = useState(isEdit)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState(null)

  const [fullName, setFullName] = useState('')
  const [dateOfBirth, setDateOfBirth] = useState('')
  const [gender, setGender] = useState(GENDERS[0])

  useEffect(() => {
    if (!isEdit) return
    let cancelled = false

    async function load() {
      try {
        const player = await getPlayer(id)
        if (cancelled) return
        setFullName(player.fullName)
        setDateOfBirth(player.dateOfBirth)
        setGender(player.gender)
      } catch (err) {
        if (!cancelled) setError(err.message ?? 'Unable to load player.')
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
        await updatePlayer(id, { fullName, dateOfBirth, gender })
      } else {
        const player = await createPlayer({ fullName, dateOfBirth, gender })
        navigate(`/players/${player.id}/edit`, { replace: true })
        return
      }
      navigate('/players')
    } catch (err) {
      setError(err.message ?? 'Unable to save player.')
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
      <h1 className="mb-4 text-2xl font-semibold">{isEdit ? 'Edit player' : 'New player'}</h1>

      <form className="card bg-base-100 flex flex-col gap-4 p-6 shadow-md" onSubmit={handleSubmit}>
        {error && (
          <div role="alert" className="alert alert-error text-sm">
            <span>{error}</span>
          </div>
        )}

        <label className="form-control">
          <span className="label-text mb-1">Full name</span>
          <input
            type="text"
            className="input input-bordered w-full"
            value={fullName}
            onChange={(e) => setFullName(e.target.value)}
            required
          />
        </label>

        <label className="form-control">
          <span className="label-text mb-1">Date of birth</span>
          <input
            type="date"
            className="input input-bordered w-full"
            value={dateOfBirth}
            onChange={(e) => setDateOfBirth(e.target.value)}
            required
          />
        </label>

        <label className="form-control">
          <span className="label-text mb-1">Gender</span>
          <select
            className="select select-bordered w-full"
            value={gender}
            onChange={(e) => setGender(e.target.value)}
          >
            {GENDERS.map((value) => (
              <option key={value} value={value}>
                {value}
              </option>
            ))}
          </select>
        </label>

        <div className="mt-2 flex justify-end gap-3">
          <button type="button" className="btn btn-ghost" onClick={() => navigate('/players')}>
            Cancel
          </button>
          <button type="submit" className="btn btn-primary" disabled={submitting}>
            {submitting ? <span className="loading loading-spinner loading-sm" /> : 'Save'}
          </button>
        </div>
      </form>

      {isEdit && (
        <div className="mt-6">
          <GuardianSection playerId={id} />
        </div>
      )}
    </div>
  )
}
