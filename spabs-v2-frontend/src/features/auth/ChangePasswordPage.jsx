import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import AuthLayout from '../../components/layout/AuthLayout'
import PasswordInput from '../../components/ui/PasswordInput'
import { changePassword } from '../../lib/api/auth'
import { dashboardPathForRole } from '../../lib/roles'
import { useAuth } from './useAuth'

export default function ChangePasswordPage() {
  const { user, refresh } = useAuth()
  const navigate = useNavigate()
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [error, setError] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setError(null)

    if (newPassword.length < 8) {
      setError('Password must be at least 8 characters.')
      return
    }

    setSubmitting(true)
    try {
      await changePassword({ currentPassword, newPassword })
      const me = await refresh()
      navigate(dashboardPathForRole(me?.role), { replace: true })
    } catch (err) {
      setError(err.message ?? 'Unable to change password.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <AuthLayout
      title="Change your password"
      subtitle={
        user?.mustChangePassword
          ? 'Your password was reset by an admin — set a new one to continue.'
          : undefined
      }
    >
      <form className="flex flex-col gap-4" onSubmit={handleSubmit}>
        {error && (
          <div role="alert" className="alert alert-error text-sm">
            <span>{error}</span>
          </div>
        )}

        <PasswordInput
          label="Current password"
          value={currentPassword}
          onChange={(e) => setCurrentPassword(e.target.value)}
          autoComplete="current-password"
          required
        />

        <PasswordInput
          label="New password"
          value={newPassword}
          onChange={(e) => setNewPassword(e.target.value)}
          autoComplete="new-password"
          minLength={8}
          required
        />

        <button type="submit" className="btn btn-primary" disabled={submitting}>
          {submitting ? <span className="loading loading-spinner loading-sm" /> : 'Change password'}
        </button>
      </form>
    </AuthLayout>
  )
}
