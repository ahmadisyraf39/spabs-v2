import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import Pagination from '../../components/ui/Pagination'
import { getAttendanceSummary } from '../../lib/api/attendances'
import { getFeeRecordsByPlayer } from '../../lib/api/feeRecords'
import { getPlayer } from '../../lib/api/players'
import { getPlayerTeamProgress } from '../../lib/api/playerProgress'
import { getAge } from '../../lib/ageUtils'
import { attendanceProgressColor } from '../../lib/attendanceUtils'
import { getParentLinksForPlayer, getParentOptions } from '../../lib/playerRelations'
import { moduleProgressColor } from '../progress/progressEnums'
import { getTeamHistoryForPlayer } from '../../lib/teamRelations'
import { usePagination } from '../../lib/usePagination'

export default function PlayerDetailsPage() {
  const { id } = useParams()
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
        const [p, links, parentOptions, teamHistory] = await Promise.all([
          getPlayer(id),
          getParentLinksForPlayer(id),
          getParentOptions(),
          getTeamHistoryForPlayer(id),
        ])
        if (cancelled) return
        setPlayer(p)
        setGuardians(
          links.map((link) => {
            const option = parentOptions.find((o) => o.parentId === link.parentId)
            return { linkId: link.id, relationship: link.relationship, ...option }
          }),
        )

        const [summaries, progressSummaries, feeRecords] = await Promise.all([
          Promise.all(teamHistory.map((team) => getAttendanceSummary(id, team.id))),
          Promise.all(teamHistory.map((team) => getPlayerTeamProgress(id, team.id))),
          getFeeRecordsByPlayer(id),
        ])
        if (cancelled) return
        setTeams(
          teamHistory.map((team, i) => {
            const teamFeeRecords = feeRecords.filter((r) => r.teamId === team.id)
            const unpaidCount = teamFeeRecords.filter((r) => r.status === 'UNPAID').length
            const overdueCount = teamFeeRecords.filter((r) => r.overdue).length
            return {
              ...team,
              attendancePercentage: summaries[i].attendancePercentage,
              attendanceRecorded: summaries[i].totalRecords > 0,
              skillPercentage: progressSummaries[i].overallPercentage,
              skillRecorded: progressSummaries[i].totalModules > 0,
              feeRecordCount: teamFeeRecords.length,
              feeUnpaidCount: unpaidCount,
              feeOverdueCount: overdueCount,
            }
          }),
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

  return <PlayerDetailsBody id={id} player={player} guardians={guardians} teams={teams} />
}

function PlayerDetailsBody({ id, player, guardians, teams }) {
  const { pageItems, page, setPage, totalPages } = usePagination(teams, 10)

  return (
    <div className="flex flex-col gap-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-semibold">{player.fullName}</h1>
        <div className="flex gap-2">
          <Link to="/players" className="btn btn-ghost btn-sm">
            Back
          </Link>
          <Link to={`/players/${id}/attendance`} className="btn btn-ghost btn-sm">
            Attendance history
          </Link>
          <Link to={`/players/${id}/edit`} className="btn btn-primary btn-sm">
            Edit
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
            <h2 className="card-title text-lg">Parents / Guardians</h2>
            {guardians.length === 0 ? (
              <p className="text-base-content/60 text-sm">No guardians linked.</p>
            ) : (
              <div className="overflow-x-auto">
                <table className="table">
                  <thead>
                    <tr>
                      <th>Guardian</th>
                      <th>Relationship</th>
                    </tr>
                  </thead>
                  <tbody>
                    {guardians.map((guardian) => (
                      <tr key={guardian.linkId}>
                        <td>
                          {guardian.userId ? (
                            <Link to={`/users/${guardian.userId}`} className="link link-primary text-sm">
                              {guardian.label}
                            </Link>
                          ) : (
                            <span className="text-sm">{guardian.label ?? `Parent #${guardian.parentId}`}</span>
                          )}
                        </td>
                        <td>
                          <span className="badge badge-ghost badge-sm">{guardian.relationship}</span>
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
                    <th>Fees</th>
                  </tr>
                </thead>
                <tbody>
                  {pageItems.map((team) => (
                    <tr key={team.linkId}>
                      <td>
                        <Link to={`/teams/${team.id}`} className="link link-primary text-sm">
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
                        <span className={`badge badge-soft badge-sm ${team.status === 'ACTIVE' ? 'badge-success' : 'badge-neutral'}`}>
                          {team.status}
                        </span>
                      </td>
                      <td className="hidden sm:table-cell">{team.joinedAt}</td>
                      <td className="hidden sm:table-cell">{team.leftAt ?? '—'}</td>
                      <td>
                        {team.attendanceRecorded ? (
                          <div className="flex items-center gap-3">
                            <div>
                              <progress
                                className={`progress ${attendanceProgressColor(team.attendancePercentage)} w-24`}
                                value={team.attendancePercentage}
                                max="100"
                              />
                              <div className="text-xs whitespace-nowrap">{team.attendancePercentage}%</div>
                            </div>
                            <Link
                              to={`/players/${id}/attendance?teamId=${team.id}`}
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
                        {team.skillRecorded ? (
                          <div className="flex items-center gap-3">
                            <div>
                              <progress
                                className={`progress ${moduleProgressColor(team.skillPercentage)} w-24`}
                                value={team.skillPercentage}
                                max="100"
                              />
                              <div className="text-xs whitespace-nowrap">{team.skillPercentage}%</div>
                            </div>
                            <Link
                              to={`/players/${id}/progress?teamId=${team.id}`}
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
                        {team.feeRecordCount === 0 ? (
                          <span className="text-base-content/60 text-xs">No fees</span>
                        ) : (
                          <div className="flex items-center gap-2">
                            <span
                              className={`badge badge-soft badge-sm ${
                                team.feeUnpaidCount === 0
                                  ? 'badge-success'
                                  : team.feeOverdueCount > 0
                                    ? 'badge-error'
                                    : 'badge-warning'
                              }`}
                            >
                              {team.feeUnpaidCount === 0 ? 'All paid' : `${team.feeUnpaidCount} unpaid`}
                            </span>
                            <Link
                              to={`/fee-records?teamId=${team.id}&search=${encodeURIComponent(player.fullName)}`}
                              className="btn btn-ghost btn-xs"
                            >
                              View
                            </Link>
                          </div>
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
