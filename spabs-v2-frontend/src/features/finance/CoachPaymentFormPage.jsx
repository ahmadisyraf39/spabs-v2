import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { createCoachPayment, getCoachPayment, updateCoachPayment } from '../../lib/api/coachPayments'
import { getCoachOptions } from '../../lib/teamRelations'
import { COACH_PAYMENT_TYPES } from './financeLedgerEnums'

function todayIso() {
  return new Date().toISOString().slice(0, 10)
}

export default function CoachPaymentFormPage() {
  const { id } = useParams()
  const isEdit = Boolean(id)
  const navigate = useNavigate()

  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState(null)

  const [coachOptions, setCoachOptions] = useState([])
  const [coachId, setCoachId] = useState('')
  const [paymentType, setPaymentType] = useState(COACH_PAYMENT_TYPES[0])
  const [amount, setAmount] = useState('')
  const [paymentDate, setPaymentDate] = useState(todayIso())
  const [remarks, setRemarks] = useState('')
  const [status, setStatus] = useState(null)

  useEffect(() => {
    let cancelled = false

    async function load() {
      try {
        const [options, payment] = await Promise.all([
          getCoachOptions(),
          isEdit ? getCoachPayment(id) : null,
        ])
        if (cancelled) return
        setCoachOptions(options)
        setCoachId((prev) => prev || String(options[0]?.coachId ?? ''))
        if (payment) {
          setCoachId(String(payment.coachId))
          setPaymentType(payment.paymentType)
          setAmount(String(payment.amount))
          setPaymentDate(payment.paymentDate)
          setRemarks(payment.remarks ?? '')
          setStatus(payment.status)
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
        coachId: Number(coachId),
        amount: Number(amount),
        paymentDate,
        paymentType,
        remarks: remarks || null,
      }
      if (isEdit) {
        await updateCoachPayment(id, payload)
      } else {
        await createCoachPayment(payload)
      }
      navigate('/coach-payments')
    } catch (err) {
      setError(err.message ?? 'Unable to save payment.')
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
      <h1 className="mb-4 text-2xl font-semibold">{isEdit ? 'Edit coach payment' : 'New coach payment'}</h1>

      <form className="card bg-base-100 flex flex-col gap-4 p-6 shadow-md" onSubmit={handleSubmit}>
        {error && (
          <div role="alert" className="alert alert-error text-sm">
            <span>{error}</span>
          </div>
        )}

        {status === 'PAID' && (
          <div role="alert" className="alert alert-warning text-sm">
            <span>
              This payment is already marked paid. Editing the amount, type, or date here won't
              update the finance transaction already recorded for it.
            </span>
          </div>
        )}

        <label className="form-control">
          <span className="label-text mb-1">Coach</span>
          <select
            className="select select-bordered w-full"
            value={coachId}
            onChange={(e) => setCoachId(e.target.value)}
            required
          >
            {coachOptions.length === 0 && <option value="">No coaches found</option>}
            {coachOptions.map((option) => (
              <option key={option.coachId} value={option.coachId}>
                {option.label}
              </option>
            ))}
          </select>
        </label>

        <label className="form-control">
          <span className="label-text mb-1">Payment type</span>
          <select
            className="select select-bordered w-full"
            value={paymentType}
            onChange={(e) => setPaymentType(e.target.value)}
          >
            {COACH_PAYMENT_TYPES.map((value) => (
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
          <span className="label-text mb-1">Payment date</span>
          <input
            type="date"
            className="input input-bordered w-full"
            value={paymentDate}
            onChange={(e) => setPaymentDate(e.target.value)}
            required
          />
        </label>

        <label className="form-control">
          <span className="label-text mb-1">Remarks</span>
          <input
            type="text"
            className="input input-bordered w-full"
            value={remarks}
            onChange={(e) => setRemarks(e.target.value)}
            placeholder="e.g. MARCH 2026"
          />
        </label>

        <div className="mt-2 flex justify-end gap-3">
          <button type="button" className="btn btn-ghost" onClick={() => navigate('/coach-payments')}>
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
