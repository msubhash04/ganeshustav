import React from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import { LayoutDashboard, Building2, ScrollText, LogOut, Menu, X } from 'lucide-react'
import { useAuth } from '../../context/AuthContext'
import MobileNavDrawer from './MobileNavDrawer'

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
    <div className="h-screen overflow-hidden flex flex-col md:flex-row">
      {/* Desktop sidebar - locked to full viewport height; header and
          logout footer stay pinned, only the nav list scrolls if it ever
          grows taller than the screen */}
      <aside className="hidden md:flex md:flex-col w-64 shrink-0 h-full overflow-hidden bg-gradient-to-b from-maroon-900 to-black text-white">
        <div className="shrink-0 flex items-center gap-3 px-6 py-6 border-b border-white/10">
          <div className="w-10 h-10 rounded-full bg-gold-500 flex items-center justify-center text-xl">🐘</div>
          <div>
            <p className="font-display font-bold leading-tight">Ganesh Utsav</p>
            <p className="text-xs text-gold-400 tracking-wide">SUPER ADMIN</p>
          </div>
        </div>
        <nav className="flex-1 overflow-y-auto px-3 py-4 space-y-1">
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
        <div className="shrink-0 px-3 py-4 border-t border-white/10">
          <div className="px-3 mb-2 text-sm text-saffron-200 truncate">{user?.name}</div>
          <button
            onClick={handleLogout}
            className="flex items-center gap-3 px-3 py-2.5 rounded-xl w-full text-saffron-100 hover:bg-white/10 transition"
          >
            <LogOut size={18} /> Logout
          </button>
        </div>
      </aside>

      <div className="flex-1 flex flex-col min-w-0 h-full overflow-hidden">
        {/* Mobile top bar - pinned, never scrolls */}
        <header className="md:hidden shrink-0 z-30 bg-gradient-to-r from-maroon-900 to-black text-white px-4 py-3 flex items-center justify-between">
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

        {/* Mobile nav drawer - rendered as a fixed overlay (see
            MobileNavDrawer) so opening it never pushes page content down */}
        <MobileNavDrawer
          open={mobileOpen}
          onClose={() => setMobileOpen(false)}
          panelClassName="bg-gradient-to-b from-maroon-900 to-black text-white"
        >
          <div className="flex items-center justify-between px-4 py-4 border-b border-white/10">
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 rounded-full bg-gold-500 flex items-center justify-center text-base">🐘</div>
              <div>
                <p className="font-display font-bold leading-tight text-sm">Ganesh Utsav</p>
                <p className="text-[10px] text-gold-400 tracking-wide">SUPER ADMIN</p>
              </div>
            </div>
            <button onClick={() => setMobileOpen(false)} className="text-saffron-100" aria-label="Close menu">
              <X size={20} />
            </button>
          </div>
          <nav className="px-3 py-3 space-y-1">
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
            <div className="border-t border-white/10 my-2" />
            <button
              onClick={handleLogout}
              className="flex items-center gap-3 px-3 py-2.5 rounded-xl w-full text-saffron-100 hover:bg-white/10 transition"
            >
              <LogOut size={18} /> Logout
            </button>
          </nav>
        </MobileNavDrawer>

        {/* Page content - the ONLY thing that scrolls in the main area */}
        <main className="flex-1 overflow-y-auto px-4 md:px-8 py-6">{children}</main>
      </div>
    </div>
  )
}
