import { getUsers } from './api/users'
import { getCoaches } from './api/profiles'
import { getPlayers } from './api/players'
import { getTeams } from './api/teams'
import {
  deletePlayerTeam,
  getPlayerTeamsByPlayer,
  getPlayerTeamsByTeam,
} from './api/playerTeams'
import {
  deleteCoachTeam,
  getCoachTeamsByCoach,
  getCoachTeamsByTeam,
} from './api/coachTeams'
import { findProfileByUserId } from './userProfiles'
import { getAge } from './ageUtils'
import { ROLES } from './roles'
import { deleteAnnouncement, getAnnouncementsByTeam } from './api/announcements'
import { deleteFeeRecord, getFeeRecordsByTeam } from './api/feeRecords'
import { deleteCoachPayment, getCoachPaymentsByCoach } from './api/coachPayments'

export function getRosterLinksForTeam(teamId) {
  return getPlayerTeamsByTeam(teamId)
}

export async function getRosterForTeam(teamId) {
  const [links, players] = await Promise.all([getPlayerTeamsByTeam(teamId), getPlayers()])
  const playersById = new Map(players.map((p) => [p.id, p]))
  return links.map((link) => {
    const player = playersById.get(link.playerId)
    return {
      ...link,
      fullName: player?.fullName ?? `Player #${link.playerId}`,
      dateOfBirth: player?.dateOfBirth,
      age: player ? getAge(player.dateOfBirth) : null,
      gender: player?.gender,
    }
  })
}

export async function getPlayerOptions() {
  const players = await getPlayers()
  return players.map((p) => ({
    playerId: p.id,
    label: `${p.fullName} (${getAge(p.dateOfBirth)} yrs, ${p.gender})`,
  }))
}

export async function deleteAllRosterLinksForTeam(teamId) {
  const links = await getPlayerTeamsByTeam(teamId)
  await Promise.all(links.map((link) => deletePlayerTeam(link.id)))
}

export async function deleteAllTeamLinksForPlayer(playerId) {
  const links = await getPlayerTeamsByPlayer(playerId)
  await Promise.all(links.map((link) => deletePlayerTeam(link.id)))
}

export async function getTeamHistoryForPlayer(playerId) {
  const [links, teams] = await Promise.all([getPlayerTeamsByPlayer(playerId), getTeams()])
  const teamsById = new Map(teams.map((t) => [t.id, t]))
  return links
    .filter((link) => teamsById.has(link.teamId))
    .map((link) => ({
      linkId: link.id,
      jerseyNumber: link.jerseyNumber,
      status: link.status,
      joinedAt: link.joinedAt,
      leftAt: link.leftAt,
      ...teamsById.get(link.teamId),
    }))
    .sort((a, b) => {
      if (a.status !== b.status) return a.status === 'ACTIVE' ? -1 : 1
      return b.joinedAt.localeCompare(a.joinedAt)
    })
}

export function getCoachLinksForTeam(teamId) {
  return getCoachTeamsByTeam(teamId)
}

export async function getCoachOptions() {
  const [users, coaches] = await Promise.all([getUsers(), getCoaches()])
  const coachUsersById = new Map(users.filter((u) => u.role === ROLES.COACH).map((u) => [u.id, u]))
  return coaches
    .filter((coach) => coachUsersById.has(coach.userId))
    .map((coach) => {
      const user = coachUsersById.get(coach.userId)
      return { coachId: coach.id, userId: user.id, label: `${user.fullName} (${user.email})` }
    })
}

export async function getCoachingStaffForTeam(teamId) {
  const [links, options] = await Promise.all([getCoachTeamsByTeam(teamId), getCoachOptions()])
  return links.map((link) => ({ ...link, ...options.find((o) => o.coachId === link.coachId) }))
}

export async function deleteAllCoachLinksForTeam(teamId) {
  const links = await getCoachTeamsByTeam(teamId)
  await Promise.all(links.map((link) => deleteCoachTeam(link.id)))
}

export async function deleteAllAnnouncementsForTeam(teamId) {
  const announcements = await getAnnouncementsByTeam(teamId)
  await Promise.all(announcements.map((a) => deleteAnnouncement(a.id)))
}

export async function deleteAllFeeRecordsForTeam(teamId) {
  const feeRecords = await getFeeRecordsByTeam(teamId)
  await Promise.all(feeRecords.map((f) => deleteFeeRecord(f.id)))
}

export async function deleteAllTeamLinksForCoachUser(userId) {
  const profile = await findProfileByUserId(ROLES.COACH, userId)
  if (!profile) return
  const links = await getCoachTeamsByCoach(profile.id)
  await Promise.all(links.map((link) => deleteCoachTeam(link.id)))
}

export async function deleteAllCoachPaymentsForCoach(userId) {
  const profile = await findProfileByUserId(ROLES.COACH, userId)
  if (!profile) return
  const payments = await getCoachPaymentsByCoach(profile.id)
  await Promise.all(payments.map((p) => deleteCoachPayment(p.id)))
}

export async function getCoachTeamHistory(coachId) {
  const [links, teams] = await Promise.all([getCoachTeamsByCoach(coachId), getTeams()])
  const teamsById = new Map(teams.map((t) => [t.id, t]))
  return links
    .filter((link) => teamsById.has(link.teamId))
    .map((link) => ({
      linkId: link.id,
      role: link.role,
      status: link.status,
      joinedAt: link.joinedAt,
      leftAt: link.leftAt,
      ...teamsById.get(link.teamId),
    }))
    .sort((a, b) => {
      if (a.status !== b.status) return a.status === 'ACTIVE' ? -1 : 1
      return b.joinedAt.localeCompare(a.joinedAt)
    })
}
