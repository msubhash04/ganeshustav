import React, { useEffect, useState } from 'react'
import { Building2, Sparkles, Wallet, Landmark, LogOut } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { developerDashboardApi } from '../api/committeeApi'
import { formatINR } from '../utils/format'

export default function DeveloperDashboard() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    developerDashboardApi
      .getOverview()
      .then(setData)
      .catch(() => setError('Could not load the global overview. Is the backend running?'))
      .finally(() => setLoading(false))
  }, [])

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  return (
    <div className="min-h-screen bg-cream">
      <header className="bg-gradient-to-r from-maroon-800 to-maroon-900 text-white px-4 md:px-8 py-4 flex items-center justify-between">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-full bg-gold-500 flex items-center justify-center text-xl">🐘</div>
          <div>
            <p className="font-display font-bold leading-tight">Ganesh Utsav Platform</p>
            <p className="text-xs text-saffron-200">Developer Super Admin</p>
          </div>
        </div>
        <div className="flex items-center gap-4">
          <span className="text-sm text-saffron-100 hidden sm:inline">{user?.name}</span>
          <button onClick={handleLogout} className="text-saffron-100 hover:text-white" aria-label="Logout">
            <LogOut size={20} />
          </button>
        </div>
      </header>

      <main className="max-w-6xl mx-auto px-4 md:px-8 py-6">
        <div className="flex items-center justify-between mb-6">
          <h1 className="page-title">Global Overview</h1>
          <button onClick={() => navigate('/committees')} className="btn-primary inline-flex items-center gap-2">
            <Building2 size={18} /> Manage Committees
          </button>
        </div>

        {error && <div className="card bg-maroon-50 border-maroon-200 text-maroon-700 mb-6">{error}</div>}

        {loading ? (
          <p className="text-maroon-400">Loading…</p>
        ) : data ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            <Widget title="Total Registered Committees" value={data.totalRegisteredCommittees} icon={Building2} tone="saffron" />
            <Widget title="Active Committees" value={data.activeCommittees} icon={Sparkles} tone="gold" />
            <Widget title="Active Utsavs (This Year)" value={data.activeUtsavsThisYear} icon={Sparkles} tone="maroon" />
            <Widget title="Total Collections (All Committees)" value={formatINR(data.totalCollectionsAllCommittees)} icon={Wallet} tone="saffron" />
            <Widget title="Total Expenses (All Committees)" value={formatINR(data.totalExpensesAllCommittees)} icon={Wallet} tone="maroon" />
            <Widget title="Total Lent Money (All Committees)" value={formatINR(data.totalLentMoneyAllCommittees)} icon={Landmark} tone="gold" />
          </div>
        ) : null}

        <p className="text-xs text-maroon-400 mt-6">
          These figures span every registered committee on the platform. Individual committee financials remain isolated from each other —
          Presidents and staff can only ever see their own committee's data.
        </p>
      </main>
    </div>
  )
}

function Widget({ title, value, icon: Icon, tone }) {
  const tones = {
    saffron: 'from-saffron-500 to-saffron-600',
    maroon: 'from-maroon-600 to-maroon-700',
    gold: 'from-gold-500 to-gold-600',
  }
  return (
    <div className="card flex items-center gap-4">
      <div className={`w-12 h-12 rounded-xl bg-gradient-to-br ${tones[tone]} flex items-center justify-center text-white shrink-0`}>
        <Icon size={22} />
      </div>
      <div className="min-w-0">
        <p className="text-sm text-maroon-500 font-medium truncate">{title}</p>
        <p className="text-xl md:text-2xl font-bold text-maroon-800 truncate">{value}</p>
      </div>
    </div>
  )
}
