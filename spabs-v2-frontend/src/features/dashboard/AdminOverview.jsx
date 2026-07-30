import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import {
  Area,
  AreaChart,
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import { getAnnouncements } from '../../lib/api/announcements'
import { getActivities } from '../../lib/api/activities'
import { getAttendanceSummary } from '../../lib/api/attendances'
import { getCoaches } from '../../lib/api/profiles'
import { getFeeSummaryForTeam } from '../../lib/api/feeRecords'
import { getFinanceTransactions } from '../../lib/api/financeTransactions'
import { getPlayers } from '../../lib/api/players'
import { getPlayerTeamProgress } from '../../lib/api/playerProgress'
import { getTeams } from '../../lib/api/teams'
import { getRosterForTeam } from '../../lib/teamRelations'
import { formatDateTime } from '../../lib/dateTimeUtils'
import { formatCurrency } from '../../lib/formatCurrency'

const COLOR_PRIMARY = '#148634'
const COLOR_INFO = '#3b82f6'
const COLOR_SUCCESS = '#22c55e'
const COLOR_ERROR = '#ef4444'

function monthKey(isoDate) {
  return isoDate.slice(0, 7)
}

function monthLabel(key) {
  const [year, month] = key.split('-').map(Number)
  return new Date(year, month - 1, 1).toLocaleDateString(undefined, { month: 'short', year: '2-digit' })
}

export default function AdminOverview() {
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const [playerCount, setPlayerCount] = useState(0)
  const [teamCount, setTeamCount] = useState(0)
  const [coachCount, setCoachCount] = useState(0)
  const [upcomingCount, setUpcomingCount] = useState(0)

  const [financeTrend, setFinanceTrend] = useState([])
  const [feeTotals, setFeeTotals] = useState({ paid: 0, outstanding: 0 })
  const [teamRoster, setTeamRoster] = useState([])
  const [teamPerformance, setTeamPerformance] = useState([])
  const [announcements, setAnnouncements] = useState([])
  const [upcomingActivities, setUpcomingActivities] = useState([])

  useEffect(() => {
    let cancelled = false

    async function load() {
      setLoading(true)
      setError(null)
      try {
        const [players, teams, coaches, activities, announcementList, transactions] = await Promise.all([
          getPlayers(),
          getTeams(),
          getCoaches(),
          getActivities(),
          getAnnouncements(),
          getFinanceTransactions(),
        ])
        if (cancelled) return

        setPlayerCount(players.length)
        setTeamCount(teams.length)
        setCoachCount(coaches.length)

        const now = new Date()
        const in7Days = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000)
        const upcoming = activities
          .filter((a) => {
            const start = new Date(a.startAt)
            return start >= now && start <= in7Days
          })
          .sort((a, b) => a.startAt.localeCompare(b.startAt))
        setUpcomingCount(upcoming.length)

        const teamsById = new Map(teams.map((t) => [t.id, t]))
        setUpcomingActivities(
          upcoming.slice(0, 8).map((a) => ({
            ...a,
            displayTitle: a.title?.trim() ? a.title : a.type,
            teamName: teamsById.get(a.teamId)?.name ?? `Team #${a.teamId}`,
          })),
        )

        setAnnouncements(
          [...announcementList]
            .sort((a, b) => b.createdAt.localeCompare(a.createdAt))
            .slice(0, 5)
            .map((a) => ({
              ...a,
              teamName: a.teamId ? (teamsById.get(a.teamId)?.name ?? `Team #${a.teamId}`) : null,
            })),
        )

        const byMonth = new Map()
        for (const t of transactions) {
          const key = monthKey(t.transactionDate)
          const entry = byMonth.get(key) ?? { key, income: 0, expense: 0 }
          if (t.financeType === 'INCOME') entry.income += Number(t.amount)
          else entry.expense += Number(t.amount)
          byMonth.set(key, entry)
        }
        const trend = []
        const cursor = new Date()
        cursor.setDate(1)
        for (let i = 11; i >= 0; i--) {
          const d = new Date(cursor.getFullYear(), cursor.getMonth() - i, 1)
          const key = `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`
          const entry = byMonth.get(key) ?? { key, income: 0, expense: 0 }
          trend.push({ ...entry, label: monthLabel(key) })
        }
        setFinanceTrend(trend)

        const feeSummaries = await Promise.all(teams.map((t) => getFeeSummaryForTeam(t.id)))
        if (cancelled) return
        setFeeTotals({
          paid: feeSummaries.reduce((sum, s) => sum + Number(s.totalPaid), 0),
          outstanding: feeSummaries.reduce((sum, s) => sum + Number(s.totalOutstanding), 0),
        })

        const performance = await Promise.all(
          teams.map(async (t) => {
            const roster = (await getRosterForTeam(t.id)).filter((r) => r.status === 'ACTIVE')
            const [summaries, progressSummaries] = await Promise.all([
              Promise.all(roster.map((r) => getAttendanceSummary(r.playerId, t.id))),
              Promise.all(roster.map((r) => getPlayerTeamProgress(r.playerId, t.id))),
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
            return { name: t.name, count: roster.length, attendance: avgAttendance, progress: avgProgress }
          }),
        )
        if (cancelled) return
        setTeamRoster(performance.map((p) => ({ name: p.name, count: p.count })))
        setTeamPerformance(performance)
      } catch (err) {
        if (!cancelled) setError(err.message ?? 'Unable to load dashboard.')
      } finally {
        if (!cancelled) setLoading(false)
      }
    }

    load()
    return () => {
      cancelled = true
    }
  }, [])

  const feePieData = useMemo(
    () => [
      { name: 'Paid', value: feeTotals.paid },
      { name: 'Outstanding', value: feeTotals.outstanding },
    ],
    [feeTotals],
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
          <div className="stat-title">Players</div>
          <div className="stat-value text-2xl">{playerCount}</div>
        </div>
        <div className="stat">
          <div className="stat-title">Teams</div>
          <div className="stat-value text-2xl">{teamCount}</div>
        </div>
        <div className="stat">
          <div className="stat-title">Coaches</div>
          <div className="stat-value text-2xl">{coachCount}</div>
        </div>
        <div className="stat">
          <div className="stat-title">Upcoming activities (7 days)</div>
          <div className="stat-value text-2xl">{upcomingCount}</div>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
        <div className="card bg-base-100 shadow-md">
          <div className="card-body">
            <h2 className="card-title text-lg">Upcoming activities</h2>
            {upcomingActivities.length === 0 ? (
              <p className="text-base-content/60 text-sm">Nothing in the next 7 days.</p>
            ) : (
              <div className="flex flex-col divide-y">
                {upcomingActivities.map((a) => (
                  <div key={a.id} className="py-2 first:pt-0 last:pb-0">
                    <div className="flex items-center gap-2">
                      <Link to={`/activities/${a.id}`} className="link link-primary text-sm font-medium">
                        {a.displayTitle}
                      </Link>
                      <Link to={`/teams/${a.teamId}`} className="link link-primary text-xs">
                        {a.teamName}
                      </Link>
                    </div>
                    <span className="text-base-content/60 text-xs">
                      {formatDateTime(a.startAt)}
                      {a.location && ` · ${a.location}`}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>

        <div className="card bg-base-100 shadow-md">
          <div className="card-body">
            <h2 className="card-title text-lg">Recent announcements</h2>
            {announcements.length === 0 ? (
              <p className="text-base-content/60 text-sm">No announcements yet.</p>
            ) : (
              <div className="flex flex-col divide-y">
                {announcements.map((a) => (
                  <div key={a.id} className="py-2 first:pt-0 last:pb-0">
                    <div className="flex items-center justify-between gap-2">
                      <Link to={`/announcements/${a.id}`} className="link link-primary text-sm font-medium">
                        {a.title}
                      </Link>
                      <span className="badge badge-ghost badge-xs shrink-0">
                        {a.teamName ?? 'Academy-wide'}
                      </span>
                    </div>
                    <span className="text-base-content/60 text-xs">
                      {new Date(a.createdAt).toLocaleDateString()}
                    </span>
                  </div>
                ))}
              </div>
            )}
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
        <div className="card bg-base-100 shadow-md lg:col-span-2">
          <div className="card-body">
            <h2 className="card-title text-lg">Attendance &amp; progress by team</h2>
            {teamPerformance.length === 0 ? (
              <p className="text-base-content/60 text-sm">No teams yet.</p>
            ) : (
              <div className="h-64 w-full">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={teamPerformance}>
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
            {teamRoster.length === 0 ? (
              <p className="text-base-content/60 text-sm">No teams yet.</p>
            ) : (
              <div className="h-64 w-full">
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={teamRoster} layout="vertical" margin={{ left: 16 }}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis type="number" allowDecimals={false} tick={{ fontSize: 12 }} />
                    <YAxis type="category" dataKey="name" tick={{ fontSize: 11 }} width={90} />
                    <Tooltip />
                    <Bar dataKey="count" name="Active players" fill={COLOR_PRIMARY} radius={[0, 4, 4, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            )}
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
        <div className="card bg-base-100 shadow-md lg:col-span-2">
          <div className="card-body">
            <h2 className="card-title text-lg">Finance trend</h2>
            {financeTrend.length === 0 ? (
              <p className="text-base-content/60 text-sm">No finance transactions recorded yet.</p>
            ) : (
              <div className="h-72 w-full">
                <ResponsiveContainer width="100%" height="100%">
                  <AreaChart data={financeTrend}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="label" tick={{ fontSize: 12 }} />
                    <YAxis tick={{ fontSize: 12 }} tickFormatter={(value) => `RM ${value}`} />
                    <Tooltip formatter={(value) => formatCurrency(value)} />
                    <Legend />
                    <Area
                      type="monotone"
                      dataKey="income"
                      name="Income"
                      stroke={COLOR_SUCCESS}
                      strokeWidth={2}
                      fill={COLOR_SUCCESS}
                      fillOpacity={0}
                    />
                    <Area
                      type="monotone"
                      dataKey="expense"
                      name="Expense"
                      stroke={COLOR_ERROR}
                      strokeWidth={2}
                      fill={COLOR_ERROR}
                      fillOpacity={0}
                    />
                  </AreaChart>
                </ResponsiveContainer>
              </div>
            )}
          </div>
        </div>

        <div className="card bg-base-100 shadow-md">
          <div className="card-body">
            <h2 className="card-title text-lg">Fee collection</h2>
            {feeTotals.paid === 0 && feeTotals.outstanding === 0 ? (
              <p className="text-base-content/60 text-sm">No fee records yet.</p>
            ) : (
              <>
                <div className="h-48 w-full">
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie data={feePieData} dataKey="value" nameKey="name" innerRadius={45} outerRadius={70}>
                        <Cell fill={COLOR_SUCCESS} />
                        <Cell fill={COLOR_ERROR} />
                      </Pie>
                      <Tooltip formatter={(value) => formatCurrency(value)} />
                    </PieChart>
                  </ResponsiveContainer>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-success">Paid: {formatCurrency(feeTotals.paid)}</span>
                  <span className="text-error">Outstanding: {formatCurrency(feeTotals.outstanding)}</span>
                </div>
              </>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}
