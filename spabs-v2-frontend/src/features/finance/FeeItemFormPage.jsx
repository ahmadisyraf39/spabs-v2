import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { createFeeItem, getFeeItem, updateFeeItem } from '../../lib/api/feeItems'
import { FEE_TYPES } from './feeEnums'

export default function FeeItemFormPage() {
  const { id } = useParams()
  const isEdit = Boolean(id)
  const navigate = useNavigate()

  const [loading, setLoading] = useState(isEdit)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState(null)

  const [name, setName] = useState('')
  const [feeType, setFeeType] = useState(FEE_TYPES[0])
  const [amount, setAmount] = useState('')

  useEffect(() => {
    if (!isEdit) return
    let cancelled = false

    async function load() {
      try {
        const item = await getFeeItem(id)
        if (cancelled) return
        setName(item.name)
        setFeeType(item.feeType)
        setAmount(String(item.amount))
      } catch (err) {
        if (!cancelled) setError(err.message ?? 'Unable to load fee item.')
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
      const payload = { name, feeType, amount: Number(amount) }
      if (isEdit) {
        await updateFeeItem(id, payload)
      } else {
        await createFeeItem(payload)
      }
      navigate('/fee-items')
    } catch (err) {
      setError(err.message ?? 'Unable to save fee item.')
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
      <h1 className="mb-4 text-2xl font-semibold">{isEdit ? 'Edit fee item' : 'New fee item'}</h1>

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
          <span className="label-text mb-1">Fee type</span>
          <select
            className="select select-bordered w-full"
            value={feeType}
            onChange={(e) => setFeeType(e.target.value)}
          >
            {FEE_TYPES.map((value) => (
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

        <div className="mt-2 flex justify-end gap-3">
          <button type="button" className="btn btn-ghost" onClick={() => navigate('/fee-items')}>
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
