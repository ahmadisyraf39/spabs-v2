import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import {
  createClubSponsorship,
  getClubSponsorship,
  updateClubSponsorship,
} from '../../lib/api/clubSponsorships'
import { getSponsors } from '../../lib/api/sponsors'
import { SPONSORSHIP_TYPES } from './sponsorshipEnums'

function todayIso() {
  return new Date().toISOString().slice(0, 10)
}

export default function ClubSponsorshipFormPage() {
  const { id } = useParams()
  const isEdit = Boolean(id)
  const navigate = useNavigate()

  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState(null)

  const [sponsors, setSponsors] = useState([])
  const [sponsorId, setSponsorId] = useState('')
  const [sponsorshipType, setSponsorshipType] = useState(SPONSORSHIP_TYPES[0])
  const [amount, setAmount] = useState('')
  const [description, setDescription] = useState('')
  const [startDate, setStartDate] = useState(todayIso())
  const [endDate, setEndDate] = useState(todayIso())

  useEffect(() => {
    let cancelled = false

    async function load() {
      try {
        const [sponsorList, record] = await Promise.all([
          getSponsors(),
          isEdit ? getClubSponsorship(id) : null,
        ])
        if (cancelled) return
        setSponsors(sponsorList)
        setSponsorId((prev) => prev || String(sponsorList[0]?.id ?? ''))
        if (record) {
          setSponsorId(String(record.sponsorId))
          setSponsorshipType(record.sponsorshipType)
          setAmount(String(record.amount))
          setDescription(record.description ?? '')
          setStartDate(record.startDate)
          setEndDate(record.endDate)
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
      const payload = {
        sponsorId: Number(sponsorId),
        sponsorshipType,
        amount: Number(amount),
        description: description || null,
        startDate,
        endDate,
      }
      if (isEdit) {
        await updateClubSponsorship(id, payload)
      } else {
        await createClubSponsorship(payload)
      }
      navigate('/sponsorships')
    } catch (err) {
      setError(err.message ?? 'Unable to save sponsorship.')
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
      <h1 className="mb-4 text-2xl font-semibold">{isEdit ? 'Edit sponsorship' : 'New sponsorship'}</h1>

      <form className="card bg-base-100 flex flex-col gap-4 p-6 shadow-md" onSubmit={handleSubmit}>
        {error && (
          <div role="alert" className="alert alert-error text-sm">
            <span>{error}</span>
          </div>
        )}

        {isEdit && (
          <div role="alert" className="alert alert-warning text-sm">
            <span>
              Editing the sponsor, amount, or dates here won't update the finance record already
              logged for this sponsorship.
            </span>
          </div>
        )}

        <label className="form-control">
          <span className="label-text mb-1">Sponsor</span>
          <select
            className="select select-bordered w-full"
            value={sponsorId}
            onChange={(e) => setSponsorId(e.target.value)}
            required
          >
            {sponsors.length === 0 && <option value="">No sponsors found</option>}
            {sponsors.map((sponsor) => (
              <option key={sponsor.id} value={sponsor.id}>
                {sponsor.name}
              </option>
            ))}
          </select>
        </label>

        <label className="form-control">
          <span className="label-text mb-1">Sponsorship type</span>
          <select
            className="select select-bordered w-full"
            value={sponsorshipType}
            onChange={(e) => setSponsorshipType(e.target.value)}
          >
            {SPONSORSHIP_TYPES.map((value) => (
              <option key={value} value={value}>
                {value}
              </option>
            ))}
          </select>
        </label>

        <label className="form-control">
          <span className="label-text mb-1">Amount (RM)</span>
          <input
            type="number"
            className="input input-bordered w-full"
            value={amount}
            onChange={(e) => setAmount(e.target.value)}
            min="0"
            step="0.01"
            required
          />
        </label>

        <label className="form-control">
          <span className="label-text mb-1">Description</span>
          <textarea
            className="textarea textarea-bordered w-full"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            rows={2}
          />
        </label>

        <label className="form-control">
          <span className="label-text mb-1">Start date</span>
          <input
            type="date"
            className="input input-bordered w-full"
            value={startDate}
            onChange={(e) => setStartDate(e.target.value)}
            required
          />
        </label>

        <label className="form-control">
          <span className="label-text mb-1">End date</span>
          <input
            type="date"
            className="input input-bordered w-full"
            value={endDate}
            onChange={(e) => setEndDate(e.target.value)}
            required
          />
        </label>

        <div className="mt-2 flex justify-end gap-3">
          <button type="button" className="btn btn-ghost" onClick={() => navigate('/sponsorships')}>
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
