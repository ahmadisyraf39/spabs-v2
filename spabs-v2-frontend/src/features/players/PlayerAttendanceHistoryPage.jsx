import { useEffect, useMemo, useState } from 'react'
import { Link, useParams, useSearchParams } from 'react-router-dom'
import Pagination from '../../components/ui/Pagination'
import SortableTh from '../../components/ui/SortableTh'
import { getAttendanceHistoryForPlayer } from '../../lib/activityRelations'
import { getPlayer } from '../../lib/api/players'
import { formatDateTime } from '../../lib/dateTimeUtils'
import { usePagination } from '../../lib/usePagination'
import { useSort } from '../../lib/useSort'
import { ACTIVITY_TYPES, ATTENDANCE_STATUSES, attendanceStatusBadgeClass } from '../activities/activityEnums'

export default function PlayerAttendanceHistoryPage() {
  const { id } = useParams()
  const [searchParams] = useSearchParams()
  const [player, setPlayer] = useState(null)
  const [records, setRecords] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [statusFilter, setStatusFilter] = useState('')
  const [typeFilter, setTypeFilter] = useState('')
  const [teamFilter, setTeamFilter] = useState(searchParams.get('teamId') ?? '')

  useEffect(() => {
    let cancelled = false

    async function load() {
      setLoading(true)
      setError(null)
      try {
        const [p, history] = await Promise.all([getPlayer(id), getAttendanceHistoryForPlayer(id)])
        if (cancelled) return
        setPlayer(p)
        setRecords(history)
      } catch (err) {
        if (!cancelled) setError(err.message ?? 'Unable to load attendance history.')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    load()
    return () => {
      cancelled = true
    }
  }, [id])

  const teamOptions = useMemo(() => {
    const byId = new Map()
    for (const r of records) {
      if (r.teamId != null && !byId.has(r.teamId)) byId.set(r.teamId, r.teamName)
    }
    return Array.from(byId, ([teamId, teamName]) => ({ teamId, teamName })).sort((a, b) =>
      a.teamName.localeCompare(b.teamName),
    )
  }, [records])

  const filteredRecords = useMemo(
    () =>
      records.filter((r) => {
        const matchesStatus = !statusFilter || r.status === statusFilter
        const matchesType = !typeFilter || r.type === typeFilter
        const matchesTeam = !teamFilter || String(r.teamId) === teamFilter
        return matchesStatus && matchesType && matchesTeam
      }),
    [records, statusFilter, typeFilter, teamFilter],
  )

  const { sorted, sortKey, sortDirection, requestSort } = useSort(filteredRecords, 'startAt', 'desc')
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
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">{player.fullName}'s attendance history</h1>
        <Link to={`/players/${id}`} className="btn btn-ghost btn-sm">
          Back to player
        </Link>
      </div>

      <div className="flex gap-3 sm:justify-end">
        <select
          className="select select-bordered select-sm flex-1 sm:flex-initial"
          value={teamFilter}
          onChange={(e) => {
            setTeamFilter(e.target.value)
            resetToFirstPage()
          }}
        >
          <option value="">All teams</option>
          {teamOptions.map((option) => (
            <option key={option.teamId} value={option.teamId}>
              {option.teamName}
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
          <option value="">All categories</option>
          {ACTIVITY_TYPES.map((value) => (
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
          {ATTENDANCE_STATUSES.map((value) => (
            <option key={value} value={value}>
              {value}
            </option>
          ))}
        </select>
      </div>

      <div className="card bg-base-100 shadow-md">
        <div className="overflow-x-auto">
          {pageItems.length === 0 ? (
            <p className="text-base-content/60 p-6 text-sm">No attendance records found.</p>
          ) : (
            <table className="table table-fixed">
              <thead>
                <tr>
                  <SortableTh
                    label="Activity"
                    sortKey="displayTitle"
                    currentKey={sortKey}
                    direction={sortDirection}
                    onSort={requestSort}
                    className="w-[26%]"
                  />
                  <SortableTh
                    label="Category"
                    sortKey="type"
                    currentKey={sortKey}
                    direction={sortDirection}
                    onSort={requestSort}
                    className="hidden sm:table-cell w-[14%]"
                  />
                  <SortableTh
                    label="Team"
                    sortKey="teamName"
                    currentKey={sortKey}
                    direction={sortDirection}
                    onSort={requestSort}
                    className="w-[20%]"
                  />
                  <SortableTh
                    label="Date"
                    sortKey="startAt"
                    currentKey={sortKey}
                    direction={sortDirection}
                    onSort={requestSort}
                    className="w-[18%]"
                  />
                  <th className="w-[10%]">Status</th>
                  <th className="hidden sm:table-cell w-[12%]">Notes</th>
                </tr>
              </thead>
              <tbody>
                {pageItems.map((record) => (
                  <tr key={record.id}>
                    <td className="truncate" title={record.displayTitle}>
                      {record.activityId ? (
                        <Link to={`/activities/${record.activityId}`} className="link link-primary text-sm">
                          {record.displayTitle}
                        </Link>
                      ) : (
                        record.displayTitle
                      )}
                    </td>
                    <td className="hidden sm:table-cell">
                      {record.type && <span className="badge badge-ghost badge-sm">{record.type}</span>}
                    </td>
                    <td>
                      {record.teamId ? (
                        <Link to={`/teams/${record.teamId}`} className="link link-primary text-sm">
                          {record.teamName}
                        </Link>
                      ) : (
                        <span className="text-sm">{record.teamName}</span>
                      )}
                    </td>
                    <td>{formatDateTime(record.startAt) ?? '—'}</td>
                    <td>
                      <span className={`badge badge-sm ${attendanceStatusBadgeClass(record.status)}`}>
                        {record.status}
                      </span>
                    </td>
                    <td className="hidden sm:table-cell truncate text-sm" title={record.notes ?? ''}>
                      {record.notes || '—'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
        <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
      </div>
    </div>
  )
}
