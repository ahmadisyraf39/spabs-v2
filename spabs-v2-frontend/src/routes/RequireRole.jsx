import { Navigate, Outlet } from 'react-router-dom'
import { useAuth } from '../features/auth/useAuth'
import { dashboardPathForRole } from '../lib/roles'

export default function RequireRole({ allowedRoles }) {
  const { user } = useAuth()

  if (!allowedRoles.includes(user?.role)) {
    return <Navigate to={dashboardPathForRole(user?.role)} replace />
  }

  return <Outlet />
}
