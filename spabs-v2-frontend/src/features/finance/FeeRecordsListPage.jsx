import { useEffect, useMemo, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import ConfirmModal from '../../components/ui/ConfirmModal'
import Pagination from '../../components/ui/Pagination'
import SortableTh from '../../components/ui/SortableTh'
import { deleteFeeRecord, getFeeRecordsByTeam, payFeeRecord } from '../../lib/api/feeRecords'
import { getPlayers } from '../../lib/api/players'
import { getTeams } from '../../lib/api/teams'
import { formatCurrency } from '../../lib/formatCurrency'
import { usePagination } from '../../lib/usePagination'
import { useSort } from '../../lib/useSort'
import { PAYMENT_STATUSES, feeStatusBadgeClass } from './feeEnums'

export default function FeeRecordsListPage() {
  const [searchParams] = useSearchParams()
  const [teams, setTeams] = useState([])
  const [records, setRecords] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [search, setSearch] = useState(searchParams.get('search') ?? '')
  const [teamFilter, setTeamFilter] = useState(searchParams.get('teamId') ?? '')
  const [statusFilter, setStatusFilter] = useState('')
  const [monthFilter, setMonthFilter] = useState(searchParams.get('month') ?? '')
  const [payingId, setPayingId] = useState(null)
  const [payTarget, setPayTarget] = useState(null)
  const [deleteTarget, setDeleteTarget] = useState(null)
  const [deleting, setDeleting] = useState(false)

  async function loadRecords() {
    setLoading(true)
    setError(null)
    try {
      const [teamList, playerList] = await Promise.all([getTeams(), getPlayers()])
      setTeams(teamList)
      const perTeamRecords = await Promise.all(teamList.map((t) => getFeeRecordsByTeam(t.id)))
      const playersById = new Map(playerList.map((p) => [p.id, p]))
      const teamsById = new Map(teamList.map((t) => [t.id, t]))
      setRecords(
        perTeamRecords.flat().map((record) => ({
          ...record,
          playerName: playersById.get(record.playerId)?.fullName ?? `Player #${record.playerId}`,
          teamName: teamsById.get(record.teamId)?.name ?? `Team #${record.teamId}`,
        })),
      )
    } catch (err) {
      setError(err.message ?? 'Unable to load fee records.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadRecords()
  }, [])

  const monthOptions = useMemo(() => {
    const months = new Set(records.map((record) => record.dueDate.slice(0, 7)))
    return Array.from(months)
      .sort()
      .reverse()
      .map((value) => {
        const [year, month] = value.split('-').map(Number)
        return {
          value,
          label: new Date(year, month - 1, 1).toLocaleDateString(undefined, {
            month: 'long',
            year: 'numeric',
          }),
        }
      })
  }, [records])

  const filteredRecords = useMemo(() => {
    const term = search.trim().toLowerCase()
    return records.filter((record) => {
      const matchesTerm = !term || record.playerName.toLowerCase().includes(term)
      const matchesTeam = !teamFilter || record.teamId === Number(teamFilter)
      const matchesStatus =
        !statusFilter ||
        (statusFilter === 'OVERDUE' ? record.overdue : record.status === statusFilter)
      const matchesMonth = !monthFilter || record.dueDate.slice(0, 7) === monthFilter
      return matchesTerm && matchesTeam && matchesStatus && matchesMonth
    })
  }, [records, search, teamFilter, statusFilter, monthFilter])

  const { sorted, sortKey, sortDirection, requestSort } = useSort(filteredRecords, 'dueDate', 'desc')
  const { pageItems, page, setPage, totalPages } = usePagination(sorted, 10)

  function resetToFirstPage() {
    setPage(1)
  }

  async function handlePay() {
    const record = payTarget
    setPayingId(record.id)
    setError(null)
    try {
      const updated = await payFeeRecord(record.id)
      setRecords((prev) =>
        prev.map((r) =>
          r.id === record.id
            ? { ...r, status: updated.status, paidAt: updated.paidAt, overdue: updated.overdue }
            : r,
        ),
      )
      setPayTarget(null)
    } catch (err) {
      setError(err.message ?? 'Unable to mark fee as paid.')
    } finally {
      setPayingId(null)
    }
  }

  async function handleDelete() {
    setDeleting(true)
    try {
      await deleteFeeRecord(deleteTarget.id)
      setRecords((prev) => prev.filter((r) => r.id !== deleteTarget.id))
      setDeleteTarget(null)
    } catch (err) {
      setError(err.message ?? 'Unable to delete fee record.')
    } finally {
      setDeleting(false)
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">Fees</h1>
        <div className="flex gap-2">
          <Link to="/fee-items" className="btn btn-outline btn-sm">
            Manage fee items
          </Link>
          <Link to="/fee-records/new" className="btn btn-primary btn-sm">
            New fee record
          </Link>
        </div>
      </div>

      {error && (
        <div role="alert" className="alert alert-error text-sm">
          <span>{error}</span>
        </div>
      )}

      <div className="flex flex-col gap-3 sm:flex-row sm:items-center w-full">
        <input
          type="text"
          placeholder="Search player"
          className="input input-bordered input-sm w-full sm:min-w-[200px] sm:flex-1"
          value={search}
          onChange={(e) => {
            setSearch(e.target.value)
            resetToFirstPage()
          }}
        />
        <div className="flex gap-3 w-full sm:contents">
          <select
            className="select select-bordered select-sm flex-1 sm:flex-initial sm:ml-auto"
            value={teamFilter}
            onChange={(e) => {
              setTeamFilter(e.target.value)
              resetToFirstPage()
            }}
          >
            <option value="">All teams</option>
            {teams.map((team) => (
              <option key={team.id} value={team.id}>
                {team.name}
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
            <option value="OVERDUE">OVERDUE</option>
          </select>
          <select
            className="select select-bordered select-sm flex-1 sm:flex-initial"
            value={monthFilter}
            onChange={(e) => {
              setMonthFilter(e.target.value)
              resetToFirstPage()
            }}
          >
            <option value="">All months</option>
            {monthOptions.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
        </div>
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
                    label="Player"
                    sortKey="playerName"
                    currentKey={sortKey}
                    direction={sortDirection}
                    onSort={requestSort}
                    className="w-[16%]"
                  />
                  <SortableTh
                    label="Team"
                    sortKey="teamName"
                    currentKey={sortKey}
                    direction={sortDirection}
                    onSort={requestSort}
                    className="hidden sm:table-cell w-[16%]"
                  />
                  <SortableTh
                    label="Fee item"
                    sortKey="feeItemName"
                    currentKey={sortKey}
                    direction={sortDirection}
                    onSort={requestSort}
                    className="hidden sm:table-cell w-[16%]"
                  />
                  <SortableTh
                    label="Amount"
                    sortKey="amount"
                    currentKey={sortKey}
                    direction={sortDirection}
                    onSort={requestSort}
                    className="w-[10%]"
                  />
                  <th className="w-[10%]">Status</th>
                  <SortableTh
                    label="Due"
                    sortKey="dueDate"
                    currentKey={sortKey}
                    direction={sortDirection}
                    onSort={requestSort}
                    className="w-[10%]"
                  />
                  <th className="w-[10%]">Paid</th>
                  <th className="w-[12%]">Actions</th>
                </tr>
              </thead>
              <tbody>
                {pageItems.map((record) => (
                  <tr key={record.id}>
                    <td className="truncate">
                      <Link to={`/players/${record.playerId}`} className="link link-primary text-sm">
                        {record.playerName}
                      </Link>
                    </td>
                    <td className="hidden sm:table-cell truncate">
                      <Link to={`/teams/${record.teamId}`} className="link link-primary text-sm">
                        {record.teamName}
                      </Link>
                    </td>
                    <td className="hidden sm:table-cell truncate" title={record.feeItemName}>
                      {record.feeItemName}
                    </td>
                    <td>{formatCurrency(record.amount)}</td>
                    <td>
                      <span className={`badge badge-sm ${feeStatusBadgeClass(record)}`}>
                        {record.overdue ? 'OVERDUE' : record.status}
                      </span>
                    </td>
                    <td>{record.dueDate}</td>
                    <td>{record.paidAt ?? '—'}</td>
                    <td className="whitespace-nowrap">
                      {record.status === 'UNPAID' && (
                        <button
                          type="button"
                          className="btn btn-ghost btn-xs"
                          onClick={() => setPayTarget(record)}
                          disabled={payingId === record.id}
                        >
                          {payingId === record.id ? (
                            <span className="loading loading-spinner loading-xs" />
                          ) : (
                            'Mark paid'
                          )}
                        </button>
                      )}
                      <button
                        type="button"
                        className="btn btn-ghost btn-xs text-error"
                        onClick={() => setDeleteTarget(record)}
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
                {pageItems.length === 0 && (
                  <tr>
                    <td colSpan={8} className="text-base-content/60 py-6 text-center">
                      No fee records found.
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
          body={`Mark this ${formatCurrency(payTarget.amount)} fee record for ${payTarget.playerName} as paid? There's no unpay/revert option once this is done.`}
          confirmLabel={payingId === payTarget.id ? 'Marking…' : 'Mark paid'}
          confirmClass="btn-primary"
          onConfirm={handlePay}
          onCancel={() => setPayTarget(null)}
        />
      )}

      {deleteTarget && (
        <ConfirmModal
          title="Delete fee record"
          body={
            deleteTarget.status === 'PAID'
              ? `Permanently delete this paid fee record for ${deleteTarget.playerName}? Its payment transaction has no link back to it and will be left orphaned — this cannot be undone.`
              : `Permanently delete this fee record for ${deleteTarget.playerName}? This cannot be undone.`
          }
          confirmLabel={deleting ? 'Deleting…' : 'Delete'}
          onConfirm={handleDelete}
          onCancel={() => setDeleteTarget(null)}
        />
      )}
    </div>
  )
}
