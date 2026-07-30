import { useEffect, useMemo, useState } from 'react'
import Pagination from '../../components/ui/Pagination'
import SortableTh from '../../components/ui/SortableTh'
import { getCoachPaymentsByCoach } from '../../lib/api/coachPayments'
import { getMyCoachProfile } from '../../lib/api/profiles'
import { formatCurrency } from '../../lib/formatCurrency'
import { usePagination } from '../../lib/usePagination'
import { useSort } from '../../lib/useSort'
import { COACH_PAYMENT_TYPES, coachPaymentTypeBadgeClass, paymentStatusBadgeClass } from '../finance/financeLedgerEnums'
import { PAYMENT_STATUSES } from '../finance/feeEnums'

export default function CoachPayrollPage() {
  const [payments, setPayments] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [typeFilter, setTypeFilter] = useState('')
  const [statusFilter, setStatusFilter] = useState('')

  useEffect(() => {
    let cancelled = false

    async function load() {
      setLoading(true)
      setError(null)
      try {
        const coach = await getMyCoachProfile()
        const paymentList = await getCoachPaymentsByCoach(coach.id)
        if (cancelled) return
        setPayments(paymentList)
      } catch (err) {
        if (!cancelled) setError(err.message ?? 'Unable to load your payment history.')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    load()
    return () => {
      cancelled = true
    }
  }, [])

  const filteredPayments = useMemo(() => {
    return payments.filter((p) => {
      const matchesType = !typeFilter || p.paymentType === typeFilter
      const matchesStatus = !statusFilter || p.status === statusFilter
      return matchesType && matchesStatus
    })
  }, [payments, typeFilter, statusFilter])

  const { sorted, sortKey, sortDirection, requestSort } = useSort(filteredPayments, 'paymentDate', 'desc')
  const { pageItems, page, setPage, totalPages } = usePagination(sorted, 10)

  function resetToFirstPage() {
    setPage(1)
  }

  if (loading) {
    return (
      <div className="flex justify-center py-10">
        <span className="loading loading-spinner loading-lg text-primary" />
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-4">
      <h1 className="text-2xl font-semibold">My payment history</h1>

      {error && (
        <div role="alert" className="alert alert-error text-sm">
          <span>{error}</span>
        </div>
      )}

      <div className="flex gap-3 sm:justify-end">
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
          <table className="table table-fixed">
            <thead>
              <tr>
                <th className="w-[16%]">Type</th>
                <SortableTh
                  label="Amount"
                  sortKey="amount"
                  currentKey={sortKey}
                  direction={sortDirection}
                  onSort={requestSort}
                  className="w-[16%]"
                />
                <SortableTh
                  label="Payment date"
                  sortKey="paymentDate"
                  currentKey={sortKey}
                  direction={sortDirection}
                  onSort={requestSort}
                  className="w-[18%]"
                />
                <th className="w-[14%]">Status</th>
                <th className="w-[36%]">Remarks</th>
              </tr>
            </thead>
            <tbody>
              {pageItems.map((p) => (
                <tr key={p.id}>
                  <td>
                    <span className={`badge badge-sm ${coachPaymentTypeBadgeClass(p.paymentType)}`}>
                      {p.paymentType}
                    </span>
                  </td>
                  <td>{formatCurrency(p.amount)}</td>
                  <td>{p.paymentDate}</td>
                  <td>
                    <span className={`badge badge-sm ${paymentStatusBadgeClass(p.status)}`}>{p.status}</span>
                  </td>
                  <td className="truncate" title={p.remarks}>
                    {p.remarks || '—'}
                  </td>
                </tr>
              ))}
              {pageItems.length === 0 && (
                <tr>
                  <td colSpan={5} className="text-base-content/60 py-6 text-center">
                    No payments recorded yet.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
        <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
      </div>
    </div>
  )
}
