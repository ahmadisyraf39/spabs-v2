import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import ConfirmModal from '../../components/ui/ConfirmModal'
import Pagination from '../../components/ui/Pagination'
import SortableTh from '../../components/ui/SortableTh'
import { deleteTeam, getTeams } from '../../lib/api/teams'
import { deleteAllActivitiesForTeam } from '../../lib/activityRelations'
import { AGE_GROUPS } from '../../lib/ageUtils'
import {
  deleteAllAnnouncementsForTeam,
  deleteAllCoachLinksForTeam,
  deleteAllFeeRecordsForTeam,
  deleteAllRosterLinksForTeam,
} from '../../lib/teamRelations'
import { usePagination } from '../../lib/usePagination'
import { useSort } from '../../lib/useSort'
import { TEAM_CATEGORIES } from './teamEnums'

export default function TeamsListPage() {
  const [teams, setTeams] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [search, setSearch] = useState('')
  const [categoryFilter, setCategoryFilter] = useState('')
  const [ageGroupFilter, setAgeGroupFilter] = useState('')
  const [deleteTarget, setDeleteTarget] = useState(null)
  const [deleting, setDeleting] = useState(false)

  async function loadTeams() {
    setLoading(true)
    setError(null)
    try {
      setTeams(await getTeams())
    } catch (err) {
      setError(err.message ?? 'Unable to load teams.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadTeams()
  }, [])

  const filteredTeams = useMemo(() => {
    const term = search.trim().toLowerCase()
    return teams.filter((t) => {
      const matchesTerm = !term || t.name.toLowerCase().includes(term)
      const matchesCategory = !categoryFilter || t.category === categoryFilter
      const matchesAgeGroup = !ageGroupFilter || t.ageGroup === ageGroupFilter
      return matchesTerm && matchesCategory && matchesAgeGroup
    })
  }, [teams, search, categoryFilter, ageGroupFilter])

  const { sorted, sortKey, sortDirection, requestSort } = useSort(filteredTeams, 'name')
  const { pageItems, page, setPage, totalPages } = usePagination(sorted, 10)

  function resetToFirstPage() {
    setPage(1)
  }

  async function handleDelete() {
    setDeleting(true)
    try {
      await deleteAllRosterLinksForTeam(deleteTarget.id)
      await deleteAllCoachLinksForTeam(deleteTarget.id)
      await deleteAllActivitiesForTeam(deleteTarget.id)
      await deleteAllAnnouncementsForTeam(deleteTarget.id)
      await deleteAllFeeRecordsForTeam(deleteTarget.id)
      await deleteTeam(deleteTarget.id)
      setTeams((prev) => prev.filter((t) => t.id !== deleteTarget.id))
      setDeleteTarget(null)
    } catch (err) {
      setError(err.message ?? 'Unable to delete team.')
    } finally {
      setDeleting(false)
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">Teams</h1>
        <Link to="/teams/new" className="btn btn-primary btn-sm">
          New team
        </Link>
      </div>

      {error && (
        <div role="alert" className="alert alert-error text-sm">
          <span>{error}</span>
        </div>
      )}

      <div className="flex flex-col gap-3 sm:flex-row sm:items-center w-full">
        <input
          type="text"
          placeholder="Search name"
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
            value={categoryFilter}
            onChange={(e) => {
              setCategoryFilter(e.target.value)
              resetToFirstPage()
            }}
          >
            <option value="">All categories</option>
            {TEAM_CATEGORIES.map((value) => (
              <option key={value} value={value}>
                {value}
              </option>
            ))}
          </select>
          <select
            className="select select-bordered select-sm flex-1 sm:flex-initial"
            value={ageGroupFilter}
            onChange={(e) => {
              setAgeGroupFilter(e.target.value)
              resetToFirstPage()
            }}
          >
            <option value="">All age groups</option>
            {AGE_GROUPS.map((value) => (
              <option key={value} value={value}>
                {value}
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
                    label="Name"
                    sortKey="name"
                    currentKey={sortKey}
                    direction={sortDirection}
                    onSort={requestSort}
                    className="w-[38%]"
                  />
                  <SortableTh
                    label="Category"
                    sortKey="category"
                    currentKey={sortKey}
                    direction={sortDirection}
                    onSort={requestSort}
                    className="w-[22%]"
                  />
                  <SortableTh
                    label="Age group"
                    sortKey="ageGroup"
                    currentKey={sortKey}
                    direction={sortDirection}
                    onSort={requestSort}
                    className="hidden sm:table-cell w-[22%]"
                  />
                  <th className="w-[18%]">Actions</th>
                </tr>
              </thead>
              <tbody>
                {pageItems.map((team) => (
                  <tr key={team.id}>
                    <td className="truncate" title={team.name}>
                      {team.name}
                    </td>
                    <td>
                      <span className="badge badge-ghost">{team.category}</span>
                    </td>
                    <td className="hidden sm:table-cell">
                      <span className="badge badge-ghost">{team.ageGroup}</span>
                    </td>
                    <td className="whitespace-nowrap">
                      <Link to={`/teams/${team.id}`} className="btn btn-ghost btn-xs text-info">
                        Details
                      </Link>
                      <Link to={`/teams/${team.id}/edit`} className="btn btn-ghost btn-xs">
                        Edit
                      </Link>
                      <button
                        type="button"
                        className="btn btn-ghost btn-xs text-error"
                        onClick={() => setDeleteTarget(team)}
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
                {pageItems.length === 0 && (
                  <tr>
                    <td colSpan={4} className="text-base-content/60 py-6 text-center">
                      No teams found.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          )}
        </div>
        <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
      </div>

      {deleteTarget && (
        <ConfirmModal
          title="Delete team"
          body={`Permanently delete ${deleteTarget.name}? This also removes its roster, coaching assignments, and scheduled activities (with their attendance records). This cannot be undone.`}
          confirmLabel={deleting ? 'Deleting…' : 'Delete'}
          onConfirm={handleDelete}
          onCancel={() => setDeleteTarget(null)}
        />
      )}
    </div>
  )
}
