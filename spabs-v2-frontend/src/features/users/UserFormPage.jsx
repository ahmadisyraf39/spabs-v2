import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import PasswordInput from '../../components/ui/PasswordInput'
import { useAuth } from '../../features/auth/useAuth'
import { createUser, deleteUser, getUser, updateUser } from '../../lib/api/users'
import { ROLES } from '../../lib/roles'
import {
  CREATE_PROFILE_BY_ROLE,
  findProfileByUserId,
  UPDATE_PROFILE_BY_ROLE,
} from '../../lib/userProfiles'
import { COACH_CERTIFICATIONS, COACH_SPECIALIZATIONS } from './coachEnums'

const ALL_ROLE_OPTIONS = [ROLES.SUPER_ADMIN, ROLES.ADMIN, ROLES.COACH, ROLES.PARENT]

export default function UserFormPage() {
  const { id } = useParams()
  const isEdit = Boolean(id)
  const navigate = useNavigate()
  const { user: currentUser } = useAuth()

  const [loading, setLoading] = useState(isEdit)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState(null)

  const [email, setEmail] = useState('')
  const [fullName, setFullName] = useState('')
  const [phoneNumber, setPhoneNumber] = useState('')
  const [role, setRole] = useState(ROLES.PARENT)
  const [active, setActive] = useState(true)
  const [password, setPassword] = useState('')

  const [profileId, setProfileId] = useState(null)
  const [specialization, setSpecialization] = useState(COACH_SPECIALIZATIONS[0])
  const [certification, setCertification] = useState(COACH_CERTIFICATIONS[0])
  const [emergencyContact, setEmergencyContact] = useState('')
  const [address, setAddress] = useState('')

  const roleOptions =
    currentUser?.role === ROLES.SUPER_ADMIN
      ? ALL_ROLE_OPTIONS
      : ALL_ROLE_OPTIONS.filter((r) => r !== ROLES.SUPER_ADMIN)

  useEffect(() => {
    if (!isEdit) return
    let cancelled = false

    async function load() {
      try {
        const u = await getUser(id)
        if (cancelled) return
        setEmail(u.email)
        setFullName(u.fullName)
        setPhoneNumber(u.phoneNumber ?? '')
        setRole(u.role)
        setActive(u.active)

        const profile = await findProfileByUserId(u.role, id)
        if (cancelled) return
        if (profile) {
          setProfileId(profile.id)
          if (u.role === ROLES.COACH) {
            setSpecialization(profile.specialization)
            setCertification(profile.certification)
          } else if (u.role === ROLES.PARENT) {
            setEmergencyContact(profile.emergencyContact ?? '')
            setAddress(profile.address ?? '')
          }
        }
      } catch (err) {
        if (!cancelled) setError(err.message ?? 'Unable to load user.')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    load()
    return () => {
      cancelled = true
    }
  }, [id, isEdit])

  function profilePayload() {
    if (role === ROLES.COACH) return { specialization, certification }
    if (role === ROLES.PARENT) return { emergencyContact, address }
    return {}
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setError(null)

    if (!isEdit && password && password.length < 8) {
      setError('Password must be at least 8 characters.')
      return
    }

    setSubmitting(true)
    try {
      if (isEdit) {
        await updateUser(id, { email, fullName, phoneNumber, role, active })
        if (profileId) {
          await UPDATE_PROFILE_BY_ROLE[role](profileId, { userId: Number(id), ...profilePayload() })
        }
        navigate('/users')
        return
      }

      const newUser = await createUser({
        email,
        fullName,
        phoneNumber,
        role,
        active,
        password: password || undefined,
      })

      try {
        await CREATE_PROFILE_BY_ROLE[role]({ userId: newUser.id, ...profilePayload() })
      } catch (profileErr) {
        await deleteUser(newUser.id).catch(() => {})
        throw profileErr
      }

      navigate('/users')
    } catch (err) {
      setError(err.message ?? 'Unable to save user.')
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
      <h1 className="mb-4 text-2xl font-semibold">{isEdit ? 'Edit user' : 'New user'}</h1>

      <form className="card bg-base-100 flex flex-col gap-4 p-6 shadow-md" onSubmit={handleSubmit}>
        {error && (
          <div role="alert" className="alert alert-error text-sm">
            <span>{error}</span>
          </div>
        )}

        <label className="form-control">
          <span className="label-text mb-1">Email</span>
          <input
            type="email"
            className="input input-bordered w-full"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
        </label>

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
          <span className="label-text mb-1">Phone number</span>
          <input
            type="tel"
            className="input input-bordered w-full"
            value={phoneNumber}
            onChange={(e) => setPhoneNumber(e.target.value)}
          />
        </label>

        <label className="form-control">
          <span className="label-text mb-1">Role</span>
          <select
            className="select select-bordered w-full"
            value={role}
            onChange={(e) => setRole(e.target.value)}
            disabled={isEdit}
          >
            {roleOptions.map((r) => (
              <option key={r} value={r}>
                {r}
              </option>
            ))}
          </select>
          {isEdit && (
            <span className="label-text-alt text-base-content/50 mt-1">
              Role can&apos;t be changed after creation.
            </span>
          )}
        </label>

        {!isEdit && (
          <PasswordInput
            label="Password (optional)"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            autoComplete="new-password"
            minLength={8}
          />
        )}
        {!isEdit && (
          <span className="label-text-alt text-base-content/50 -mt-2">
            Leave blank to have the system generate one and email it to the user.
          </span>
        )}

        <label className="label cursor-pointer justify-start gap-3">
          <input
            type="checkbox"
            className="checkbox"
            checked={active}
            onChange={(e) => setActive(e.target.checked)}
          />
          <span className="label-text">Active</span>
        </label>

        {role === ROLES.COACH && (
          <>
            <label className="form-control">
              <span className="label-text mb-1">Specialization</span>
              <select
                className="select select-bordered w-full"
                value={specialization}
                onChange={(e) => setSpecialization(e.target.value)}
                required
              >
                {COACH_SPECIALIZATIONS.map((value) => (
                  <option key={value} value={value}>
                    {value}
                  </option>
                ))}
              </select>
            </label>
            <label className="form-control">
              <span className="label-text mb-1">Certification</span>
              <select
                className="select select-bordered w-full"
                value={certification}
                onChange={(e) => setCertification(e.target.value)}
                required
              >
                {COACH_CERTIFICATIONS.map((value) => (
                  <option key={value} value={value}>
                    {value}
                  </option>
                ))}
              </select>
            </label>
          </>
        )}

        {role === ROLES.PARENT && (
          <>
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
              <input
                type="text"
                className="input input-bordered w-full"
                value={address}
                onChange={(e) => setAddress(e.target.value)}
              />
            </label>
          </>
        )}

        <div className="mt-2 flex justify-end gap-3">
          <button type="button" className="btn btn-ghost" onClick={() => navigate('/users')}>
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
