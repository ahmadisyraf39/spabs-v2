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

export default function TeamDetailsPage() {
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
          rosterList.map((link) => {
            const summary = summaryByPlayerId.get(link.playerId)
            const progress = progressByPlayerId.get(link.playerId)
            return {
              ...link,
              ...(summary && {
                attendancePercentage: summary.attendancePercentage,
                attendanceRecorded: summary.totalRecords > 0,
              }),
              ...(progress && {
                skillPercentage: progress.overallPercentage,
                skillRecorded: progress.totalModules > 0,
              }),
            }
          }),
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

  const activeRoster = [...roster]
    .filter((r) => r.status === 'ACTIVE')
    .sort((a, b) => a.fullName.localeCompare(b.fullName))
  const activeStaff = staff.filter((s) => s.status === 'ACTIVE')

  return <TeamDetailsBody id={id} team={team} activeRoster={activeRoster} activeStaff={activeStaff} />
}

function TeamDetailsBody({ id, team, activeRoster, activeStaff }) {
  const { pageItems, page, setPage, totalPages } = usePagination(activeRoster, 10)

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">{team.name}</h1>
        <div className="flex gap-2">
          <Link to="/teams" className="btn btn-ghost btn-sm">
            Back
          </Link>
          <Link to={`/teams/${id}/edit`} className="btn btn-primary btn-sm">
            Edit
          </Link>
        </div>
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
              <dd>{activeRoster.length}</dd>
            </dl>
          </div>
        </div>

        <div className="card bg-base-100 shadow-md lg:col-span-2">
          <div className="card-body">
            <h2 className="card-title text-lg">Coaching staff</h2>
            {activeStaff.length === 0 ? (
              <p className="text-base-content/60 text-sm">No coaches linked.</p>
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
                        <td>
                          {coach.userId ? (
                            <Link to={`/users/${coach.userId}`} className="link link-primary text-sm">
                              {coach.label}
                            </Link>
                          ) : (
                            <span className="text-sm">{coach.label ?? `Coach #${coach.coachId}`}</span>
                          )}
                        </td>
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
          {activeRoster.length === 0 ? (
            <p className="text-base-content/60 text-sm">No players linked.</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="table">
                <thead>
                  <tr>
                    <th>Player</th>
                    <th className="hidden sm:table-cell">Age</th>
                    <th className="hidden sm:table-cell">Gender</th>
                    <th className="hidden sm:table-cell">Jersey #</th>
                    <th>Attendance</th>
                    <th>Skills</th>
                  </tr>
                </thead>
                <tbody>
                  {pageItems.map((player) => (
                    <tr key={player.id}>
                      <td>
                        <Link to={`/players/${player.playerId}`} className="link link-primary text-sm">
                          {player.fullName}
                        </Link>
                      </td>
                      <td className="hidden sm:table-cell">{player.age}</td>
                      <td className="hidden sm:table-cell">{player.gender}</td>
                      <td className="hidden sm:table-cell">
                        {player.jerseyNumber != null ? (
                          <span className="badge badge-ghost badge-sm">#{player.jerseyNumber}</span>
                        ) : (
                          '—'
                        )}
                      </td>
                      <td>
                        {player.attendanceRecorded ? (
                          <div className="flex items-center gap-3">
                            <div>
                              <progress
                                className={`progress ${attendanceProgressColor(player.attendancePercentage)} w-24`}
                                value={player.attendancePercentage}
                                max="100"
                              />
                              <div className="text-xs whitespace-nowrap">{player.attendancePercentage}%</div>
                            </div>
                            <Link
                              to={`/players/${player.playerId}/attendance?teamId=${id}`}
                              className="btn btn-ghost btn-xs"
                            >
                              View
                            </Link>
                          </div>
                        ) : (
                          <span className="text-base-content/60 text-xs">No data</span>
                        )}
                      </td>
                      <td>
                        {player.skillRecorded ? (
                          <div className="flex items-center gap-3">
                            <div>
                              <progress
                                className={`progress ${moduleProgressColor(player.skillPercentage)} w-24`}
                                value={player.skillPercentage}
                                max="100"
                              />
                              <div className="text-xs whitespace-nowrap">{player.skillPercentage}%</div>
                            </div>
                            <Link
                              to={`/players/${player.playerId}/progress?teamId=${id}`}
                              className="btn btn-ghost btn-xs"
                            >
                              View
                            </Link>
                          </div>
                        ) : (
                          <span className="text-base-content/60 text-xs">No data</span>
                        )}
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
