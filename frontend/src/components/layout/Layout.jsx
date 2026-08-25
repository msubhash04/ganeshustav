import React from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { LayoutDashboard, HandCoins, Receipt, FileBarChart, Users, LogOut, Sparkles, CalendarDays, Gavel, Landmark, Gift } from 'lucide-react'
import { useAuth } from '../../context/AuthContext'

const NAV_ITEMS = [
  { to: '/', label: 'Dashboard', icon: LayoutDashboard, end: true },
  { to: '/festival-setup', label: 'Festival Setup', icon: CalendarDays },
  { to: '/collections', label: 'Collections', icon: HandCoins },
  { to: '/expenses', label: 'Expenses', icon: Receipt },
  { to: '/sponsorships', label: 'Sponsorships', icon: Gift, presidentOnly: true },
  { to: '/auction', label: 'Auction', icon: Gavel },
  { to: '/loans', label: 'Loans', icon: Landmark, presidentOnly: true },
  { to: '/reports', label: 'Reports', icon: FileBarChart },
  { to: '/members', label: 'Committee', icon: Users },
]

export default function Layout({ children, festivalName = 'Ganesh Utsav' }) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <div className="min-h-screen flex flex-col md:flex-row">
      {/* Desktop sidebar */}
      <aside className="hidden md:flex md:flex-col w-64 shrink-0 bg-gradient-to-b from-maroon-700 to-maroon-800 text-white">
        <div className="flex items-center gap-3 px-6 py-6 border-b border-white/10">
          <div className="w-10 h-10 rounded-full bg-saffron-400 flex items-center justify-center text-xl">🐘</div>
          <div>
            <p className="font-display font-bold leading-tight">{festivalName}</p>
            <p className="text-xs text-saffron-200">Expense Tracker</p>
          </div>
        </div>
        <nav className="flex-1 px-3 py-4 space-y-1">
          {NAV_ITEMS.filter((item) => !item.presidentOnly || user?.role === 'PRESIDENT').map(({ to, label, icon: Icon, end }) => (
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
              {label}
            </NavLink>
          ))}
        </nav>
        <div className="px-3 py-4 border-t border-white/10">
          <div className="px-3 mb-2 text-sm text-saffron-200">
            {user?.name} <span className="text-saffron-300">· {user?.role}</span>
          </div>
          <button
            onClick={handleLogout}
            className="flex items-center gap-3 px-3 py-2.5 rounded-xl w-full text-saffron-100 hover:bg-white/10 transition"
          >
            <LogOut size={18} /> Logout
          </button>
        </div>
      </aside>

      <div className="flex-1 flex flex-col min-w-0">
        {/* Top header */}
        <header className="sticky top-0 z-30 bg-white/90 backdrop-blur border-b border-saffron-100 px-4 md:px-8 py-3 flex items-center justify-between">
          <div className="flex items-center gap-2 md:hidden">
            <div className="w-8 h-8 rounded-full bg-saffron-400 flex items-center justify-center text-base">🐘</div>
            <span className="font-display font-bold text-maroon-800">{festivalName}</span>
          </div>
          <div className="hidden md:flex items-center gap-2 text-maroon-600">
            <Sparkles size={18} className="text-gold-500" />
            <span className="font-medium">Ganpati Bappa Morya! Welcome back{user?.name ? `, ${user.name}` : ''}.</span>
          </div>
          <button
            onClick={handleLogout}
            className="md:hidden text-maroon-500"
            aria-label="Logout"
          >
            <LogOut size={20} />
          </button>
        </header>

        {/* Page content */}
        <main className="flex-1 px-4 md:px-8 py-6 pb-24 md:pb-8">{children}</main>
      </div>

      {/* Mobile bottom nav - curated subset to avoid crowding on small screens */}
      <nav className="md:hidden fixed bottom-0 left-0 right-0 z-30 bg-white border-t border-saffron-100 flex justify-around py-2 shadow-[0_-2px_10px_rgba(0,0,0,0.06)]">
        {NAV_ITEMS.filter((item) => !item.presidentOnly || user?.role === 'PRESIDENT')
          .filter((item) => ['/', '/collections', '/expenses', '/auction', '/reports'].includes(item.to))
          .map(({ to, label, icon: Icon, end }) => (
          <NavLink
            key={to}
            to={to}
            end={end}
            className={({ isActive }) =>
              `flex flex-col items-center gap-0.5 px-2 py-1 rounded-lg text-xs font-medium ${
                isActive ? 'text-saffron-600' : 'text-maroon-400'
              }`
            }
          >
            <Icon size={20} />
            {label}
          </NavLink>
        ))}
      </nav>
    </div>
  )
}
