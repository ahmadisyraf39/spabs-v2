import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import AuthLayout from '../../components/layout/AuthLayout'
import PasswordInput from '../../components/ui/PasswordInput'
import { dashboardPathForRole } from '../../lib/roles'
import { useAuth } from './useAuth'

export default function LoginPage() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setError(null)
    setSubmitting(true)
    try {
      const me = await login({ email, password })
      if (!me) {
        setError('Something went wrong loading your account. Please try again.')
        return
      }
      navigate(me.mustChangePassword ? '/change-password' : dashboardPathForRole(me.role), {
        replace: true,
      })
    } catch (err) {
      setError(err.message ?? 'Unable to log in.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <AuthLayout title="Log in" subtitle="Welcome back — enter your details to continue.">
      <form className="flex flex-col gap-4" onSubmit={handleSubmit}>
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
            autoComplete="email"
            required
          />
        </label>

        <PasswordInput
          label="Password"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          autoComplete="current-password"
          required
        />

        <div className="text-center">
          <Link to="/forgot-password" className="link link-primary text-sm">
            Forgot password?
          </Link>
        </div>

        <button type="submit" className="btn btn-primary" disabled={submitting}>
          {submitting ? <span className="loading loading-spinner loading-sm" /> : 'Log in'}
        </button>
      </form>
    </AuthLayout>
  )
}
