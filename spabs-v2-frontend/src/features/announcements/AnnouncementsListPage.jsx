import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import ConfirmModal from '../../components/ui/ConfirmModal'
import Pagination from '../../components/ui/Pagination'
import SortableTh from '../../components/ui/SortableTh'
import { deleteAnnouncement, getAnnouncements } from '../../lib/api/announcements'
import { getTeams } from '../../lib/api/teams'
import { usePagination } from '../../lib/usePagination'
import { useSort } from '../../lib/useSort'

const TEAM_FILTER_ACADEMY_WIDE = 'ACADEMY_WIDE'

export default function AnnouncementsListPage() {
  const [teams, setTeams] = useState([])
  const [announcements, setAnnouncements] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [search, setSearch] = useState('')
  const [teamFilter, setTeamFilter] = useState('')
  const [deleteTarget, setDeleteTarget] = useState(null)
  const [deleting, setDeleting] = useState(false)

  async function loadAnnouncements() {
    setLoading(true)
    setError(null)
    try {
      const [teamList, announcementList] = await Promise.all([getTeams(), getAnnouncements()])
      const teamsById = new Map(teamList.map((t) => [t.id, t]))
      setTeams(teamList)
      setAnnouncements(
        announcementList.map((a) => ({
          ...a,
          teamName: a.teamId ? (teamsById.get(a.teamId)?.name ?? `Team #${a.teamId}`) : null,
        })),
      )
    } catch (err) {
      setError(err.message ?? 'Unable to load announcements.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadAnnouncements()
  }, [])

  const filteredAnnouncements = useMemo(() => {
    const term = search.trim().toLowerCase()
    return announcements.filter((a) => {
      const matchesTerm =
        !term || a.title.toLowerCase().includes(term) || a.content.toLowerCase().includes(term)
      const matchesTeam =
        !teamFilter ||
        (teamFilter === TEAM_FILTER_ACADEMY_WIDE ? !a.teamId : a.teamId === Number(teamFilter))
      return matchesTerm && matchesTeam
    })
  }, [announcements, search, teamFilter])

  const { sorted, sortKey, sortDirection, requestSort } = useSort(filteredAnnouncements, 'createdAt', 'desc')
  const { pageItems, page, setPage, totalPages } = usePagination(sorted, 10)

  function resetToFirstPage() {
    setPage(1)
  }

  async function handleDelete() {
    setDeleting(true)
    try {
      await deleteAnnouncement(deleteTarget.id)
      setAnnouncements((prev) => prev.filter((a) => a.id !== deleteTarget.id))
      setDeleteTarget(null)
    } catch (err) {
      setError(err.message ?? 'Unable to delete announcement.')
    } finally {
      setDeleting(false)
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">Announcements</h1>
        <Link to="/announcements/new" className="btn btn-primary btn-sm">
          New announcement
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
          placeholder="Search title or content"
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
            <option value={TEAM_FILTER_ACADEMY_WIDE}>Academy-wide</option>
            {teams.map((team) => (
              <option key={team.id} value={team.id}>
                {team.name}
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
                    label="Title"
                    sortKey="title"
                    currentKey={sortKey}
                    direction={sortDirection}
                    onSort={requestSort}
                    className="w-[20%]"
                  />
                  <th className="w-[14%]">Team</th>
                  <th className="hidden sm:table-cell w-[36%]">Content</th>
                  <SortableTh
                    label="Posted"
                    sortKey="createdAt"
                    currentKey={sortKey}
                    direction={sortDirection}
                    onSort={requestSort}
                    className="w-[14%]"
                  />
                  <th className="w-[16%]">Actions</th>
                </tr>
              </thead>
              <tbody>
                {pageItems.map((a) => (
                  <tr key={a.id}>
                    <td className="truncate" title={a.title}>
                      {a.title}
                    </td>
                    <td>
                      <span className="badge badge-ghost badge-sm">{a.teamName ?? 'Academy-wide'}</span>
                    </td>
                    <td className="hidden sm:table-cell truncate" title={a.content}>
                      {a.content}
                    </td>
                    <td>{new Date(a.createdAt).toLocaleDateString()}</td>
                    <td className="whitespace-nowrap">
                      <Link to={`/announcements/${a.id}`} className="btn btn-ghost btn-xs text-info">
                        Details
                      </Link>
                      <Link to={`/announcements/${a.id}/edit`} className="btn btn-ghost btn-xs">
                        Edit
                      </Link>
                      <button
                        type="button"
                        className="btn btn-ghost btn-xs text-error"
                        onClick={() => setDeleteTarget(a)}
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
                {pageItems.length === 0 && (
                  <tr>
                    <td colSpan={5} className="text-base-content/60 py-6 text-center">
                      No announcements found.
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
          title="Delete announcement"
          body={`Permanently delete "${deleteTarget.title}"? This cannot be undone.`}
          confirmLabel={deleting ? 'Deleting…' : 'Delete'}
          onConfirm={handleDelete}
          onCancel={() => setDeleteTarget(null)}
        />
      )}
    </div>
  )
}
