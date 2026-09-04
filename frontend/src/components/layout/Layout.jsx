import React, { useState, useEffect } from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { LayoutDashboard, HandCoins, Receipt, FileBarChart, Users, LogOut, Sparkles, CalendarDays, Gavel, Landmark, Gift, KeyRound, Eye, ShieldAlert, DoorOpen, Menu, X, Archive, Lock } from 'lucide-react'
import { useAuth } from '../../context/AuthContext'
import ChangePasswordModal from '../common/ChangePasswordModal'
import MobileNavDrawer from './MobileNavDrawer'
import { festivalYearApi } from '../../api/festivalYearApi'

const NAV_ITEMS = [
  { to: '/', label: 'Dashboard', icon: LayoutDashboard, end: true },
  { to: '/festival-setup', label: 'Festival Setup', icon: CalendarDays },
  // Active Festival Guard: these five require an active festival year to
  // actually be usable (see FestivalYearGate) - lockedByYear just adds a
  // small lock indicator here so it's visible before clicking in, it
  // never removes the nav item or blocks the route itself.
  { to: '/collections', label: 'Collections', icon: HandCoins, lockedByYear: true },
  { to: '/expenses', label: 'Expenses', icon: Receipt, lockedByYear: true },
  { to: '/sponsorships', label: 'Sponsorships', icon: Gift, presidentOnly: true, lockedByYear: true },
  { to: '/auction', label: 'Auction', icon: Gavel, lockedByYear: true },
  { to: '/loans', label: 'Loans', icon: Landmark, presidentOnly: true },
  { to: '/reports', label: 'Reports', icon: FileBarChart, lockedByYear: true },
  // Festival Archives is intentionally NEVER locked by the active-year
  // guard above - browsing past, already-archived festivals must always
  // work regardless of whether a new year has been set up yet.
  { to: '/archives', label: 'Festival Archives', icon: Archive },
  // Staff management stays off-limits during Tenant Inspection, in both
  // modes (see InspectionModeFilter) - but VIEWING the roster is allowed,
  // so the nav item stays visible; Members.jsx itself hides the
  // add/deactivate/remove actions when inspecting.
  { to: '/members', label: 'Committee', icon: Users },
]

