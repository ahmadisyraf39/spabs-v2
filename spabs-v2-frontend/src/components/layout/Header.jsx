import { useAuth } from '../../features/auth/useAuth'

export default function Header() {
  const { user } = useAuth()

  return (
    <header className="navbar bg-base-100 border-base-300 border-b px-4 sm:px-6">
      <div className="navbar-start">
        <label htmlFor="app-drawer" className="btn btn-ghost btn-sm" aria-label="Toggle sidebar">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            className="h-5 w-5"
            fill="none"
            viewBox="0 0 24 24"
            stroke="currentColor"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M4 6h16M4 12h16M4 18h16"
            />
          </svg>
        </label>
      </div>

      <div className="navbar-end">
        <div className="text-right">
          <p className="text-sm font-medium">{user?.fullName ?? user?.email}</p>
          <p className="text-primary text-xs font-medium">{user?.role}</p>
        </div>
      </div>
    </header>
  )
}
