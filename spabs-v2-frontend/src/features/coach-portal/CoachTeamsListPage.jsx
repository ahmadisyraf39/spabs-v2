import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import Pagination from '../../components/ui/Pagination'
import { getMyCoachProfile } from '../../lib/api/profiles'
import { getCoachTeamHistory } from '../../lib/teamRelations'
import { usePagination } from '../../lib/usePagination'

export default function CoachTeamsListPage() {
  const [history, setHistory] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [statusFilter, setStatusFilter] = useState('ACTIVE')

  useEffect(() => {
    let cancelled = false

    async function load() {
      setLoading(true)
      setError(null)
      try {
        const coach = await getMyCoachProfile()
        const entries = await getCoachTeamHistory(coach.id)
        if (cancelled) return
        setHistory(entries)
      } catch (err) {
        if (!cancelled) setError(err.message ?? 'Unable to load your teams.')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    load()
    return () => {
      cancelled = true
    }
  }, [])

  const filtered = useMemo(
    () => history.filter((entry) => !statusFilter || entry.status === statusFilter),
    [history, statusFilter],
  )

  const { pageItems, page, setPage, totalPages } = usePagination(filtered, 10)

  if (loading) {
    return (
      <div className="flex justify-center py-10">
        <span className="loading loading-spinner loading-lg text-primary" />
      </div>
    )
  }

  return (
    <div className="flex flex-col gap-4">
      <h1 className="text-2xl font-semibold">My teams</h1>

      {error && (
        <div role="alert" className="alert alert-error text-sm">
          <span>{error}</span>
        </div>
      )}

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
          <option value="ACTIVE">ACTIVE</option>
          <option value="INACTIVE">INACTIVE</option>
        </select>
      </div>

      <div className="card bg-base-100 shadow-md">
        <div className="overflow-x-auto">
          <table className="table table-fixed">
            <thead>
              <tr>
                <th className="w-[22%]">Team</th>
                <th className="hidden sm:table-cell w-[14%]">Category</th>
                <th className="hidden sm:table-cell w-[14%]">Age group</th>
                <th className="w-[12%]">My role</th>
                <th className="w-[12%]">Status</th>
                <th className="hidden sm:table-cell w-[13%]">Joined</th>
                <th className="hidden sm:table-cell w-[13%]">Left</th>
                <th className="w-[10%]">Actions</th>
              </tr>
            </thead>
            <tbody>
              {pageItems.map((entry) => (
                <tr key={entry.linkId}>
                  <td className="truncate">{entry.name}</td>
                  <td className="hidden sm:table-cell">
                    <span className="badge badge-ghost">{entry.category}</span>
                  </td>
                  <td className="hidden sm:table-cell">
                    <span className="badge badge-ghost">{entry.ageGroup}</span>
                  </td>
                  <td>
                    <span className="badge badge-ghost badge-sm">{entry.role}</span>
                  </td>
                  <td>
                    <span
                      className={`badge badge-soft badge-sm ${entry.status === 'ACTIVE' ? 'badge-success' : 'badge-neutral'}`}
                    >
                      {entry.status}
                    </span>
                  </td>
                  <td className="hidden sm:table-cell">{entry.joinedAt}</td>
                  <td className="hidden sm:table-cell">{entry.leftAt ?? '—'}</td>
                  <td>
                    <Link to={`/dashboard/coach/teams/${entry.id}`} className="btn btn-ghost btn-xs text-info">
                      View
                    </Link>
                  </td>
                </tr>
              ))}
              {pageItems.length === 0 && (
                <tr>
                  <td colSpan={8} className="text-base-content/60 py-6 text-center">
                    No team assignments found.
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
