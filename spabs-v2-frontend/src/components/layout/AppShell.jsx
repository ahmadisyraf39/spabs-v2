import { useEffect, useState } from 'react'
import { Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../../features/auth/useAuth'
import { ADMIN_ROLES, ROLES, dashboardPathForRole } from '../../lib/roles'
import Header from './Header'
import {
  ArchiveBoxIcon,
  BanknotesIcon,
  CalendarIcon,
  CurrencyDollarIcon,
  GiftIcon,
  HomeIcon,
  MegaphoneIcon,
  PresentationChartIcon,
  TrendingUpIcon,
  TrophyIcon,
  UserGroupIcon,
  UserIcon,
} from './navIcons'
import Sidebar from './Sidebar'

const DRAWER_ID = 'app-drawer'
const DESKTOP_QUERY = '(min-width: 1024px)'

export default function AppShell() {
  const { user } = useAuth()
  const location = useLocation()
  const [startExpanded] = useState(() => window.matchMedia(DESKTOP_QUERY).matches)

  const navItems = [
    { to: dashboardPathForRole(user?.role), label: 'Dashboard', icon: <HomeIcon />, end: true },
  ]
  if (ADMIN_ROLES.includes(user?.role)) {
    navItems.push({ to: '/users', label: 'Users', icon: <UserIcon /> })
    navItems.push({ to: '/players', label: 'Players', icon: <UserGroupIcon /> })
    navItems.push({ to: '/teams', label: 'Teams', icon: <TrophyIcon /> })
    navItems.push({ to: '/activities', label: 'Activities', icon: <CalendarIcon /> })
    navItems.push({ to: '/skills', label: 'Skills', icon: <TrendingUpIcon /> })
    navItems.push({ to: '/fee-records', label: 'Fees', icon: <CurrencyDollarIcon /> })
    navItems.push({ to: '/inventory', label: 'Inventory', icon: <ArchiveBoxIcon /> })
    navItems.push({ to: '/sponsorships', label: 'Sponsorship', icon: <GiftIcon /> })
    navItems.push({ to: '/announcements', label: 'Announcements', icon: <MegaphoneIcon /> })
    navItems.push({ to: '/coach-payments', label: 'Payroll', icon: <BanknotesIcon /> })
    navItems.push({ to: '/finance-transactions', label: 'Ledger', icon: <PresentationChartIcon /> })
  } else if (user?.role === ROLES.COACH) {
    navItems.push({ to: '/dashboard/coach/activities', label: 'Activities', icon: <CalendarIcon /> })
    navItems.push({ to: '/dashboard/coach/teams', label: 'Teams', icon: <TrophyIcon /> })
    navItems.push({ to: '/dashboard/coach/payroll', label: 'Payroll', icon: <BanknotesIcon /> })
    navItems.push({ to: '/dashboard/coach/inventory', label: 'Inventory', icon: <ArchiveBoxIcon /> })
    navItems.push({
      to: '/dashboard/coach/announcements',
      label: 'Announcements',
      icon: <MegaphoneIcon />,
    })
  } else if (user?.role === ROLES.PARENT) {
    navItems.push({ to: '/dashboard/parent/activities', label: 'Activities', icon: <CalendarIcon /> })
    navItems.push({ to: '/dashboard/parent/players', label: 'Players', icon: <UserGroupIcon /> })
    navItems.push({ to: '/dashboard/parent/fees', label: 'Fees', icon: <CurrencyDollarIcon /> })
    navItems.push({
      to: '/dashboard/parent/announcements',
      label: 'Announcements',
      icon: <MegaphoneIcon />,
    })
  }

  useEffect(() => {
    if (window.matchMedia(DESKTOP_QUERY).matches) return
    const checkbox = document.getElementById(DRAWER_ID)
    if (checkbox) checkbox.checked = false
  }, [location.pathname])

  return (
    <div className="drawer lg:drawer-open">
      <input id={DRAWER_ID} type="checkbox" className="drawer-toggle" defaultChecked={startExpanded} />
      <div className="drawer-content flex min-h-screen flex-col">
        <Header />
        <main className="bg-base-200 flex-1 overflow-y-auto p-4 sm:p-6">
          <Outlet />
        </main>
      </div>
      <Sidebar navItems={navItems} />
    </div>
  )
}
