import { getUsers } from './api/users'
import { getParents } from './api/profiles'
import { getPlayers } from './api/players'
import {
  deletePlayerParent,
  getPlayerParentsByParent,
  getPlayerParentsByPlayer,
} from './api/playerParents'
import { ROLES } from './roles'

export function getParentLinksForPlayer(playerId) {
  return getPlayerParentsByPlayer(playerId)
}

export async function deleteAllParentLinksForPlayer(playerId) {
  const links = await getParentLinksForPlayer(playerId)
  await Promise.all(links.map((link) => deletePlayerParent(link.id)))
}

export async function getParentOptions() {
  const [users, parents] = await Promise.all([getUsers(), getParents()])
  const parentUsersById = new Map(users.filter((u) => u.role === ROLES.PARENT).map((u) => [u.id, u]))

  return parents
    .filter((parent) => parentUsersById.has(parent.userId))
    .map((parent) => {
      const user = parentUsersById.get(parent.userId)
      return { parentId: parent.id, userId: user.id, label: `${user.fullName} (${user.email})` }
    })
}

export async function getGuardiansForPlayer(playerId) {
  const [links, users, parents] = await Promise.all([getParentLinksForPlayer(playerId), getUsers(), getParents()])
  const parentsById = new Map(parents.map((p) => [p.id, p]))
  const usersById = new Map(users.map((u) => [u.id, u]))
  return links.map((link) => {
    const parent = parentsById.get(link.parentId)
    const user = parent ? usersById.get(parent.userId) : null
    return {
      linkId: link.id,
      parentId: link.parentId,
      userId: user?.id ?? null,
      fullName: user?.fullName ?? `Parent #${link.parentId}`,
      email: user?.email ?? null,
      phoneNumber: user?.phoneNumber ?? null,
      emergencyContact: parent?.emergencyContact ?? null,
      relationship: link.relationship,
    }
  })
}

export async function getPlayersForParent(parentId) {
  const [links, players] = await Promise.all([getPlayerParentsByParent(parentId), getPlayers()])
  const playerIds = new Set(links.map((link) => link.playerId))
  return players
    .filter((player) => playerIds.has(player.id))
    .map((player) => ({
      ...player,
      relationship: links.find((link) => link.playerId === player.id)?.relationship,
    }))
}
