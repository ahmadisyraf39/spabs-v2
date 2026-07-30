import { useEffect, useMemo, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import Pagination from '../../components/ui/Pagination'
import { getAttendanceSummary } from '../../lib/api/attendances'
import { getFeeRecordsByPlayer } from '../../lib/api/feeRecords'
import { getPlayer } from '../../lib/api/players'
import { getPlayerTeamProgress } from '../../lib/api/playerProgress'
import { getAge } from '../../lib/ageUtils'
import { attendanceProgressColor } from '../../lib/attendanceUtils'
import { getGuardiansForPlayer } from '../../lib/playerRelations'
import { getCoachingStaffForTeam, getTeamHistoryForPlayer } from '../../lib/teamRelations'
import { usePagination } from '../../lib/usePagination'
import { moduleProgressColor } from '../progress/progressEnums'

export default function ParentChildDetailsPage() {
  const { id } = useParams()
  const [player, setPlayer] = useState(null)
  const [guardians, setGuardians] = useState([])
  const [teams, setTeams] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [statusFilter, setStatusFilter] = useState('ACTIVE')

  useEffect(() => {
    let cancelled = false

    async function load() {
      setLoading(true)
      setError(null)
      try {
        const [p, guardianList, history] = await Promise.all([
          getPlayer(id),
          getGuardiansForPlayer(id),
          getTeamHistoryForPlayer(id),
        ])
        if (cancelled) return
        const [staffLists, summaries, progressSummaries, feeRecords] = await Promise.all([
          Promise.all(history.map((t) => getCoachingStaffForTeam(t.id))),
          Promise.all(history.map((t) => getAttendanceSummary(id, t.id))),
          Promise.all(history.map((t) => getPlayerTeamProgress(id, t.id))),
          getFeeRecordsByPlayer(id),
        ])
        if (cancelled) return
        setPlayer(p)
        setGuardians(guardianList)
        setTeams(
          history.map((t, i) => {
            const teamFeeRecords = feeRecords.filter((r) => r.teamId === t.id)
            const unpaidCount = teamFeeRecords.filter((r) => r.status === 'UNPAID').length
            const overdueCount = teamFeeRecords.filter((r) => r.overdue).length
            return {
              ...t,
              coaches: staffLists[i].filter((c) => c.status === 'ACTIVE'),
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
        if (!cancelled) setError(err.message ?? 'Unable to load this child.')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    load()
    return () => {
      cancelled = true
    }
  }, [id])

  const filteredTeams = useMemo(
    () => teams.filter((t) => !statusFilter || t.status === statusFilter),
    [teams, statusFilter],
  )

  const { pageItems, page, setPage, totalPages } = usePagination(filteredTeams, 10)

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
          <Link to={`/dashboard/parent/children/${id}/attendance`} className="btn btn-ghost btn-sm">
            Attendance
          </Link>
          <Link to={`/dashboard/parent/children/${id}/progress`} className="btn btn-ghost btn-sm">
            Progress
          </Link>
          <Link to={`/dashboard/parent/children/${id}/fees`} className="btn btn-ghost btn-sm">
            Fees
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

      <div className="flex gap-3 sm:justify-end">
        <select
          className="select select-bordered select-sm flex-1 sm:flex-initial"
          value={statusFilter}
          onChange={(e) => {
            setStatusFilter(e.target.value)
            setPage(1)
          }}
        >
          <option value="">All statuses</option>
          <option value="ACTIVE">ACTIVE</option>
          <option value="INACTIVE">INACTIVE</option>
        </select>
      </div>

      <div className="card bg-base-100 shadow-md">
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
                <th className="hidden sm:table-cell">Coaches</th>
                <th>Attendance</th>
                <th>Skills</th>
                <th>Fees</th>
              </tr>
            </thead>
            <tbody>
              {pageItems.map((team) => (
                <tr key={team.linkId}>
                  <td className="truncate">{team.name}</td>
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
                  <td className="hidden sm:table-cell">
                    {team.coaches.length === 0 ? (
                      <span className="text-base-content/60 text-xs">None</span>
                    ) : (
                      <ul className="flex flex-col gap-1">
                        {team.coaches.map((coach) => (
                          <li key={coach.id} className="text-xs whitespace-nowrap">
                            {coach.label ?? `Coach #${coach.coachId}`}
                          </li>
                        ))}
                      </ul>
                    )}
                  </td>
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
                        <Link to={`/dashboard/parent/children/${id}/attendance`} className="btn btn-ghost btn-xs">
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
                        <Link to={`/dashboard/parent/children/${id}/progress`} className="btn btn-ghost btn-xs">
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
                        <Link to={`/dashboard/parent/children/${id}/fees`} className="btn btn-ghost btn-xs">
                          View
                        </Link>
                      </div>
                    )}
                  </td>
                </tr>
              ))}
              {pageItems.length === 0 && (
                <tr>
                  <td colSpan={11} className="text-base-content/60 py-6 text-center">
                    No teams found.
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