export default function Layout({ children, festivalName = 'Ganesh Utsav' }) {
  const { user, logout, exitInspection } = useAuth()
  const navigate = useNavigate()
  const [changePasswordOpen, setChangePasswordOpen] = useState(false)
  const [exiting, setExiting] = useState(false)
  const [mobileNavOpen, setMobileNavOpen] = useState(false)
  // null while we don't know yet - avoids flashing a lock icon on every
  // nav item for a split second before the check resolves
  const [hasActiveYear, setHasActiveYear] = useState(null)

  useEffect(() => {
    let cancelled = false
    festivalYearApi.getActive()
      .then((year) => { if (!cancelled) setHasActiveYear(!!year) })
      .catch(() => { if (!cancelled) setHasActiveYear(false) })
    return () => { cancelled = true }
  }, [])

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  const handleExitInspection = async () => {
    setExiting(true)
    try {
      await exitInspection()
      navigate('/')
    } finally {
      setExiting(false)
    }
  }

  const visibleNavItems = NAV_ITEMS.filter((item) =>
    (!item.presidentOnly || user?.role === 'PRESIDENT') &&
    !(item.inspectionExcluded && user?.isInspecting)
  )

  const isLocked = (item) => item.lockedByYear && hasActiveYear === false

  return (
    <div className="min-h-screen flex flex-col md:flex-row">
      {/* Tenant Inspection banner - persistent, unmissable, shown above everything else */}
      {user?.isInspecting && (
        <div className={`fixed top-0 left-0 right-0 z-40 flex flex-col sm:flex-row items-center justify-between gap-2 px-4 py-2 text-sm font-medium ${
          user.inspectionMode === 'ADMIN_OVERRIDE' ? 'bg-amber-600 text-white' : 'bg-maroon-900 text-white'
        }`}>
          <span className="flex items-center gap-2 text-center sm:text-left">
            {user.inspectionMode === 'ADMIN_OVERRIDE' ? <ShieldAlert size={16} className="shrink-0" /> : <Eye size={16} className="shrink-0" />}
            <span>
              Inspecting: <strong>{user.committeeName}</strong> ({user.tenantCode}) ·{' '}
              {user.inspectionMode === 'ADMIN_OVERRIDE' ? 'Admin Override' : 'Read-Only'}
            </span>
          </span>
          <button
            onClick={handleExitInspection}
            disabled={exiting}
            className="flex items-center gap-1.5 px-3 py-1 rounded-lg bg-white/15 hover:bg-white/25 transition disabled:opacity-60 shrink-0"
          >
            <DoorOpen size={14} /> {exiting ? 'Exiting…' : 'Exit Inspection'}
          </button>
        </div>
      )}

      {/* Desktop sidebar */}
      <aside className={`hidden md:flex md:flex-col w-64 shrink-0 bg-gradient-to-b from-maroon-700 to-maroon-800 text-white ${user?.isInspecting ? 'mt-9' : ''}`}>
        <div className="flex items-center gap-3 px-6 py-6 border-b border-white/10">
          <div className="w-10 h-10 rounded-full bg-saffron-400 flex items-center justify-center text-xl">🐘</div>
          <div>
            <p className="font-display font-bold leading-tight">{festivalName}</p>
            <p className="text-xs text-saffron-200">Expense Tracker</p>
          </div>
        </div>
        <nav className="flex-1 px-3 py-4 space-y-1">
          {visibleNavItems.map(({ to, label, icon: Icon, end, ...item }) => (
            <NavLink
              key={to}
              to={to}
              end={end}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3 py-2.5 rounded-xl transition font-medium ${
                  isActive ? 'bg-saffron-500 text-white' : 'text-saffron-100 hover:bg-white/10'
                }`
              }
            >
              <Icon size={18} />
              <span className="flex-1">{label}</span>
              {isLocked(item) && <Lock size={13} className="text-saffron-300 shrink-0" />}
            </NavLink>
          ))}
        </nav>
        <div className="px-3 py-4 border-t border-white/10">
          <div className="px-3 mb-2 text-sm text-saffron-200">
            {user?.name} <span className="text-saffron-300">· {user?.isInspecting ? `${user.role} (inspecting)` : user?.role}</span>
          </div>
          {!user?.isInspecting && (
            <button
              onClick={() => setChangePasswordOpen(true)}
              className="flex items-center gap-3 px-3 py-2.5 rounded-xl w-full text-saffron-100 hover:bg-white/10 transition"
            >
              <KeyRound size={18} /> Change Password
            </button>
          )}
          <button
            onClick={handleLogout}
            className="flex items-center gap-3 px-3 py-2.5 rounded-xl w-full text-saffron-100 hover:bg-white/10 transition"
          >
            <LogOut size={18} /> Logout
          </button>
        </div>
      </aside>

      <ChangePasswordModal open={changePasswordOpen} onClose={() => setChangePasswordOpen(false)} />

      <div className={`flex-1 flex flex-col min-w-0 ${user?.isInspecting ? 'mt-9' : ''}`}>
        {/* Top header - hamburger menu on mobile, welcome message on desktop */}
        <header className="sticky top-0 z-30 bg-white/90 backdrop-blur border-b border-saffron-100 px-4 md:px-8 py-3 flex items-center justify-between">
          <div className="flex items-center gap-2 md:hidden">
            <div className="w-8 h-8 rounded-full bg-saffron-400 flex items-center justify-center text-base">🐘</div>
            <span className="font-display font-bold text-maroon-800">{festivalName}</span>
          </div>
          <div className="hidden md:flex items-center gap-2 text-maroon-600">
            <Sparkles size={18} className="text-gold-500" />
            <span className="font-medium">
              {user?.isInspecting
                ? `Viewing ${user.committeeName}'s dashboard as their President would see it.`
                : `Ganpati Bappa Morya! Welcome back${user?.name ? `, ${user.name}` : ''}.`}
            </span>
          </div>
          <button
            onClick={() => setMobileNavOpen((v) => !v)}
            className="md:hidden text-maroon-600"
            aria-label={mobileNavOpen ? 'Close menu' : 'Open menu'}
          >
            {mobileNavOpen ? <X size={22} /> : <Menu size={22} />}
          </button>
        </header>

        {/* Mobile nav drawer - every screen the desktop sidebar has,
            reachable on mobile too (a curated bottom tab bar used to hide
            Festival Setup, Sponsorships, Loans, and Committee entirely).
            Rendered as a fixed overlay (see MobileNavDrawer) so opening
            it never pushes the page content below the header down. */}
        <MobileNavDrawer open={mobileNavOpen} onClose={() => setMobileNavOpen(false)}>
          <div className="flex items-center justify-between px-4 py-4 border-b border-white/10">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 rounded-full bg-saffron-400 flex items-center justify-center text-base">🐘</div>
              <span className="font-display font-bold text-white">{festivalName}</span>
            </div>
            <button onClick={() => setMobileNavOpen(false)} className="text-saffron-100" aria-label="Close menu">
              <X size={20} />
            </button>
          </div>
          <nav className="px-3 py-3 space-y-1">
            {visibleNavItems.map(({ to, label, icon: Icon, end, ...item }) => (
              <NavLink
                key={to}
                to={to}
                end={end}
                onClick={() => setMobileNavOpen(false)}
                className={({ isActive }) =>
                  `flex items-center gap-3 px-3 py-2.5 rounded-xl transition font-medium ${
                    isActive ? 'bg-saffron-500 text-white' : 'text-saffron-100 hover:bg-white/10'
                  }`
                }
              >
                <Icon size={18} />
                <span className="flex-1">{label}</span>
                {isLocked(item) && <Lock size={13} className="text-saffron-300 shrink-0" />}
              </NavLink>
            ))}
            <div className="border-t border-white/10 my-2" />
            <div className="px-3 pb-1 text-xs text-saffron-300">
              {user?.name} · {user?.isInspecting ? `${user.role} (inspecting)` : user?.role}
            </div>
            {!user?.isInspecting && (
              <button
                onClick={() => { setChangePasswordOpen(true); setMobileNavOpen(false) }}
                className="flex items-center gap-3 px-3 py-2.5 rounded-xl w-full text-saffron-100 hover:bg-white/10 transition"
              >
                <KeyRound size={18} /> Change Password
              </button>
            )}
            <button
              onClick={handleLogout}
              className="flex items-center gap-3 px-3 py-2.5 rounded-xl w-full text-saffron-100 hover:bg-white/10 transition"
            >
              <LogOut size={18} /> Logout
            </button>
          </nav>
        </MobileNavDrawer>

        {/* Page content */}
        <main className="flex-1 px-4 md:px-8 py-6">{children}</main>
      </div>
    </div>
  )
}
