import React from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { LayoutDashboard, Building2, ScrollText, LogOut, Menu } from 'lucide-react'
import { useAuth } from '../../context/AuthContext'

const NAV_ITEMS = [
  { to: '/', label: 'Overview', icon: LayoutDashboard, end: true },
  { to: '/committees', label: 'Committees', icon: Building2 },
  { to: '/inspection-log', label: 'Inspection Log', icon: ScrollText },
]

export default function DeveloperLayout({ children }) {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [mobileOpen, setMobileOpen] = React.useState(false)

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <div className="min-h-screen flex flex-col md:flex-row">
      {/* Desktop sidebar */}
      <aside className="hidden md:flex md:flex-col w-64 shrink-0 bg-gradient-to-b from-maroon-900 to-black text-white">
        <div className="flex items-center gap-3 px-6 py-6 border-b border-white/10">
          <div className="w-10 h-10 rounded-full bg-gold-500 flex items-center justify-center text-xl">🐘</div>
          <div>
            <p className="font-display font-bold leading-tight">Ganesh Utsav</p>
            <p className="text-xs text-gold-400 tracking-wide">SUPER ADMIN</p>
          </div>
        </div>
        <nav className="flex-1 px-3 py-4 space-y-1">
          {NAV_ITEMS.map(({ to, label, icon: Icon, end }) => (
            <NavLink
              key={to}
              to={to}
              end={end}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3 py-2.5 rounded-xl transition font-medium ${
                  isActive ? 'bg-gold-500 text-maroon-900' : 'text-saffron-100 hover:bg-white/10'
                }`
              }
            >
              <Icon size={18} />
              {label}
            </NavLink>
          ))}
        </nav>
        <div className="px-3 py-4 border-t border-white/10">
          <div className="px-3 mb-2 text-sm text-saffron-200 truncate">{user?.name}</div>
          <button
            onClick={handleLogout}
            className="flex items-center gap-3 px-3 py-2.5 rounded-xl w-full text-saffron-100 hover:bg-white/10 transition"
          >
            <LogOut size={18} /> Logout
          </button>
        </div>
      </aside>

      <div className="flex-1 flex flex-col min-w-0">
        {/* Mobile top bar */}
        <header className="md:hidden sticky top-0 z-30 bg-gradient-to-r from-maroon-900 to-black text-white px-4 py-3 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 rounded-full bg-gold-500 flex items-center justify-center text-base">🐘</div>
            <div>
              <p className="font-display font-bold leading-tight text-sm">Ganesh Utsav</p>
              <p className="text-[10px] text-gold-400 tracking-wide">SUPER ADMIN</p>
            </div>
          </div>
          <button onClick={() => setMobileOpen((v) => !v)} aria-label="Menu" className="text-saffron-100">
            <Menu size={22} />
          </button>
        </header>

        {/* Mobile nav drawer */}
        {mobileOpen && (
          <nav className="md:hidden bg-maroon-900 text-white px-3 py-2 space-y-1">
            {NAV_ITEMS.map(({ to, label, icon: Icon, end }) => (
              <NavLink
                key={to}
                to={to}
                end={end}
                onClick={() => setMobileOpen(false)}
                className={({ isActive }) =>
                  `flex items-center gap-3 px-3 py-2.5 rounded-xl transition font-medium ${
                    isActive ? 'bg-gold-500 text-maroon-900' : 'text-saffron-100 hover:bg-white/10'
                  }`
                }
              >
                <Icon size={18} />
                {label}
              </NavLink>
            ))}
            <button
              onClick={handleLogout}
              className="flex items-center gap-3 px-3 py-2.5 rounded-xl w-full text-saffron-100 hover:bg-white/10 transition"
            >
              <LogOut size={18} /> Logout
            </button>
          </nav>
        )}

        <main className="flex-1 px-4 md:px-8 py-6">{children}</main>
      </div>
    </div>
  )
}
