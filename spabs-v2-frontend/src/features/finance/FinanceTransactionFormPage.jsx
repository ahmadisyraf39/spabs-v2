import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { getActivities } from '../../lib/api/activities'
import {
  createActivityFinanceEntry,
  createFinanceTransaction,
  getFinanceTransaction,
  updateFinanceTransaction,
} from '../../lib/api/financeTransactions'
import { categoriesForType, financeReferenceLabel, FINANCE_TYPES } from './financeLedgerEnums'

function todayIso() {
  return new Date().toISOString().slice(0, 10)
}

export default function FinanceTransactionFormPage() {
  const { id } = useParams()
  const isEdit = Boolean(id)
  const navigate = useNavigate()

  const [loading, setLoading] = useState(true)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState(null)

  const [linkedTo, setLinkedTo] = useState(null)
  const [entryMode, setEntryMode] = useState('standalone')
  const [activities, setActivities] = useState([])
  const [activityId, setActivityId] = useState('')

  const [financeType, setFinanceType] = useState(FINANCE_TYPES[0])
  const [financeCategory, setFinanceCategory] = useState(categoriesForType(FINANCE_TYPES[0])[0])
  const [amount, setAmount] = useState('')
  const [description, setDescription] = useState('')
  const [transactionDate, setTransactionDate] = useState(todayIso())

  useEffect(() => {
    let cancelled = false

    async function load() {
      try {
        const [activityList, transaction] = await Promise.all([
          getActivities(),
          isEdit ? getFinanceTransaction(id) : null,
        ])
        if (cancelled) return
        setActivities(activityList)
        if (transaction) {
          setFinanceType(transaction.financeType)
          setFinanceCategory(transaction.financeCategory)
          setAmount(String(transaction.amount))
          setDescription(transaction.description ?? '')
          setTransactionDate(transaction.transactionDate)
          setLinkedTo(financeReferenceLabel(transaction.referenceType, transaction.referenceId))
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

  function handleTypeChange(nextType) {
    setFinanceType(nextType)
    setFinanceCategory(categoriesForType(nextType)[0])
  }

  async function handleSubmit(event) {
    event.preventDefault()
    setError(null)
    if (!isEdit && entryMode === 'activity' && !activityId) {
      setError('Select an activity.')
      return
    }
    setSubmitting(true)
    try {
      const payload = {
        financeType,
        financeCategory,
        amount: Number(amount),
        description: description || null,
        transactionDate,
      }
      if (isEdit) {
        await updateFinanceTransaction(id, payload)
      } else if (entryMode === 'activity') {
        await createActivityFinanceEntry({ ...payload, activityId: Number(activityId) })
      } else {
        await createFinanceTransaction(payload)
      }
      navigate('/finance-transactions')
    } catch (err) {
      setError(err.message ?? 'Unable to save transaction.')
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
      <h1 className="mb-4 text-2xl font-semibold">{isEdit ? 'Edit transaction' : 'New transaction'}</h1>

      <form className="card bg-base-100 flex flex-col gap-4 p-6 shadow-md" onSubmit={handleSubmit}>
        {error && (
          <div role="alert" className="alert alert-error text-sm">
            <span>{error}</span>
          </div>
        )}

        {linkedTo && (
          <div role="alert" className="alert alert-warning text-sm">
            <span>
              This transaction is linked to {linkedTo}. Editing it here won't update that record —
              they may drift out of sync.
            </span>
          </div>
        )}

        {!isEdit && (
          <label className="form-control">
            <span className="label-text mb-1">Entry type</span>
            <select
              className="select select-bordered w-full"
              value={entryMode}
              onChange={(e) => setEntryMode(e.target.value)}
            >
              <option value="standalone">Standalone</option>
              <option value="activity">Tied to an activity</option>
            </select>
          </label>
        )}

        {!isEdit && entryMode === 'activity' && (
          <label className="form-control">
            <span className="label-text mb-1">Activity</span>
            <select
              className="select select-bordered w-full"
              value={activityId}
              onChange={(e) => setActivityId(e.target.value)}
              required
            >
              <option value="">Select an activity</option>
              {activities.map((a) => (
                <option key={a.id} value={a.id}>
                  {a.title?.trim() ? a.title : a.type}
                </option>
              ))}
            </select>
          </label>
        )}

        <label className="form-control">
          <span className="label-text mb-1">Type</span>
          <select
            className="select select-bordered w-full"
            value={financeType}
            onChange={(e) => handleTypeChange(e.target.value)}
          >
            {FINANCE_TYPES.map((value) => (
              <option key={value} value={value}>
                {value}
              </option>
            ))}
          </select>
        </label>

        <label className="form-control">
          <span className="label-text mb-1">Category</span>
          <select
            className="select select-bordered w-full"
            value={financeCategory}
            onChange={(e) => setFinanceCategory(e.target.value)}
          >
            {categoriesForType(financeType).map((value) => (
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
          <span className="label-text mb-1">Description</span>
          <input
            type="text"
            className="input input-bordered w-full"
            value={description}
            onChange={(e) => setDescription(e.target.value)}
          />
        </label>

        <label className="form-control">
          <span className="label-text mb-1">Date</span>
          <input
            type="date"
            className="input input-bordered w-full"
            value={transactionDate}
            onChange={(e) => setTransactionDate(e.target.value)}
            required
          />
        </label>

        <div className="mt-2 flex justify-end gap-3">
          <button
            type="button"
            className="btn btn-ghost"
            onClick={() => navigate('/finance-transactions')}
          >
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
