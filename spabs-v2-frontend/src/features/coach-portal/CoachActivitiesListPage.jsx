import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import Pagination from '../../components/ui/Pagination'
import SortableTh from '../../components/ui/SortableTh'
import { getActivities } from '../../lib/api/activities'
import { getMyCoachProfile } from '../../lib/api/profiles'
import { getCoachTeamsByCoach } from '../../lib/api/coachTeams'
import { getTeams } from '../../lib/api/teams'
import { formatDateTime, getActivityTimeframe } from '../../lib/dateTimeUtils'
import { usePagination } from '../../lib/usePagination'
import { useSort } from '../../lib/useSort'
import { ACTIVITY_TYPES } from '../activities/activityEnums'

export default function CoachActivitiesListPage() {
  const [teams, setTeams] = useState([])
  const [activities, setActivities] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [teamFilter, setTeamFilter] = useState('')
  const [typeFilter, setTypeFilter] = useState('')
  const [timeframeFilter, setTimeframeFilter] = useState('upcoming')

  useEffect(() => {
    let cancelled = false

    async function load() {
      setLoading(true)
      setError(null)
      try {
        const coach = await getMyCoachProfile()
        const [links, allTeams, allActivities] = await Promise.all([
          getCoachTeamsByCoach(coach.id),
          getTeams(),
          getActivities(),
        ])
        if (cancelled) return
        const myTeamIds = new Set(links.filter((l) => l.status === 'ACTIVE').map((l) => l.teamId))
        setTeams(allTeams.filter((t) => myTeamIds.has(t.id)))
        setActivities(allActivities.filter((a) => myTeamIds.has(a.teamId)))
      } catch (err) {
        if (!cancelled) setError(err.message ?? 'Unable to load activities.')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    load()
    return () => {
      cancelled = true
    }
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
    return activitiesWithDisplay.filter((a) => {
      const matchesTeam = !teamFilter || a.teamId === Number(teamFilter)
      const matchesType = !typeFilter || a.type === typeFilter
      const matchesTimeframe =
        !timeframeFilter ||
        (timeframeFilter === 'upcoming' ? a.timeframe !== 'past' : a.timeframe === 'past')
      return matchesTeam && matchesType && matchesTimeframe
    })
  }, [activitiesWithDisplay, teamFilter, typeFilter, timeframeFilter])

  const { sorted, sortKey, sortDirection, requestSort } = useSort(filteredActivities, 'startAt', 'desc')
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
      <h1 className="text-2xl font-semibold">Activities</h1>

      {error && (
        <div role="alert" className="alert alert-error text-sm">
          <span>{error}</span>
        </div>
      )}

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

      <div className="card bg-base-100 shadow-md">
        <div className="overflow-x-auto">
          <table className="table table-fixed">
            <thead>
              <tr>
                <SortableTh
                  label="Title"
                  sortKey="displayTitle"
                  currentKey={sortKey}
                  direction={sortDirection}
                  onSort={requestSort}
                  className="w-[30%]"
                />
                <th className="hidden sm:table-cell w-[14%]">Type</th>
                <th className="w-[24%]">Team</th>
                <SortableTh
                  label="Start"
                  sortKey="startAt"
                  currentKey={sortKey}
                  direction={sortDirection}
                  onSort={requestSort}
                  className="w-[18%]"
                />
                <th className="w-[14%]">Actions</th>
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
                  <td className="truncate">{activity.teamName}</td>
                  <td>{formatDateTime(activity.startAt)}</td>
                  <td>
                    <Link to={`/dashboard/coach/activities/${activity.id}`} className="btn btn-ghost btn-xs text-info">
                      View
                    </Link>
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
        </div>
        <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
      </div>
    </div>
  )
}
