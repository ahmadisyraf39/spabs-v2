import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import Pagination from '../../components/ui/Pagination'
import SortableTh from '../../components/ui/SortableTh'
import { getActivities } from '../../lib/api/activities'
import { getMyParentProfile } from '../../lib/api/profiles'
import { getTeams } from '../../lib/api/teams'
import { getAttendanceHistoryForPlayer } from '../../lib/activityRelations'
import { formatDateTime, getActivityTimeframe } from '../../lib/dateTimeUtils'
import { getPlayersForParent } from '../../lib/playerRelations'
import { getTeamHistoryForPlayer } from '../../lib/teamRelations'
import { usePagination } from '../../lib/usePagination'
import { useSort } from '../../lib/useSort'
import { attendanceStatusBadgeClass } from '../activities/activityEnums'

export default function ParentActivitiesListPage() {
  const [children, setChildren] = useState([])
  const [teams, setTeams] = useState([])
  const [activities, setActivities] = useState([])
  const [childTeamIds, setChildTeamIds] = useState(new Map())
  const [attendanceByChildId, setAttendanceByChildId] = useState(new Map())
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [childFilter, setChildFilter] = useState('')
  const [teamFilter, setTeamFilter] = useState('')
  const [timeframeFilter, setTimeframeFilter] = useState('upcoming')

  useEffect(() => {
    let cancelled = false

    async function load() {
      setLoading(true)
      setError(null)
      try {
        const parent = await getMyParentProfile()
        const [kids, allTeams, allActivities] = await Promise.all([
          getPlayersForParent(parent.id),
          getTeams(),
          getActivities(),
        ])
        if (cancelled) return
        const [teamHistories, attendanceHistories] = await Promise.all([
          Promise.all(kids.map((child) => getTeamHistoryForPlayer(child.id))),
          Promise.all(kids.map((child) => getAttendanceHistoryForPlayer(child.id))),
        ])
        if (cancelled) return

        const byChild = new Map(
          kids.map((child, i) => [
            child.id,
            new Set(teamHistories[i].filter((t) => t.status === 'ACTIVE').map((t) => t.id)),
          ]),
        )
        const myTeamIds = new Set([...byChild.values()].flatMap((set) => [...set]))
        const attendanceMap = new Map(
          kids.map((child, i) => [
            child.id,
            new Map(attendanceHistories[i].map((record) => [record.activityId, record.status])),
          ]),
        )

        setChildren(kids)
        setChildTeamIds(byChild)
        setAttendanceByChildId(attendanceMap)
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

  const childrenByTeamId = useMemo(() => {
    const map = new Map()
    for (const child of children) {
      for (const teamId of childTeamIds.get(child.id) ?? []) {
        if (!map.has(teamId)) map.set(teamId, [])
        map.get(teamId).push(child)
      }
    }
    return map
  }, [children, childTeamIds])

  const activitiesWithDisplay = useMemo(
    () =>
      activities.map((a) => {
        const matchedChildren = childrenByTeamId.get(a.teamId) ?? []
        return {
          ...a,
          displayTitle: a.title?.trim() ? a.title : a.type,
          teamName: teamsById.get(a.teamId)?.name ?? `Team #${a.teamId}`,
          timeframe: getActivityTimeframe(a.startAt),
          attendanceEntries: matchedChildren.map((child) => ({
            name: child.fullName,
            status: attendanceByChildId.get(child.id)?.get(a.id) ?? null,
          })),
        }
      }),
    [activities, teamsById, childrenByTeamId, attendanceByChildId],
  )

  const filteredActivities = useMemo(() => {
    return activitiesWithDisplay.filter((a) => {
      const matchesChild = !childFilter || childTeamIds.get(Number(childFilter))?.has(a.teamId)
      const matchesTeam = !teamFilter || a.teamId === Number(teamFilter)
      const matchesTimeframe =
        !timeframeFilter ||
        (timeframeFilter === 'upcoming' ? a.timeframe !== 'past' : a.timeframe === 'past')
      return matchesChild && matchesTeam && matchesTimeframe
    })
  }, [activitiesWithDisplay, childFilter, teamFilter, timeframeFilter, childTeamIds])

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
                <th className="hidden sm:table-cell w-[12%]">Type</th>
                <th className="w-[16%]">Team</th>
                <th className="w-[14%]">Player</th>
                <SortableTh
                  label="Start"
                  sortKey="startAt"
                  currentKey={sortKey}
                  direction={sortDirection}
                  onSort={requestSort}
                  className="w-[14%]"
                />
                <th className="w-[14%]">Attendance</th>
                <th className="w-[10%]">Actions</th>
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
                  <td className="truncate" title={activity.attendanceEntries.map((e) => e.name).join(', ')}>
                    {activity.attendanceEntries.map((e) => e.name).join(', ') || '—'}
                  </td>
                  <td>{formatDateTime(activity.startAt)}</td>
                  <td>
                    {activity.attendanceEntries.length === 0 ? (
                      '—'
                    ) : (
                      <div className="flex flex-col gap-1">
                        {activity.attendanceEntries.map((e) => (
                          <span key={e.name} className="flex items-center gap-1">
                            {activity.attendanceEntries.length > 1 && (
                              <span className="text-base-content/60 text-xs">{e.name}:</span>
                            )}
                            {e.status ? (
                              <span className={`badge badge-sm ${attendanceStatusBadgeClass(e.status)}`}>
                                {e.status}
                              </span>
                            ) : (
                              <span className="text-base-content/60 text-xs">Not recorded</span>
                            )}
                          </span>
                        ))}
                      </div>
                    )}
                  </td>
                  <td>
                    <Link
                      to={`/dashboard/parent/activities/${activity.id}`}
                      className="btn btn-ghost btn-xs text-info"
                    >
                      View
                    </Link>
                  </td>
                </tr>
              ))}
              {pageItems.length === 0 && (
                <tr>
                  <td colSpan={7} className="text-base-content/60 py-6 text-center">
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
