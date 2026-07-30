import { useEffect, useState } from 'react'
import { getMyParentProfile, updateMyParentProfile } from '../../lib/api/profiles'
import { updateMyProfile } from '../../lib/api/users'
import { useAuth } from './useAuth'

export default function MyProfilePage() {
  const { user, refresh } = useAuth()
  const isParent = user?.role === 'PARENT'

  const [phoneNumber, setPhoneNumber] = useState(user?.phoneNumber ?? '')
  const [emergencyContact, setEmergencyContact] = useState('')
  const [address, setAddress] = useState('')
  const [loading, setLoading] = useState(isParent)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState(null)
  const [success, setSuccess] = useState(false)

  useEffect(() => {
    if (!isParent) return
    let cancelled = false

    async function load() {
      try {
        const parent = await getMyParentProfile()
        if (cancelled) return
        setEmergencyContact(parent.emergencyContact ?? '')
        setAddress(parent.address ?? '')
      } catch (err) {
        if (!cancelled) setError(err.message ?? 'Unable to load profile.')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    load()
    return () => {
      cancelled = true
    }
  }, [isParent])

  async function handleSubmit(event) {
    event.preventDefault()
    setError(null)
    setSuccess(false)
    setSubmitting(true)
    try {
      await updateMyProfile({ phoneNumber: phoneNumber || null })
      if (isParent) {
        await updateMyParentProfile({
          emergencyContact: emergencyContact || null,
          address: address || null,
        })
      }
      await refresh()
      setSuccess(true)
    } catch (err) {
      setError(err.message ?? 'Unable to save profile.')
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
      <h1 className="mb-4 text-2xl font-semibold">My profile</h1>

      <form className="card bg-base-100 flex flex-col gap-4 p-6 shadow-md" onSubmit={handleSubmit}>
        {error && (
          <div role="alert" className="alert alert-error text-sm">
            <span>{error}</span>
          </div>
        )}
        {success && (
          <div role="alert" className="alert alert-success text-sm">
            <span>Profile updated.</span>
          </div>
        )}

        <label className="form-control">
          <span className="label-text mb-1">Full name</span>
          <input type="text" className="input input-bordered w-full" value={user?.fullName ?? ''} disabled />
        </label>

        <label className="form-control">
          <span className="label-text mb-1">Email</span>
          <input type="text" className="input input-bordered w-full" value={user?.email ?? ''} disabled />
        </label>

        <label className="form-control">
          <span className="label-text mb-1">Role</span>
          <input type="text" className="input input-bordered w-full" value={user?.role ?? ''} disabled />
        </label>

        <label className="form-control">
          <span className="label-text mb-1">Phone number</span>
          <input
            type="tel"
            className="input input-bordered w-full"
            value={phoneNumber}
            onChange={(e) => setPhoneNumber(e.target.value)}
          />
        </label>

        {isParent && (
          <>
            <div className="divider my-0">Guardian details</div>

            <label className="form-control">
              <span className="label-text mb-1">Emergency contact</span>
              <input
                type="text"
                className="input input-bordered w-full"
                value={emergencyContact}
                onChange={(e) => setEmergencyContact(e.target.value)}
              />
            </label>

            <label className="form-control">
              <span className="label-text mb-1">Address</span>
              <textarea
                className="textarea textarea-bordered w-full"
                value={address}
                onChange={(e) => setAddress(e.target.value)}
                rows={3}
              />
            </label>
          </>
        )}

        <div className="mt-2 flex justify-end">
          <button type="submit" className="btn btn-primary" disabled={submitting}>
            {submitting ? <span className="loading loading-spinner loading-sm" /> : 'Save changes'}
          </button>
        </div>
      </form>
    </div>
  )
}
