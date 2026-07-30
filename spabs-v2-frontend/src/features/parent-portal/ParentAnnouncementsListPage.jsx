import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import Pagination from '../../components/ui/Pagination'
import SortableTh from '../../components/ui/SortableTh'
import { getMyAnnouncements } from '../../lib/api/announcements'
import { getTeams } from '../../lib/api/teams'
import { usePagination } from '../../lib/usePagination'
import { useSort } from '../../lib/useSort'

export default function ParentAnnouncementsListPage() {
  const [announcements, setAnnouncements] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [search, setSearch] = useState('')
  const [teamFilter, setTeamFilter] = useState('')

  useEffect(() => {
    let cancelled = false

    async function load() {
      setLoading(true)
      setError(null)
      try {
        const [announcementList, teams] = await Promise.all([getMyAnnouncements(), getTeams()])
        if (cancelled) return
        const teamsById = new Map(teams.map((t) => [t.id, t]))
        setAnnouncements(
          announcementList.map((a) => ({
            ...a,
            teamName: a.teamId ? (teamsById.get(a.teamId)?.name ?? `Team #${a.teamId}`) : null,
          })),
        )
      } catch (err) {
        if (!cancelled) setError(err.message ?? 'Unable to load announcements.')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    load()
    return () => {
      cancelled = true
    }
  }, [])

  const teamOptions = useMemo(() => {
    const byId = new Map()
    for (const a of announcements) {
      if (a.teamId != null && !byId.has(a.teamId)) byId.set(a.teamId, a.teamName)
    }
    return Array.from(byId, ([teamId, teamName]) => ({ teamId, teamName })).sort((a, b) =>
      a.teamName.localeCompare(b.teamName),
    )
  }, [announcements])

  const filteredAnnouncements = useMemo(() => {
    const term = search.trim().toLowerCase()
    return announcements.filter((a) => {
      const matchesTerm =
        !term || a.title.toLowerCase().includes(term) || a.content.toLowerCase().includes(term)
      const matchesTeam = !teamFilter || String(a.teamId) === teamFilter
      return matchesTerm && matchesTeam
    })
  }, [announcements, search, teamFilter])

  const { sorted, sortKey, sortDirection, requestSort } = useSort(filteredAnnouncements, 'createdAt', 'desc')
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
      <h1 className="text-2xl font-semibold">Announcements</h1>

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
            {teamOptions.map((option) => (
              <option key={option.teamId} value={option.teamId}>
                {option.teamName}
              </option>
            ))}
          </select>
        </div>
      </div>

      <div className="card bg-base-100 shadow-md">
        <div className="overflow-x-auto">
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
                    <Link
                      to={`/dashboard/parent/announcements/${a.id}`}
                      className="btn btn-ghost btn-xs text-info"
                    >
                      Details
                    </Link>
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
        </div>
        <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
      </div>
    </div>
  )
}
