import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import ConfirmModal from '../../components/ui/ConfirmModal'
import Pagination from '../../components/ui/Pagination'
import SortableTh from '../../components/ui/SortableTh'
import { deleteActivity, getActivities } from '../../lib/api/activities'
import { getTeams } from '../../lib/api/teams'
import { deleteAllAttendanceForActivity } from '../../lib/activityRelations'
import { formatDateTime, getActivityTimeframe } from '../../lib/dateTimeUtils'
import { usePagination } from '../../lib/usePagination'
import { useSort } from '../../lib/useSort'
import { ACTIVITY_TYPES } from './activityEnums'

export default function ActivitiesListPage() {
  const [activities, setActivities] = useState([])
  const [teams, setTeams] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [search, setSearch] = useState('')
  const [teamFilter, setTeamFilter] = useState('')
  const [typeFilter, setTypeFilter] = useState('')
  const [timeframeFilter, setTimeframeFilter] = useState('upcoming')
  const [deleteTarget, setDeleteTarget] = useState(null)
  const [deleting, setDeleting] = useState(false)

  async function loadActivities() {
    setLoading(true)
    setError(null)
    try {
      const [activityList, teamList] = await Promise.all([getActivities(), getTeams()])
      setActivities(activityList)
      setTeams(teamList)
    } catch (err) {
      setError(err.message ?? 'Unable to load activities.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadActivities()
  }, [])

  const teamsById = useMemo(() => new Map(teams.map((t) => [t.id, t])), [teams])

  const activitiesWithDisplay = useMemo(
    () =>
      activities.map((a) => ({
        ...a,
        displayTitle: a.title?.trim() ? a.title : a.type,
        teamName: teamsById.get(a.teamId)?.name ?? `Team #${a.teamId}`,
        timeframe: getActivityTimeframe(a.startAt),
      })),
    [activities, teamsById],
  )

  const filteredActivities = useMemo(() => {
    const term = search.trim().toLowerCase()
    return activitiesWithDisplay.filter((a) => {
      const matchesTerm = !term || a.displayTitle.toLowerCase().includes(term)
      const matchesTeam = !teamFilter || a.teamId === Number(teamFilter)
      const matchesType = !typeFilter || a.type === typeFilter
      const matchesTimeframe =
        !timeframeFilter ||
        (timeframeFilter === 'upcoming' ? a.timeframe !== 'past' : a.timeframe === 'past')
      return matchesTerm && matchesTeam && matchesType && matchesTimeframe
    })
  }, [activitiesWithDisplay, search, teamFilter, typeFilter, timeframeFilter])

  const { sorted, sortKey, sortDirection, requestSort } = useSort(filteredActivities, 'startAt', 'desc')
  const { pageItems, page, setPage, totalPages } = usePagination(sorted, 10)

  function resetToFirstPage() {
    setPage(1)
  }

  async function handleDelete() {
    setDeleting(true)
    try {
      await deleteAllAttendanceForActivity(deleteTarget.id)
      await deleteActivity(deleteTarget.id)
      setActivities((prev) => prev.filter((a) => a.id !== deleteTarget.id))
      setDeleteTarget(null)
    } catch (err) {
      setError(err.message ?? 'Unable to delete activity.')
    } finally {
      setDeleting(false)
    }
  }

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">Activities</h1>
        <div className="flex gap-2">
          <Link to="/activities/recurring" className="btn btn-outline btn-sm">
            Generate recurring training
          </Link>
          <Link to="/activities/new" className="btn btn-primary btn-sm">
            New activity
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
          placeholder="Search title"
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
            value={typeFilter}
            onChange={(e) => {
              setTypeFilter(e.target.value)
              resetToFirstPage()
            }}
          >
            <option value="">All types</option>
            {ACTIVITY_TYPES.map((value) => (
              <option key={value} value={value}>
                {value}
              </option>
            ))}
          </select>
          <select
            className="select select-bordered select-sm flex-1 sm:flex-initial"
            value={timeframeFilter}
            onChange={(e) => {
              setTimeframeFilter(e.target.value)
              resetToFirstPage()
            }}
          >
            <option value="upcoming">Upcoming</option>
            <option value="past">Past</option>
            <option value="">All activities</option>
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
                    sortKey="displayTitle"
                    currentKey={sortKey}
                    direction={sortDirection}
                    onSort={requestSort}
                    className="w-[26%]"
                  />
                  <SortableTh
                    label="Type"
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
                    className="w-[24%]"
                  />
                  <SortableTh
                    label="Start"
                    sortKey="startAt"
                    currentKey={sortKey}
                    direction={sortDirection}
                    onSort={requestSort}
                    className="w-[18%]"
                  />
                  <th className="w-[18%]">Actions</th>
                </tr>
              </thead>
              <tbody>
                {pageItems.map((activity) => (
                  <tr key={activity.id}>
                    <td className="truncate" title={activity.displayTitle}>
                      {activity.displayTitle}
                    </td>
                    <td className="hidden sm:table-cell">
                      <span className="badge badge-ghost">{activity.type}</span>
                    </td>
                    <td>
                      <Link to={`/teams/${activity.teamId}`} className="link link-primary text-sm">
                        {activity.teamName}
                      </Link>
                    </td>
                    <td>{formatDateTime(activity.startAt)}</td>
                    <td className="whitespace-nowrap">
                      <Link to={`/activities/${activity.id}`} className="btn btn-ghost btn-xs text-info">
                        Details
                      </Link>
                      {activity.timeframe !== 'past' && (
                        <Link to={`/activities/${activity.id}/edit`} className="btn btn-ghost btn-xs">
                          Edit
                        </Link>
                      )}
                      <button
                        type="button"
                        className="btn btn-ghost btn-xs text-error"
                        onClick={() => setDeleteTarget(activity)}
                      >
                        Delete
                      </button>
                    </td>
                  </tr>
                ))}
                {pageItems.length === 0 && (
                  <tr>
                    <td colSpan={5} className="text-base-content/60 py-6 text-center">
                      No activities found.
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
          title="Delete activity"
          body={`Permanently delete "${deleteTarget.title || deleteTarget.type}"? This also removes its attendance records. This cannot be undone.`}
          confirmLabel={deleting ? 'Deleting…' : 'Delete'}
          onConfirm={handleDelete}
          onCancel={() => setDeleteTarget(null)}
        />
      )}
    </div>
  )
}
