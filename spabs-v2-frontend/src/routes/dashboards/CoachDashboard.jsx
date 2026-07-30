import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { Bar, BarChart, CartesianGrid, Legend, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { getMyCoachProfile } from '../../lib/api/profiles'
import { getCoachPaymentsByCoach } from '../../lib/api/coachPayments'
import { getCoachTeamsByCoach } from '../../lib/api/coachTeams'
import { getAttendanceSummary } from '../../lib/api/attendances'
import { getPlayerTeamProgress } from '../../lib/api/playerProgress'
import { getMyUpcomingActivities } from '../../lib/api/activities'
import { getTeams } from '../../lib/api/teams'
import { getRosterForTeam } from '../../lib/teamRelations'
import MyAnnouncements from '../../features/dashboard/MyAnnouncements'
import MyUpcomingActivities from '../../features/dashboard/MyUpcomingActivities'

const COLOR_PRIMARY = '#148634'
const COLOR_INFO = '#3b82f6'

export default function CoachDashboard() {
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const [teamCount, setTeamCount] = useState(0)
  const [playerCount, setPlayerCount] = useState(0)
  const [upcomingCount, setUpcomingCount] = useState(0)
  const [unpaidCount, setUnpaidCount] = useState(0)
  const [teamChartData, setTeamChartData] = useState([])

  useEffect(() => {
    let cancelled = false

    async function load() {
      setLoading(true)
      setError(null)
      try {
        const coach = await getMyCoachProfile()
        const [links, allTeams, payments, upcoming] = await Promise.all([
          getCoachTeamsByCoach(coach.id),
          getTeams(),
          getCoachPaymentsByCoach(coach.id),
          getMyUpcomingActivities(),
        ])
        if (cancelled) return

        const teamsById = new Map(allTeams.map((t) => [t.id, t]))
        const activeLinks = links.filter((l) => l.status === 'ACTIVE' && teamsById.has(l.teamId))
        setTeamCount(activeLinks.length)
        setUnpaidCount(payments.filter((p) => p.status === 'UNPAID').length)

        const now = new Date()
        const in7Days = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000)
        setUpcomingCount(
          upcoming.filter((a) => {
            const start = new Date(a.startAt)
            return start >= now && start <= in7Days
          }).length,
        )

        const teamStats = await Promise.all(
          activeLinks.map(async (link) => {
            const team = teamsById.get(link.teamId)
            const roster = (await getRosterForTeam(link.teamId)).filter((r) => r.status === 'ACTIVE')
            const [summaries, progressSummaries] = await Promise.all([
              Promise.all(roster.map((r) => getAttendanceSummary(r.playerId, link.teamId))),
              Promise.all(roster.map((r) => getPlayerTeamProgress(r.playerId, link.teamId))),
            ])
            const recordedSummaries = summaries.filter((s) => s.totalRecords > 0)
            const recordedProgress = progressSummaries.filter((p) => p.totalModules > 0)
            const avgAttendance = recordedSummaries.length
              ? Math.round(
                  recordedSummaries.reduce((sum, s) => sum + s.attendancePercentage, 0) /
                    recordedSummaries.length,
                )
              : 0
            const avgProgress = recordedProgress.length
              ? Math.round(
                  recordedProgress.reduce((sum, p) => sum + p.overallPercentage, 0) / recordedProgress.length,
                )
              : 0
            return { name: team.name, players: roster.length, attendance: avgAttendance, progress: avgProgress }
          }),
        )
        if (cancelled) return
        setTeamChartData(teamStats)
        setPlayerCount(teamStats.reduce((sum, t) => sum + t.players, 0))
      } catch (err) {
        if (!cancelled) setError(err.message ?? 'Unable to load your dashboard.')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    load()
    return () => {
      cancelled = true
    }
  }, [])

  const playersChartData = useMemo(
    () => teamChartData.map((t) => ({ name: t.name, count: t.players })),
    [teamChartData],
  )

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
    <div className="flex flex-col gap-4">
      <div className="stats stats-vertical sm:stats-horizontal bg-base-100 w-full shadow-md">
        <div className="stat">
          <div className="stat-title">My teams</div>
          <div className="stat-value text-2xl">{teamCount}</div>
        </div>
        <div className="stat">
          <div className="stat-title">My players</div>
          <div className="stat-value text-2xl">{playerCount}</div>
        </div>
        <div className="stat">
          <div className="stat-title">Upcoming activities (7 days)</div>
          <div className="stat-value text-2xl">{upcomingCount}</div>
        </div>
        <div className="stat">
          <div className="stat-title">Unpaid payments</div>
          <div className="stat-value text-2xl">{unpaidCount}</div>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
        <div className="card bg-base-100 shadow-md lg:col-span-2">
          <div className="card-body">
            <h2 className="card-title text-lg">Attendance &amp; progress by team</h2>
            {teamChartData.length === 0 ? (
              <p className="text-base-content/60 text-sm">Not currently assigned to any team.</p>
            ) : (
              <div className="h-64 w-full">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={teamChartData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="name" tick={{ fontSize: 12 }} />
                    <YAxis tick={{ fontSize: 12 }} unit="%" domain={[0, 100]} />
                    <Tooltip formatter={(value) => `${value}%`} />
                    <Legend />
                    <Bar dataKey="attendance" name="Attendance %" fill={COLOR_PRIMARY} radius={[4, 4, 0, 0]} />
                    <Bar dataKey="progress" name="Progress %" fill={COLOR_INFO} radius={[4, 4, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            )}
          </div>
        </div>

        <div className="card bg-base-100 shadow-md">
          <div className="card-body">
            <h2 className="card-title text-lg">Players per team</h2>
            {playersChartData.length === 0 ? (
              <p className="text-base-content/60 text-sm">No teams yet.</p>
            ) : (
              <div className="h-64 w-full">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={playersChartData} layout="vertical" margin={{ left: 16 }}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis type="number" allowDecimals={false} tick={{ fontSize: 12 }} />
                    <YAxis type="category" dataKey="name" tick={{ fontSize: 11 }} width={90} />
                    <Tooltip />
                    <Bar dataKey="count" name="Players" fill={COLOR_PRIMARY} radius={[0, 4, 4, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            )}
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
        <MyUpcomingActivities
          activityPath={(activity) => `/dashboard/coach/activities/${activity.id}`}
          renderAction={(activity) => (
            <Link to={`/dashboard/coach/activities/${activity.id}`} className="btn btn-primary btn-xs shrink-0">
              Take attendance
            </Link>
          )}
        />
        <MyAnnouncements
          announcementPath={(announcement) => `/dashboard/coach/announcements/${announcement.id}`}
        />
      </div>
    </div>
  )
}
