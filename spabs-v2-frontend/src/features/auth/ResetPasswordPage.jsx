import { useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import AuthLayout from '../../components/layout/AuthLayout'
import PasswordInput from '../../components/ui/PasswordInput'
import { resetPassword } from '../../lib/api/auth'

export default function ResetPasswordPage() {
  const [searchParams] = useSearchParams()
  const token = searchParams.get('token') ?? ''
  const [newPassword, setNewPassword] = useState('')
  const [error, setError] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const [done, setDone] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setError(null)

    if (newPassword.length < 8) {
      setError('Password must be at least 8 characters.')
      return
    }

    setSubmitting(true)
    try {
      await resetPassword({ token, newPassword })
      setDone(true)
    } catch (err) {
      setError(err.message ?? 'Unable to reset password.')
    } finally {
      setSubmitting(false)
    }
  }

  if (done) {
    return (
      <AuthLayout title="Password reset">
        <p className="text-base-content/80 text-sm">
          Your password has been reset. You can now log in with your new password.
        </p>
        <Link to="/login" className="btn btn-primary mt-6 w-full">
          Back to login
        </Link>
      </AuthLayout>
    )
  }

  if (!token) {
    return (
      <AuthLayout title="Reset password">
        <div role="alert" className="alert alert-error text-sm">
          <span>This reset link is missing its token. Request a new one.</span>
        </div>
        <Link to="/forgot-password" className="btn btn-primary mt-4 w-full">
          Request a new link
        </Link>
      </AuthLayout>
    )
  }

  return (
    <AuthLayout title="Reset password" subtitle="Choose a new password for your account.">
      <form className="flex flex-col gap-4" onSubmit={handleSubmit}>
        {error && (
          <div role="alert" className="alert alert-error text-sm">
            <span>{error}</span>
          </div>
        )}

        <PasswordInput
          label="New password"
          value={newPassword}
          onChange={(e) => setNewPassword(e.target.value)}
          autoComplete="new-password"
          minLength={8}
          required
        />

        <button type="submit" className="btn btn-primary" disabled={submitting}>
          {submitting ? <span className="loading loading-spinner loading-sm" /> : 'Reset password'}
        </button>
      </form>
    </AuthLayout>
  )
}
