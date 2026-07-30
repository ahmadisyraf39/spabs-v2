import { useEffect, useMemo, useState } from 'react'
import { useParams } from 'react-router-dom'
import Pagination from '../../components/ui/Pagination'
import SortableTh from '../../components/ui/SortableTh'
import { getFeeRecordsByPlayer } from '../../lib/api/feeRecords'
import { getPlayer } from '../../lib/api/players'
import { formatCurrency } from '../../lib/formatCurrency'
import { usePagination } from '../../lib/usePagination'
import { useSort } from '../../lib/useSort'
import { PAYMENT_STATUSES, feeStatusBadgeClass } from '../finance/feeEnums'

export default function ParentChildFeesPage() {
  const { id } = useParams()
  const [player, setPlayer] = useState(null)
  const [records, setRecords] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [statusFilter, setStatusFilter] = useState('')

  useEffect(() => {
    let cancelled = false

    async function load() {
      setLoading(true)
      setError(null)
      try {
        const [p, recordList] = await Promise.all([getPlayer(id), getFeeRecordsByPlayer(id)])
        if (cancelled) return
        setPlayer(p)
        setRecords(recordList)
      } catch (err) {
        if (!cancelled) setError(err.message ?? 'Unable to load fee records.')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    load()
    return () => {
      cancelled = true
    }
  }, [id])

  const filteredRecords = useMemo(() => {
    return records.filter((r) => {
      if (!statusFilter) return true
      if (statusFilter === 'OVERDUE') return r.overdue
      return r.status === statusFilter
    })
  }, [records, statusFilter])

  const { sorted, sortKey, sortDirection, requestSort } = useSort(filteredRecords, 'dueDate', 'desc')
  const { pageItems, page, setPage, totalPages } = usePagination(sorted, 10)

  if (loading) {
    return (
      <div className="flex justify-center py-10">
        <span className="loading loading-spinner loading-lg text-primary" />
      </div>
    )
  }

  if (error) {
    return (
      <div className="mx-auto max-w-lg">
        <div role="alert" className="alert alert-error text-sm">
          <span>{error}</span>
        </div>
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-4">
      <h1 className="text-2xl font-semibold">{player.fullName}'s fees</h1>

      <div className="flex gap-3 sm:justify-end">
        <select
          className="select select-bordered select-sm flex-1 sm:flex-initial"
          value={statusFilter}
          onChange={(e) => {
            setStatusFilter(e.target.value)
            setPage(1)
          }}
        >
          <option value="">All statuses</option>
          {PAYMENT_STATUSES.map((value) => (
            <option key={value} value={value}>
              {value}
            </option>
          ))}
          <option value="OVERDUE">OVERDUE</option>
        </select>
      </div>

      <div className="card bg-base-100 shadow-md">
        <div className="overflow-x-auto">
          <table className="table table-fixed">
            <thead>
              <tr>
                <th className="w-[30%]">Fee item</th>
                <SortableTh
                  label="Amount"
                  sortKey="amount"
                  currentKey={sortKey}
                  direction={sortDirection}
                  onSort={requestSort}
                  className="w-[16%]"
                />
                <SortableTh
                  label="Due"
                  sortKey="dueDate"
                  currentKey={sortKey}
                  direction={sortDirection}
                  onSort={requestSort}
                  className="w-[18%]"
                />
                <th className="w-[16%]">Status</th>
                <th className="w-[20%]">Paid</th>
              </tr>
            </thead>
            <tbody>
              {pageItems.map((r) => (
                <tr key={r.id}>
                  <td className="truncate">{r.feeItemName}</td>
                  <td>{formatCurrency(r.amount)}</td>
                  <td>{r.dueDate}</td>
                  <td>
                    <span className={`badge badge-sm ${feeStatusBadgeClass(r)}`}>
                      {r.overdue ? 'OVERDUE' : r.status}
                    </span>
                  </td>
                  <td>{r.paidAt ?? '—'}</td>
                </tr>
              ))}
              {pageItems.length === 0 && (
                <tr>
                  <td colSpan={5} className="text-base-content/60 py-6 text-center">
                    No fee records.
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
