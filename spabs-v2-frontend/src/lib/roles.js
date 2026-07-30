export const ROLES = {
  SUPER_ADMIN: 'SUPER_ADMIN',
  ADMIN: 'ADMIN',
  COACH: 'COACH',
  PARENT: 'PARENT',
}

export const ADMIN_ROLES = [ROLES.SUPER_ADMIN, ROLES.ADMIN]

const DASHBOARD_PATH_BY_ROLE = {
  [ROLES.SUPER_ADMIN]: '/dashboard/super-admin',
  [ROLES.ADMIN]: '/dashboard/admin',
  [ROLES.COACH]: '/dashboard/coach',
  [ROLES.PARENT]: '/dashboard/parent',
}

export function dashboardPathForRole(role) {
  return DASHBOARD_PATH_BY_ROLE[role] ?? '/login'
}
