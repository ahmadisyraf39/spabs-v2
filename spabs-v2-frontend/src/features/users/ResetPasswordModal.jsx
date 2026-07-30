import { useState } from 'react'
import { resetUserPassword } from '../../lib/api/users'

export default function ResetPasswordModal({ user, onClose }) {
  const [status, setStatus] = useState('confirm')
  const [temporaryPassword, setTemporaryPassword] = useState(null)
  const [error, setError] = useState(null)
  const [copied, setCopied] = useState(false)

  async function handleConfirm() {
    setStatus('loading')
    try {
      const result = await resetUserPassword(user.id)
      setTemporaryPassword(result.temporaryPassword)
      setStatus('done')
    } catch (err) {
      setError(err.message ?? 'Unable to reset password.')
      setStatus('error')
    }
  }

  async function handleCopy() {
    await navigator.clipboard.writeText(temporaryPassword)
    setCopied(true)
  }

  return (
    <div className="modal modal-open">
      <div className="modal-box">
        <h3 className="text-lg font-semibold">Reset password</h3>

        {status === 'confirm' && (
          <>
            <p className="text-base-content/70 mt-2 text-sm">
              This generates a new temporary password for <strong>{user.fullName}</strong> and
              forces them to change it on next login. Continue?
            </p>
            <div className="modal-action">
              <button type="button" className="btn" onClick={onClose}>
                Cancel
              </button>
              <button type="button" className="btn btn-primary" onClick={handleConfirm}>
                Reset password
              </button>
            </div>
          </>
        )}

        {status === 'loading' && (
          <div className="flex justify-center py-6">
            <span className="loading loading-spinner loading-lg text-primary" />
          </div>
        )}

        {status === 'error' && (
          <>
            <div role="alert" className="alert alert-error mt-3 text-sm">
              <span>{error}</span>
            </div>
            <div className="modal-action">
              <button type="button" className="btn" onClick={onClose}>
                Close
              </button>
            </div>
          </>
        )}

        {status === 'done' && (
          <>
            <p className="text-base-content/70 mt-2 text-sm">
              This password won&apos;t be shown again — copy it now and share it securely.
            </p>
            <div className="join mt-3 w-full">
              <input
                type="text"
                readOnly
                value={temporaryPassword}
                className="input input-bordered join-item w-full font-mono"
              />
              <button type="button" className="btn join-item" onClick={handleCopy}>
                {copied ? 'Copied' : 'Copy'}
              </button>
            </div>
            <div className="modal-action">
              <button type="button" className="btn btn-primary" onClick={onClose}>
                Done
              </button>
            </div>
          </>
        )}
      </div>
    </div>
  )
}
