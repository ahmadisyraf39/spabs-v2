import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { createSponsor, getSponsor, updateSponsor } from '../../lib/api/sponsors'

export default function SponsorFormPage() {
  const { id } = useParams()
  const isEdit = Boolean(id)
  const navigate = useNavigate()

  const [loading, setLoading] = useState(isEdit)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState(null)

  const [name, setName] = useState('')
  const [contactPerson, setContactPerson] = useState('')
  const [phoneNumber, setPhoneNumber] = useState('')
  const [email, setEmail] = useState('')
  const [address, setAddress] = useState('')

  useEffect(() => {
    if (!isEdit) return
    let cancelled = false

    async function load() {
      try {
        const sponsor = await getSponsor(id)
        if (cancelled) return
        setName(sponsor.name)
        setContactPerson(sponsor.contactPerson ?? '')
        setPhoneNumber(sponsor.phoneNumber ?? '')
        setEmail(sponsor.email ?? '')
        setAddress(sponsor.address ?? '')
      } catch (err) {
        if (!cancelled) setError(err.message ?? 'Unable to load sponsor.')
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
        name,
        contactPerson: contactPerson || null,
        phoneNumber: phoneNumber || null,
        email: email || null,
        address: address || null,
      }
      if (isEdit) {
        await updateSponsor(id, payload)
      } else {
        await createSponsor(payload)
      }
      navigate('/sponsors')
    } catch (err) {
      setError(err.message ?? 'Unable to save sponsor.')
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
      <h1 className="mb-4 text-2xl font-semibold">{isEdit ? 'Edit sponsor' : 'New sponsor'}</h1>

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
          <span className="label-text mb-1">Contact person</span>
          <input
            type="text"
            className="input input-bordered w-full"
            value={contactPerson}
            onChange={(e) => setContactPerson(e.target.value)}
          />
        </label>

        <label className="form-control">
          <span className="label-text mb-1">Phone</span>
          <input
            type="text"
            className="input input-bordered w-full"
            value={phoneNumber}
            onChange={(e) => setPhoneNumber(e.target.value)}
          />
        </label>

        <label className="form-control">
          <span className="label-text mb-1">Email</span>
          <input
            type="email"
            className="input input-bordered w-full"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
          />
        </label>

        <label className="form-control">
          <span className="label-text mb-1">Address</span>
          <textarea
            className="textarea textarea-bordered w-full"
            value={address}
            onChange={(e) => setAddress(e.target.value)}
            rows={2}
          />
        </label>

        <div className="mt-2 flex justify-end gap-3">
          <button type="button" className="btn btn-ghost" onClick={() => navigate('/sponsors')}>
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
