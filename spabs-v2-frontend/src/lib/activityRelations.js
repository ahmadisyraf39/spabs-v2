import { deleteActivity, getActivities, getActivitiesByTeam } from './api/activities'
import { deleteAttendance, getAttendancesByActivity, getAttendancesByPlayer } from './api/attendances'
import { getTeams } from './api/teams'
import { getRosterForTeam } from './teamRelations'

export async function getAttendanceGridForActivity(activityId, teamId, activityDate) {
  const [roster, records] = await Promise.all([
    getRosterForTeam(teamId),
    getAttendancesByActivity(activityId),
  ])
  const recordByPlayerId = new Map(records.map((r) => [r.playerId, r]))
  const eligible = roster.filter(
    (r) => r.joinedAt <= activityDate && (!r.leftAt || r.leftAt >= activityDate),
  )
  return eligible.map((r) => {
    const existing = recordByPlayerId.get(r.playerId)
    return {
      playerId: r.playerId,
      fullName: r.fullName,
      attendanceId: existing?.id ?? null,
      status: existing?.status ?? '',
      notes: existing?.notes ?? '',
    }
  })
}

export async function getAttendanceHistoryForPlayer(playerId) {
  const [records, activities, teams] = await Promise.all([
    getAttendancesByPlayer(playerId),
    getActivities(),
    getTeams(),
  ])
  const activitiesById = new Map(activities.map((a) => [a.id, a]))
  const teamsById = new Map(teams.map((t) => [t.id, t]))
  return records.map((record) => {
    const activity = activitiesById.get(record.activityId)
    const team = activity ? teamsById.get(activity.teamId) : null
    return {
      id: record.id,
      activityId: record.activityId,
      displayTitle: activity
        ? activity.title?.trim()
          ? activity.title
          : activity.type
        : `Activity #${record.activityId} (deleted)`,
      type: activity?.type ?? null,
      teamId: activity?.teamId ?? null,
      teamName: team?.name ?? (activity ? `Team #${activity.teamId}` : '—'),
      startAt: activity?.startAt ?? null,
      status: record.status,
      notes: record.notes,
    }
  })
}

export async function deleteAllAttendanceForActivity(activityId) {
  const records = await getAttendancesByActivity(activityId)
  await Promise.all(records.map((r) => deleteAttendance(r.id)))
}

export async function deleteAllAttendanceForPlayer(playerId) {
  const records = await getAttendancesByPlayer(playerId)
  await Promise.all(records.map((r) => deleteAttendance(r.id)))
}

export async function deleteAllActivitiesForTeam(teamId) {
  const activities = await getActivitiesByTeam(teamId)
  await Promise.all(
    activities.map(async (activity) => {
      await deleteAllAttendanceForActivity(activity.id)
      await deleteActivity(activity.id)
    }),
  )
}
