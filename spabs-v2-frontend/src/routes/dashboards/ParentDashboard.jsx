import { useEffect, useMemo, useState } from 'react'
import { Bar, BarChart, CartesianGrid, Cell, Legend, Pie, PieChart, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { getMyParentProfile } from '../../lib/api/profiles'
import { getPlayersForParent } from '../../lib/playerRelations'
import { getTeamHistoryForPlayer } from '../../lib/teamRelations'
import { getAttendanceSummary } from '../../lib/api/attendances'
import { getPlayerTeamProgress } from '../../lib/api/playerProgress'
import { getFeeRecordsByPlayer } from '../../lib/api/feeRecords'
import { getMyUpcomingActivities } from '../../lib/api/activities'
import { formatCurrency } from '../../lib/formatCurrency'
import MyAnnouncements from '../../features/dashboard/MyAnnouncements'
import MyUpcomingActivities from '../../features/dashboard/MyUpcomingActivities'

const COLOR_PRIMARY = '#148634'
const COLOR_INFO = '#3b82f6'
const COLOR_SUCCESS = '#22c55e'
const COLOR_ERROR = '#ef4444'

export default function ParentDashboard() {
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [children, setChildren] = useState([])

  const [upcomingCount, setUpcomingCount] = useState(0)
  const [feeTotals, setFeeTotals] = useState({ paid: 0, outstanding: 0 })
  const [teamChartData, setTeamChartData] = useState([])
  const [childrenByTeamId, setChildrenByTeamId] = useState(new Map())

  useEffect(() => {
    let cancelled = false

    async function load() {
      setLoading(true)
      setError(null)
      try {
        const parent = await getMyParentProfile()
        const kids = await getPlayersForParent(parent.id)
        if (cancelled) return
        setChildren(kids)

        const [feeRecordLists, upcoming, teamHistories] = await Promise.all([
          Promise.all(kids.map((k) => getFeeRecordsByPlayer(k.id))),
          getMyUpcomingActivities(),
          Promise.all(kids.map((k) => getTeamHistoryForPlayer(k.id))),
        ])
        if (cancelled) return

        const allRecords = feeRecordLists.flat()
        setFeeTotals({
          paid: allRecords.filter((r) => r.status === 'PAID').reduce((sum, r) => sum + Number(r.amount), 0),
          outstanding: allRecords
            .filter((r) => r.status !== 'PAID')
            .reduce((sum, r) => sum + Number(r.amount), 0),
        })

        const now = new Date()
        const in7Days = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000)
        setUpcomingCount(
          upcoming.filter((a) => {
            const start = new Date(a.startAt)
            return start >= now && start <= in7Days
          }).length,
        )

        const byTeamId = new Map()
        for (let i = 0; i < kids.length; i++) {
          for (const t of teamHistories[i].filter((th) => th.status === 'ACTIVE')) {
            if (!byTeamId.has(t.id)) byTeamId.set(t.id, [])
            byTeamId.get(t.id).push(kids[i])
          }
        }
        setChildrenByTeamId(byTeamId)

        const activePairs = kids.flatMap((k, i) =>
          teamHistories[i].filter((t) => t.status === 'ACTIVE').map((t) => ({ child: k, team: t })),
        )
        const [summaries, progressSummaries] = await Promise.all([
          Promise.all(activePairs.map((p) => getAttendanceSummary(p.child.id, p.team.id))),
          Promise.all(activePairs.map((p) => getPlayerTeamProgress(p.child.id, p.team.id))),
        ])
        if (cancelled) return

        setTeamChartData(
          activePairs.map((p, i) => ({
            name: kids.length > 1 ? `${p.child.fullName} · ${p.team.name}` : p.team.name,
            attendance: summaries[i].totalRecords > 0 ? summaries[i].attendancePercentage : 0,
            progress: progressSummaries[i].totalModules > 0 ? progressSummaries[i].overallPercentage : 0,
          })),
        )
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
          <div className="stat-title">Children</div>
          <div className="stat-value text-2xl">{children.length}</div>
        </div>
        <div className="stat">
          <div className="stat-title">Upcoming activities (7 days)</div>
          <div className="stat-value text-2xl">{upcomingCount}</div>
        </div>
        <div className="stat">
          <div className="stat-title">Outstanding fees</div>
          <div className="stat-value text-error text-2xl">{formatCurrency(feeTotals.outstanding)}</div>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-3">
        <div className="card bg-base-100 shadow-md lg:col-span-2">
          <div className="card-body">
            <h2 className="card-title text-lg">Attendance &amp; progress by team</h2>
            {teamChartData.length === 0 ? (
              <p className="text-base-content/60 text-sm">Not currently on any team.</p>
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
            <h2 className="card-title text-lg">Fees (all children)</h2>
            {feeTotals.paid === 0 && feeTotals.outstanding === 0 ? (
              <p className="text-base-content/60 text-sm">No fee records yet.</p>
            ) : (
              <>
                <div className="h-40 w-full">
                  <ResponsiveContainer width="100%" height="100%">
                    <PieChart>
                      <Pie data={feePieData} dataKey="value" nameKey="name" innerRadius={40} outerRadius={62}>
                        <Cell fill={COLOR_SUCCESS} />
                        <Cell fill={COLOR_ERROR} />
                      </Pie>
                      <Tooltip formatter={(value) => formatCurrency(value)} />
                    </PieChart>
                  </ResponsiveContainer>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-success">Paid: {formatCurrency(feeTotals.paid)}</span>
                  <span className="text-error">Owed: {formatCurrency(feeTotals.outstanding)}</span>
                </div>
              </>
            )}
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 gap-4 lg:grid-cols-2">
        <MyUpcomingActivities
          activityPath={(activity) => `/dashboard/parent/activities/${activity.id}`}
          playerNames={(activity) =>
            (childrenByTeamId.get(activity.teamId) ?? []).map((c) => c.fullName).join(', ')
          }
        />
        <MyAnnouncements
          announcementPath={(announcement) => `/dashboard/parent/announcements/${announcement.id}`}
        />
      </div>
    </div>
  )
}
