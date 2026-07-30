import { useState } from 'react'
import { NavLink } from 'react-router-dom'
import logo from '../../assets/logo.svg'
import { useAuth } from '../../features/auth/useAuth'
import ConfirmModal from '../ui/ConfirmModal'
import { UserIcon } from './navIcons'

export default function Sidebar({ navItems }) {
  const { logout } = useAuth()
  const [confirmingLogout, setConfirmingLogout] = useState(false)

  return (
    <div className="drawer-side z-20">
      <label htmlFor="app-drawer" aria-label="Close sidebar" className="drawer-overlay"></label>
      <aside className="bg-primary text-primary-content is-drawer-close:w-16 is-drawer-open:w-64 flex h-full flex-col transition-all duration-200">
        <div className="flex h-16 items-center justify-center gap-2 px-4">
          <img src={logo} alt="" className="bg-base-100 h-9 w-9 shrink-0 rounded-lg p-1" />
          <span className="is-drawer-close:hidden truncate text-lg font-semibold">SPABS-V2</span>
        </div>

        <nav className="flex-1 py-4">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={({ isActive }) =>
                `hover:bg-secondary flex items-center gap-3 px-5 py-2.5 text-sm transition-colors ${
                  isActive ? 'bg-secondary font-medium' : ''
                }`
              }
            >
              <span className="shrink-0">{item.icon}</span>
              <span className="is-drawer-close:hidden truncate">{item.label}</span>
            </NavLink>
          ))}
        </nav>

        <div className="border-primary-content/10 mt-auto border-t p-3">
          <NavLink
            to="/profile"
            className={({ isActive }) =>
              `hover:bg-secondary flex items-center gap-3 rounded px-2 py-2 text-sm transition-colors ${
                isActive ? 'bg-secondary font-medium' : ''
              }`
            }
          >
            <span className="shrink-0">
              <UserIcon />
            </span>
            <span className="is-drawer-close:hidden truncate">My profile</span>
          </NavLink>
          <button
            type="button"
            onClick={() => setConfirmingLogout(true)}
            className="hover:bg-secondary flex w-full items-center gap-3 rounded px-2 py-2 text-sm transition-colors"
          >
            <svg
              xmlns="http://www.w3.org/2000/svg"
              className="h-5 w-5 shrink-0"
              fill="none"
              viewBox="0 0 24 24"
              stroke="currentColor"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                strokeWidth={2}
                d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1"
              />
            </svg>
            <span className="is-drawer-close:hidden truncate">Log out</span>
          </button>
        </div>
      </aside>

      {confirmingLogout && (
        <ConfirmModal
          title="Log out"
          body="Are you sure you want to log out?"
          confirmLabel="Log out"
          confirmClass="btn-primary"
          onConfirm={logout}
          onCancel={() => setConfirmingLogout(false)}
        />
      )}
    </div>
  )
}
