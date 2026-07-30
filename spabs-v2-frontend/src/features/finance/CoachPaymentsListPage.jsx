import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import ConfirmModal from '../../components/ui/ConfirmModal'
import Pagination from '../../components/ui/Pagination'
import SortableTh from '../../components/ui/SortableTh'
import { deleteCoachPayment, getCoachPayments, payCoachPayment } from '../../lib/api/coachPayments'
import { formatCurrency } from '../../lib/formatCurrency'
import { getCoachOptions } from '../../lib/teamRelations'
import { usePagination } from '../../lib/usePagination'
import { useSort } from '../../lib/useSort'
import { COACH_PAYMENT_TYPES, coachPaymentTypeBadgeClass, paymentStatusBadgeClass } from './financeLedgerEnums'
import { PAYMENT_STATUSES } from './feeEnums'

export default function CoachPaymentsListPage() {
  const [coachOptions, setCoachOptions] = useState([])
  const [payments, setPayments] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [coachFilter, setCoachFilter] = useState('')
  const [typeFilter, setTypeFilter] = useState('')
  const [statusFilter, setStatusFilter] = useState('')
  const [payingId, setPayingId] = useState(null)
  const [payTarget, setPayTarget] = useState(null)
  const [deleteTarget, setDeleteTarget] = useState(null)
  const [deleting, setDeleting] = useState(false)

  async function loadPayments() {
    setLoading(true)
    setError(null)
    try {
      const [options, paymentList] = await Promise.all([getCoachOptions(), getCoachPayments()])
      const labelsByCoachId = new Map(options.map((o) => [o.coachId, o.label]))
      setCoachOptions(options)
      setPayments(
        paymentList.map((p) => ({
          ...p,
          coachLabel: labelsByCoachId.get(p.coachId) ?? `Coach #${p.coachId}`,
        })),
      )
    } catch (err) {
      setError(err.message ?? 'Unable to load coach payments.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadPayments()
  }, [])

  const filteredPayments = useMemo(() => {
    return payments.filter((p) => {
      const matchesCoach = !coachFilter || p.coachId === Number(coachFilter)
      const matchesType = !typeFilter || p.paymentType === typeFilter
      const matchesStatus = !statusFilter || p.status === statusFilter
      return matchesCoach && matchesType && matchesStatus
    })
  }, [payments, coachFilter, typeFilter, statusFilter])

  const { sorted, sortKey, sortDirection, requestSort } = useSort(filteredPayments, 'paymentDate', 'desc')
  const { pageItems, page, setPage, totalPages } = usePagination(sorted, 10)

  function resetToFirstPage() {
    setPage(1)
  }

  async function handlePay() {
    const payment = payTarget
    setPayingId(payment.id)
    setError(null)
    try {
      const updated = await payCoachPayment(payment.id)
      setPayments((prev) => prev.map((p) => (p.id === payment.id ? { ...p, status: updated.status } : p)))
      setPayTarget(null)
    } catch (err) {
      setError(err.message ?? 'Unable to mark payment as paid.')
    } finally {
      setPayingId(null)
    }
  }

  async function handleDelete() {
    setDeleting(true)
    try {
      await deleteCoachPayment(deleteTarget.id)
      setPayments((prev) => prev.filter((p) => p.id !== deleteTarget.id))
      setDeleteTarget(null)
    } catch (err) {
      setError(err.message ?? 'Unable to delete payment.')
    } finally {
      setDeleting(false)
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">Payroll</h1>
        <Link to="/coach-payments/new" className="btn btn-primary btn-sm">
          New payment
        </Link>
      </div>

      {error && (
        <div role="alert" className="alert alert-error text-sm">
          <span>{error}</span>
        </div>
      )}

      <div className="flex gap-3 sm:justify-end">
        <select
          className="select select-bordered select-sm flex-1 sm:flex-initial"
          value={coachFilter}
          onChange={(e) => {
            setCoachFilter(e.target.value)
            resetToFirstPage()
          }}
        >
          <option value="">All coaches</option>
          {coachOptions.map((option) => (
            <option key={option.coachId} value={option.coachId}>
              {option.label}
            </option>
          ))}
        </select>
        <select
          className="select select-bordered select-sm flex-1 sm:flex-initial"
          value={typeFilter}
          onChange={(e) => {
            setTypeFilter(e.target.value)
            resetToFirstPage()
          }}
        >
          <option value="">All payment types</option>
          {COACH_PAYMENT_TYPES.map((value) => (
            <option key={value} value={value}>
              {value}
            </option>
          ))}
        </select>
        <select
          className="select select-bordered select-sm flex-1 sm:flex-initial"
          value={statusFilter}
          onChange={(e) => {
            setStatusFilter(e.target.value)
            resetToFirstPage()
          }}
        >
          <option value="">All statuses</option>
          {PAYMENT_STATUSES.map((value) => (
            <option key={value} value={value}>
              {value}
            </option>
          ))}
        </select>
      </div>

      <div className="card bg-base-100 shadow-md">
        <div className="overflow-x-auto">
          {loading ? (
            <div className="flex justify-center py-10">
              <span className="loading loading-spinner loading-lg text-primary" />
            </div>
          ) : (
            <table className="table table-fixed">
              <thead>
                <tr>
                  <SortableTh
                    label="Coach"
                    sortKey="coachLabel"
                    currentKey={sortKey}
                    direction={sortDirection}
                    onSort={requestSort}
                    className="w-[22%]"
                  />
                  <th className="hidden sm:table-cell w-[14%]">Type</th>
                  <SortableTh
                    label="Amount"
                    sortKey="amount"
                    currentKey={sortKey}
                    direction={sortDirection}
                    onSort={requestSort}
                    className="w-[12%]"
                  />
                  <SortableTh
                    label="Payment date"
                    sortKey="paymentDate"
                    currentKey={sortKey}
                    direction={sortDirection}
                    onSort={requestSort}
                    className="w-[14%]"
                  />
                  <th className="w-[10%]">Status</th>
                  <th className="hidden sm:table-cell w-[14%]">Remarks</th>
                  <th className="w-[14%]">Actions</th>
                </tr>
              </thead>
              <tbody>
                {pageItems.map((payment) => (
                  <tr key={payment.id}>
                    <td className="truncate" title={payment.coachLabel}>
                      {payment.coachLabel}
                    </td>
                    <td className="hidden sm:table-cell">
                      <span className={`badge badge-sm ${coachPaymentTypeBadgeClass(payment.paymentType)}`}>
                        {payment.paymentType}
                      </span>
                    </td>
                    <td>{formatCurrency(payment.amount)}</td>
                    <td>{payment.paymentDate}</td>
                    <td>
                      <span className={`badge badge-sm ${paymentStatusBadgeClass(payment.status)}`}>
                        {payment.status}
                      </span>
                    </td>
                    <td className="hidden sm:table-cell truncate" title={payment.remarks}>
                      {payment.remarks || '—'}
                    </td>
                    <td className="whitespace-nowrap">
                      {payment.status === 'UNPAID' && (
                        <button
                          type="button"
                          className="btn btn-ghost btn-xs"
                          onClick={() => setPayTarget(payment)}
                          disabled={payingId === payment.id}
                        >
                          {payingId === payment.id ? (
                            <span className="loading loading-spinner loading-xs" />
                          ) : (
                            'Mark paid'
                          )}
                        </button>
                      )}
                      <Link to={`/coach-payments/${payment.id}/edit`} className="btn btn-ghost btn-xs">
                        Edit
                      </Link>
                      <button
                        type="button"
                        className="btn btn-ghost btn-xs text-error"
                        onClick={() => setDeleteTarget(payment)}
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
                {pageItems.length === 0 && (
                  <tr>
                    <td colSpan={7} className="text-base-content/60 py-6 text-center">
                      No coach payments found.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          )}
        </div>
        <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
      </div>

      {payTarget && (
        <ConfirmModal
          title="Mark as paid"
          body={`Mark this ${formatCurrency(payTarget.amount)} payment for ${payTarget.coachLabel} as paid? There's no unpay/revert option once this is done.`}
          confirmLabel={payingId === payTarget.id ? 'Marking…' : 'Mark paid'}
          confirmClass="btn-primary"
          onConfirm={handlePay}
          onCancel={() => setPayTarget(null)}
        />
      )}

      {deleteTarget && (
        <ConfirmModal
          title="Delete coach payment"
          body={
            deleteTarget.status === 'PAID'
              ? `Permanently delete this paid payment for ${deleteTarget.coachLabel}? Its finance transaction has no link back to it and will be left orphaned — this cannot be undone.`
              : `Permanently delete this payment for ${deleteTarget.coachLabel}? This cannot be undone.`
          }
          confirmLabel={deleting ? 'Deleting…' : 'Delete'}
          onConfirm={handleDelete}
          onCancel={() => setDeleteTarget(null)}
        />
      )}
    </div>
  )
}
