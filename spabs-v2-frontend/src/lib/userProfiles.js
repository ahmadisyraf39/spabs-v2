import * as profilesApi from './api/profiles'
import { ROLES } from './roles'

export const GET_PROFILES_BY_ROLE = {
  [ROLES.SUPER_ADMIN]: profilesApi.getAdmins,
  [ROLES.ADMIN]: profilesApi.getAdmins,
  [ROLES.COACH]: profilesApi.getCoaches,
  [ROLES.PARENT]: profilesApi.getParents,
}
export const CREATE_PROFILE_BY_ROLE = {
  [ROLES.SUPER_ADMIN]: profilesApi.createAdmin,
  [ROLES.ADMIN]: profilesApi.createAdmin,
  [ROLES.COACH]: profilesApi.createCoach,
  [ROLES.PARENT]: profilesApi.createParent,
}
export const UPDATE_PROFILE_BY_ROLE = {
  [ROLES.SUPER_ADMIN]: profilesApi.updateAdmin,
  [ROLES.ADMIN]: profilesApi.updateAdmin,
  [ROLES.COACH]: profilesApi.updateCoach,
  [ROLES.PARENT]: profilesApi.updateParent,
}
export const DELETE_PROFILE_BY_ROLE = {
  [ROLES.SUPER_ADMIN]: profilesApi.deleteAdmin,
  [ROLES.ADMIN]: profilesApi.deleteAdmin,
  [ROLES.COACH]: profilesApi.deleteCoach,
  [ROLES.PARENT]: profilesApi.deleteParent,
}

export async function findProfileByUserId(role, userId) {
  const profiles = await GET_PROFILES_BY_ROLE[role]()
  return profiles.find((p) => p.userId === Number(userId)) ?? null
}

export async function deleteProfileForUser(role, userId) {
  const profile = await findProfileByUserId(role, userId)
  if (profile) {
    await DELETE_PROFILE_BY_ROLE[role](profile.id)
  }
}
