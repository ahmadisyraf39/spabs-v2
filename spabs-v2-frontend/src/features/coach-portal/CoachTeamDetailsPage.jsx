import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import Pagination from '../../components/ui/Pagination'
import { getAttendanceSummary } from '../../lib/api/attendances'
import { getPlayerTeamProgress } from '../../lib/api/playerProgress'
import { getTeam } from '../../lib/api/teams'
import { attendanceProgressColor } from '../../lib/attendanceUtils'
import { moduleProgressColor } from '../progress/progressEnums'
import { getCoachingStaffForTeam, getRosterForTeam } from '../../lib/teamRelations'
import { usePagination } from '../../lib/usePagination'

export default function CoachTeamDetailsPage() {
  const { id } = useParams()
  const [team, setTeam] = useState(null)
  const [roster, setRoster] = useState([])
  const [staff, setStaff] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    let cancelled = false

    async function load() {
      setLoading(true)
      setError(null)
      try {
        const [t, rosterList, staffList] = await Promise.all([
          getTeam(id),
          getRosterForTeam(id),
          getCoachingStaffForTeam(id),
        ])
        if (cancelled) return
        setTeam(t)
        setStaff(staffList)

        const activeLinks = rosterList.filter((r) => r.status === 'ACTIVE')
        const [summaries, progressSummaries] = await Promise.all([
          Promise.all(activeLinks.map((link) => getAttendanceSummary(link.playerId, id))),
          Promise.all(activeLinks.map((link) => getPlayerTeamProgress(link.playerId, id))),
        ])
        if (cancelled) return
        const summaryByPlayerId = new Map(activeLinks.map((link, i) => [link.playerId, summaries[i]]))
        const progressByPlayerId = new Map(
          activeLinks.map((link, i) => [link.playerId, progressSummaries[i]]),
        )
        setRoster(
          activeLinks
            .map((link) => {
              const summary = summaryByPlayerId.get(link.playerId)
              const progress = progressByPlayerId.get(link.playerId)
              return {
                ...link,
                attendancePercentage: summary?.attendancePercentage,
                attendanceRecorded: (summary?.totalRecords ?? 0) > 0,
                skillPercentage: progress?.overallPercentage,
                skillRecorded: (progress?.totalModules ?? 0) > 0,
              }
            })
            .sort((a, b) => a.fullName.localeCompare(b.fullName)),
        )
      } catch (err) {
        if (!cancelled) setError(err.message ?? 'Unable to load team.')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    load()
    return () => {
      cancelled = true
    }
  }, [id])

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

  const activeStaff = staff.filter((s) => s.status === 'ACTIVE')

  return (
    <CoachTeamDetailsBody id={id} team={team} roster={roster} activeStaff={activeStaff} />
  )
}

function CoachTeamDetailsBody({ id, team, roster, activeStaff }) {
  const { pageItems, page, setPage, totalPages } = usePagination(roster, 10)

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">{team.name}</h1>
        <Link to="/dashboard/coach/teams" className="btn btn-ghost btn-sm">
          Back to my teams
        </Link>
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        <div className="card bg-base-100 shadow-md">
          <div className="card-body">
            <h2 className="card-title text-lg">Team info</h2>
            <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-3 text-sm">
              <dt className="text-base-content/60">Name</dt>
              <dd>{team.name}</dd>

              <dt className="text-base-content/60">Category</dt>
              <dd>
                <span className="badge badge-ghost">{team.category}</span>
              </dd>

              <dt className="text-base-content/60">Age group</dt>
              <dd>
                <span className="badge badge-ghost">{team.ageGroup}</span>
              </dd>

              <dt className="text-base-content/60">Players</dt>
              <dd>{roster.length}</dd>
            </dl>
          </div>
        </div>

        <div className="card bg-base-100 shadow-md lg:col-span-2">
          <div className="card-body">
            <h2 className="card-title text-lg">Coaching staff</h2>
            {activeStaff.length === 0 ? (
              <p className="text-base-content/60 text-sm">No coaches assigned.</p>
            ) : (
              <div className="overflow-x-auto">
                <table className="table">
                  <thead>
                    <tr>
                      <th>Coach</th>
                      <th>Role</th>
                    </tr>
                  </thead>
                  <tbody>
                    {activeStaff.map((coach) => (
                      <tr key={coach.id}>
                        <td>{coach.label ?? `Coach #${coach.coachId}`}</td>
                        <td>
                          <span className="badge badge-ghost badge-sm">{coach.role}</span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </div>
      </div>

      <div className="card bg-base-100 shadow-md">
        <div className="card-body">
          <h2 className="card-title text-lg">Roster</h2>
          {roster.length === 0 ? (
            <p className="text-base-content/60 text-sm">No active players on this team.</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="table table-fixed">
                <thead>
                  <tr>
                    <th className="w-[22%]">Player</th>
                    <th className="w-[13%]">Jersey #</th>
                    <th className="w-[27%]">Attendance</th>
                    <th className="w-[27%]">Skills</th>
                    <th className="w-[11%]">Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {pageItems.map((player) => (
                    <tr key={player.playerId}>
                      <td className="truncate">{player.fullName}</td>
                      <td>
                        {player.jerseyNumber != null ? (
                          <span className="badge badge-ghost badge-sm">#{player.jerseyNumber}</span>
                        ) : (
                          '—'
                        )}
                      </td>
                      <td>
                        {player.attendanceRecorded ? (
                          <div className="flex items-center gap-2">
                            <progress
                              className={`progress ${attendanceProgressColor(player.attendancePercentage)} w-20`}
                              value={player.attendancePercentage}
                              max="100"
                            />
                            <span className="text-xs whitespace-nowrap">{player.attendancePercentage}%</span>
                          </div>
                        ) : (
                          <span className="text-base-content/60 text-xs">No data</span>
                        )}
                      </td>
                      <td>
                        {player.skillRecorded ? (
                          <div className="flex items-center gap-2">
                            <progress
                              className={`progress ${moduleProgressColor(player.skillPercentage)} w-20`}
                              value={player.skillPercentage}
                              max="100"
                            />
                            <span className="text-xs whitespace-nowrap">{player.skillPercentage}%</span>
                          </div>
                        ) : (
                          <span className="text-base-content/60 text-xs">No data</span>
                        )}
                      </td>
                      <td className="whitespace-nowrap">
                        <Link
                          to={`/dashboard/coach/teams/${id}/players/${player.playerId}`}
                          className="btn btn-ghost btn-xs text-info"
                        >
                          Details
                        </Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
        <Pagination page={page} totalPages={totalPages} onPageChange={setPage} />
      </div>
    </div>
  )
}
