import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../features/auth/useAuth'
import { dashboardPathForRole } from '../lib/roles'

export default function ProtectedRoute() {
  const { status, user } = useAuth()
  const location = useLocation()

  if (status === 'idle') {
    return (
      <div className="flex h-screen items-center justify-center">
        <span className="loading loading-spinner loading-lg text-primary" />
      </div>
    )
  }

  if (status === 'unauthenticated') {
    return <Navigate to="/login" replace />
  }

  const onChangePasswordPage = location.pathname === '/change-password'

  if (user?.mustChangePassword && !onChangePasswordPage) {
    return <Navigate to="/change-password" replace />
  }

  if (!user?.mustChangePassword && onChangePasswordPage) {
    return <Navigate to={dashboardPathForRole(user?.role)} replace />
  }

  return <Outlet />
}
