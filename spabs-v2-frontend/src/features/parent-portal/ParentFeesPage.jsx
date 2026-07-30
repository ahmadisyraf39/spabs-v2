import { useEffect, useMemo, useState } from 'react'
import Pagination from '../../components/ui/Pagination'
import SortableTh from '../../components/ui/SortableTh'
import { getFeeRecordsByPlayer } from '../../lib/api/feeRecords'
import { getMyParentProfile } from '../../lib/api/profiles'
import { formatCurrency } from '../../lib/formatCurrency'
import { getPlayersForParent } from '../../lib/playerRelations'
import { usePagination } from '../../lib/usePagination'
import { useSort } from '../../lib/useSort'
import { PAYMENT_STATUSES, feeStatusBadgeClass } from '../finance/feeEnums'

export default function ParentFeesPage() {
  const [children, setChildren] = useState([])
  const [records, setRecords] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [childFilter, setChildFilter] = useState('')
  const [statusFilter, setStatusFilter] = useState('')

  useEffect(() => {
    let cancelled = false

    async function load() {
      setLoading(true)
      setError(null)
      try {
        const parent = await getMyParentProfile()
        const kids = await getPlayersForParent(parent.id)
        if (cancelled) return
        const recordLists = await Promise.all(kids.map((k) => getFeeRecordsByPlayer(k.id)))
        if (cancelled) return

        const childrenById = new Map(kids.map((k) => [k.id, k]))
        setChildren(kids)
        setRecords(
          recordLists.flat().map((r) => ({
            ...r,
            playerName: childrenById.get(r.playerId)?.fullName ?? `Player #${r.playerId}`,
          })),
        )
      } catch (err) {
        if (!cancelled) setError(err.message ?? 'Unable to load fees.')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    load()
    return () => {
      cancelled = true
    }
  }, [])

  const filteredRecords = useMemo(() => {
    return records.filter((r) => {
      const matchesChild = !childFilter || r.playerId === Number(childFilter)
      const matchesStatus =
        !statusFilter || (statusFilter === 'OVERDUE' ? r.overdue : r.status === statusFilter)
      return matchesChild && matchesStatus
    })
  }, [records, childFilter, statusFilter])

  const { sorted, sortKey, sortDirection, requestSort } = useSort(filteredRecords, 'dueDate', 'desc')
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
      <h1 className="text-2xl font-semibold">Fees</h1>

      {error && (
        <div role="alert" className="alert alert-error text-sm">
          <span>{error}</span>
        </div>
      )}

      <div className="flex gap-3 sm:justify-end">
        {children.length > 1 && (
          <select
            className="select select-bordered select-sm flex-1 sm:flex-initial"
            value={childFilter}
            onChange={(e) => {
              setChildFilter(e.target.value)
              resetToFirstPage()
            }}
          >
            <option value="">All children</option>
            {children.map((child) => (
              <option key={child.id} value={child.id}>
                {child.fullName}
              </option>
            ))}
          </select>
        )}
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
          <option value="OVERDUE">OVERDUE</option>
        </select>
      </div>

      <div className="card bg-base-100 shadow-md">
        <div className="overflow-x-auto">
          <table className="table table-fixed">
            <thead>
              <tr>
                <th className="w-[18%]">Player</th>
                <th className="hidden sm:table-cell w-[24%]">Fee item</th>
                <SortableTh
                  label="Amount"
                  sortKey="amount"
                  currentKey={sortKey}
                  direction={sortDirection}
                  onSort={requestSort}
                  className="w-[14%]"
                />
                <SortableTh
                  label="Due"
                  sortKey="dueDate"
                  currentKey={sortKey}
                  direction={sortDirection}
                  onSort={requestSort}
                  className="w-[16%]"
                />
                <th className="w-[14%]">Status</th>
                <th className="hidden sm:table-cell w-[14%]">Paid</th>
              </tr>
            </thead>
            <tbody>
              {pageItems.map((r) => (
                <tr key={r.id}>
                  <td className="truncate">{r.playerName}</td>
                  <td className="hidden sm:table-cell truncate" title={r.feeItemName}>
                    {r.feeItemName}
                  </td>
                  <td>{formatCurrency(r.amount)}</td>
                  <td>{r.dueDate}</td>
                  <td>
                    <span className={`badge badge-sm ${feeStatusBadgeClass(r)}`}>
                      {r.overdue ? 'OVERDUE' : r.status}
                    </span>
                  </td>
                  <td className="hidden sm:table-cell">{r.paidAt ?? '—'}</td>
                </tr>
              ))}
              {pageItems.length === 0 && (
                <tr>
                  <td colSpan={6} className="text-base-content/60 py-6 text-center">
                    No fee records found.
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
