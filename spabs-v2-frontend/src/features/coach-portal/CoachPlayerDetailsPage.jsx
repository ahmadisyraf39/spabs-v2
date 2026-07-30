import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import Pagination from '../../components/ui/Pagination'
import { getAttendanceSummary } from '../../lib/api/attendances'
import { getPlayer } from '../../lib/api/players'
import { getPlayerTeamProgress } from '../../lib/api/playerProgress'
import { getAge } from '../../lib/ageUtils'
import { attendanceProgressColor } from '../../lib/attendanceUtils'
import { getGuardiansForPlayer } from '../../lib/playerRelations'
import { getTeamHistoryForPlayer } from '../../lib/teamRelations'
import { usePagination } from '../../lib/usePagination'
import { moduleProgressColor } from '../progress/progressEnums'

export default function CoachPlayerDetailsPage() {
  const { teamId, playerId } = useParams()
  const [player, setPlayer] = useState(null)
  const [guardians, setGuardians] = useState([])
  const [teams, setTeams] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  useEffect(() => {
    let cancelled = false

    async function load() {
      setLoading(true)
      setError(null)
      try {
        const [p, guardianList, teamHistory] = await Promise.all([
          getPlayer(playerId),
          getGuardiansForPlayer(playerId),
          getTeamHistoryForPlayer(playerId),
        ])
        if (cancelled) return
        setPlayer(p)
        setGuardians(guardianList)

        const [summaries, progressSummaries] = await Promise.all([
          Promise.all(teamHistory.map((team) => getAttendanceSummary(playerId, team.id))),
          Promise.all(teamHistory.map((team) => getPlayerTeamProgress(playerId, team.id))),
        ])
        if (cancelled) return
        setTeams(
          teamHistory.map((team, i) => ({
            ...team,
            attendancePercentage: summaries[i].attendancePercentage,
            attendanceRecorded: summaries[i].totalRecords > 0,
            skillPercentage: progressSummaries[i].overallPercentage,
            skillRecorded: progressSummaries[i].totalModules > 0,
          })),
        )
      } catch (err) {
        if (!cancelled) setError(err.message ?? 'Unable to load player.')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    load()
    return () => {
      cancelled = true
    }
  }, [playerId])

  const { pageItems, page, setPage, totalPages } = usePagination(teams, 10)

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
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">{player.fullName}</h1>
        <div className="flex gap-2">
          <Link to={`/dashboard/coach/teams/${teamId}/players/${playerId}/progress`} className="btn btn-primary btn-sm">
            Record progress
          </Link>
          <Link to={`/dashboard/coach/teams/${teamId}`} className="btn btn-ghost btn-sm">
            Back to team
          </Link>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-3">
        <div className="card bg-base-100 shadow-md">
          <div className="card-body">
            <h2 className="card-title text-lg">Player info</h2>
            <dl className="grid grid-cols-[auto_1fr] gap-x-4 gap-y-3 text-sm">
              <dt className="text-base-content/60">Full name</dt>
              <dd>{player.fullName}</dd>

              <dt className="text-base-content/60">Date of birth</dt>
              <dd>{player.dateOfBirth}</dd>

              <dt className="text-base-content/60">Age</dt>
              <dd>{getAge(player.dateOfBirth)}</dd>

              <dt className="text-base-content/60">Gender</dt>
              <dd>
                <span className="badge badge-ghost">{player.gender}</span>
              </dd>
            </dl>
          </div>
        </div>

        <div className="card bg-base-100 shadow-md lg:col-span-2">
          <div className="card-body">
            <h2 className="card-title text-lg">Guardians</h2>
            {guardians.length === 0 ? (
              <p className="text-base-content/60 text-sm">No guardians linked.</p>
            ) : (
              <ul className="flex flex-col divide-y">
                {guardians.map((g) => (
                  <li key={g.linkId} className="flex flex-col gap-1 py-3 first:pt-0 last:pb-0">
                    <div className="flex items-center gap-2">
                      <span className="font-medium">{g.fullName}</span>
                      <span className="badge badge-ghost badge-sm">{g.relationship}</span>
                    </div>
                    <span className="text-base-content/70 text-sm">
                      Emergency contact: {g.emergencyContact || 'Not on file'}
                    </span>
                    {g.phoneNumber && (
                      <span className="text-base-content/70 text-sm">Phone: {g.phoneNumber}</span>
                    )}
                    <span className="text-base-content/70 text-sm">{g.email}</span>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>
      </div>

      <div className="card bg-base-100 shadow-md">
        <div className="card-body">
          <h2 className="card-title text-lg">Teams</h2>
          {teams.length === 0 ? (
            <p className="text-base-content/60 text-sm">No teams linked.</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="table">
                <thead>
                  <tr>
                    <th>Team</th>
                    <th className="hidden sm:table-cell">Category</th>
                    <th className="hidden sm:table-cell">Age group</th>
                    <th className="hidden sm:table-cell">Jersey #</th>
                    <th>Status</th>
                    <th className="hidden sm:table-cell">Joined</th>
                    <th className="hidden sm:table-cell">Left</th>
                    <th>Attendance</th>
                    <th>Skills</th>
                  </tr>
                </thead>
                <tbody>
                  {pageItems.map((team) => (
                    <tr key={team.linkId}>
                      <td>
                        <Link to={`/dashboard/coach/teams/${team.id}`} className="link link-primary text-sm">
                          {team.name}
                        </Link>
                      </td>
                      <td className="hidden sm:table-cell">
                        <span className="badge badge-ghost badge-sm">{team.category}</span>
                      </td>
                      <td className="hidden sm:table-cell">
                        <span className="badge badge-ghost badge-sm">{team.ageGroup}</span>
                      </td>
                      <td className="hidden sm:table-cell">
                        {team.jerseyNumber != null ? (
                          <span className="badge badge-ghost badge-sm">#{team.jerseyNumber}</span>
                        ) : (
                          '—'
                        )}
                      </td>
                      <td>
                        <span
                          className={`badge badge-soft badge-sm ${team.status === 'ACTIVE' ? 'badge-success' : 'badge-neutral'}`}
                        >
                          {team.status}
                        </span>
                      </td>
                      <td className="hidden sm:table-cell">{team.joinedAt}</td>
                      <td className="hidden sm:table-cell">{team.leftAt ?? '—'}</td>
                      <td>
                        {team.attendanceRecorded ? (
                          <div className="flex items-center gap-2">
                            <progress
                              className={`progress ${attendanceProgressColor(team.attendancePercentage)} w-20`}
                              value={team.attendancePercentage}
                              max="100"
                            />
                            <span className="text-xs whitespace-nowrap">{team.attendancePercentage}%</span>
                          </div>
                        ) : (
                          <span className="text-base-content/60 text-xs">No data</span>
                        )}
                      </td>
                      <td>
                        {team.skillRecorded ? (
                          <div className="flex items-center gap-2">
                            <progress
                              className={`progress ${moduleProgressColor(team.skillPercentage)} w-20`}
                              value={team.skillPercentage}
                              max="100"
                            />
                            <span className="text-xs whitespace-nowrap">{team.skillPercentage}%</span>
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
