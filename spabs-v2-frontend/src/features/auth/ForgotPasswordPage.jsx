import { useState } from 'react'
import { Link } from 'react-router-dom'
import AuthLayout from '../../components/layout/AuthLayout'
import { forgotPassword } from '../../lib/api/auth'

export default function ForgotPasswordPage() {
  const [email, setEmail] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [submitted, setSubmitted] = useState(false)

  async function handleSubmit(event) {
    event.preventDefault()
    setSubmitting(true)
    try {
      await forgotPassword({ email })
    } finally {
      setSubmitting(false)
      setSubmitted(true)
    }
  }

  if (submitted) {
    return (
      <AuthLayout title="Check your email">
        <p className="text-base-content/80 text-sm">
          If an account exists for <span className="font-medium">{email}</span>, we&apos;ve sent a
          password reset link to it.
        </p>
        <Link to="/login" className="btn btn-primary mt-6 w-full">
          Back to login
        </Link>
      </AuthLayout>
    )
  }

  return (
    <AuthLayout
      title="Forgot password"
      subtitle="Enter your email and we'll send you a reset link."
    >
      <form className="flex flex-col gap-4" onSubmit={handleSubmit}>
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

        <button type="submit" className="btn btn-primary" disabled={submitting}>
          {submitting ? <span className="loading loading-spinner loading-sm" /> : 'Send reset link'}
        </button>

        <Link to="/login" className="link link-primary self-center text-sm">
          Back to login
        </Link>
      </form>
    </AuthLayout>
  )
}
