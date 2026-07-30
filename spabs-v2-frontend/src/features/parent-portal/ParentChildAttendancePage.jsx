import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import Pagination from '../../components/ui/Pagination'
import SortableTh from '../../components/ui/SortableTh'
import { getPlayer } from '../../lib/api/players'
import { getAttendanceHistoryForPlayer } from '../../lib/activityRelations'
import { formatDateTime } from '../../lib/dateTimeUtils'
import { usePagination } from '../../lib/usePagination'
import { useSort } from '../../lib/useSort'
import { attendanceStatusBadgeClass } from '../activities/activityEnums'

export default function ParentChildAttendancePage() {
  const { id } = useParams()
  const [player, setPlayer] = useState(null)
  const [records, setRecords] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

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

  const { sorted, sortKey, sortDirection, requestSort } = useSort(records, 'startAt', 'desc')
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
      <h1 className="text-2xl font-semibold">{player.fullName}'s attendance history</h1>

      <div className="card bg-base-100 shadow-md">
        <div className="overflow-x-auto">
          <table className="table table-fixed">
            <thead>
              <tr>
                <SortableTh
                  label="Activity"
                  sortKey="displayTitle"
                  currentKey={sortKey}
                  direction={sortDirection}
                  onSort={requestSort}
                  className="w-[30%]"
                />
                <th className="w-[14%]">Category</th>
                <th className="w-[18%]">Team</th>
                <SortableTh
                  label="Date"
                  sortKey="startAt"
                  currentKey={sortKey}
                  direction={sortDirection}
                  onSort={requestSort}
                  className="w-[16%]"
                />
                <th className="w-[12%]">Status</th>
                <th className="hidden sm:table-cell w-[10%]">Notes</th>
              </tr>
            </thead>
            <tbody>
              {pageItems.map((record) => (
                <tr key={record.id}>
                  <td className="truncate" title={record.displayTitle}>
                    {record.activityId ? (
                      <Link
                        to={`/dashboard/parent/activities/${record.activityId}`}
                        className="link link-primary text-sm"
                      >
                        {record.displayTitle}
                      </Link>
                    ) : (
                      record.displayTitle
                    )}
                  </td>
                  <td>{record.type && <span className="badge badge-ghost badge-sm">{record.type}</span>}</td>
                  <td className="truncate">{record.teamName}</td>
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
              {pageItems.length === 0 && (
                <tr>
                  <td colSpan={6} className="text-base-content/60 py-6 text-center">
                    No attendance recorded yet.
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
