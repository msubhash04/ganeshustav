import React, { useEffect, useState } from 'react'
import { Building2, Sparkles, Wallet, Landmark, Search, Eye, Lock, Unlock } from 'lucide-react'
import { Link } from 'react-router-dom'
import DeveloperLayout from '../components/layout/DeveloperLayout'
import InspectCommitteeModal from '../components/common/InspectCommitteeModal'
import { developerDashboardApi, committeeApi } from '../api/committeeApi'
import { formatINR } from '../utils/format'

export default function DeveloperDashboard() {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  const [committees, setCommittees] = useState([])
  const [committeesLoading, setCommitteesLoading] = useState(true)
  const [query, setQuery] = useState('')
  const [inspectTarget, setInspectTarget] = useState(null)

  useEffect(() => {
    developerDashboardApi
      .getOverview()
      .then(setData)
      .catch(() => setError('Could not load the global overview. Is the backend running?'))
      .finally(() => setLoading(false))
  }, [])

  // Debounced-by-nature: re-fetches on every keystroke, but the payload
  // is small (committee directory, not financial detail), so it stays
  // snappy without needing real debouncing.
  useEffect(() => {
    setCommitteesLoading(true)
    const params = query ? { query } : {}
    committeeApi.search(params).then(setCommittees).finally(() => setCommitteesLoading(false))
  }, [query])

  const handleToggleLock = async (c) => {
    if (c.active) {
      if (!confirm(`Lock "${c.name}"? Its committee members will be signed out immediately and won't be able to log back in until unlocked.`)) return
      await committeeApi.lock(c.id)
    } else {
      await committeeApi.unlock(c.id)
    }
    committeeApi.search(query ? { query } : {}).then(setCommittees)
  }

  return (
    <DeveloperLayout>
      <h1 className="page-title mb-6">Global Overview</h1>

      {error && <div className="card bg-maroon-50 border-maroon-200 text-maroon-700 mb-6">{error}</div>}

      {loading ? (
        <p className="text-maroon-400">Loading…</p>
      ) : data ? (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 mb-3">
          <Widget title="Total Registered Committees" value={data.totalRegisteredCommittees} icon={Building2} tone="saffron" />
          <Widget title="Active Committees" value={data.activeCommittees} icon={Sparkles} tone="gold" />
          <Widget title="Active Utsavs (This Year)" value={data.activeUtsavsThisYear} icon={Sparkles} tone="maroon" />
          <Widget title="Total Collections (All Committees)" value={formatINR(data.totalCollectionsAllCommittees)} icon={Wallet} tone="saffron" />
          <Widget title="Total Expenses (All Committees)" value={formatINR(data.totalExpensesAllCommittees)} icon={Wallet} tone="maroon" />
          <Widget title="Total Lent Money (All Committees)" value={formatINR(data.totalLentMoneyAllCommittees)} icon={Landmark} tone="gold" />
        </div>
      ) : null}

      <p className="text-xs text-maroon-400 mb-8">
        These figures span every registered committee on the platform. Individual committee financials remain isolated from each other —
        Presidents and staff can only ever see their own committee's data.
      </p>

      {/* Committees at a Glance - the fastest path from "who do I need to
          look at" to actually inspecting them, without a trip to the
          full directory page first. Regenerate/Lock stay one click away
          too, for the common housekeeping actions. */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-2 mb-4">
        <h2 className="text-lg font-display font-bold text-maroon-800">Committees at a Glance</h2>
        <Link to="/committees" className="text-sm font-medium text-saffron-600 hover:text-saffron-700 shrink-0">
          Full directory & registration →
        </Link>
      </div>

      <div className="relative mb-4 max-w-sm">
        <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-maroon-300" />
        <input
          className="input-field pl-9"
          placeholder="Find a committee by name or code…"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
        />
      </div>

      {committeesLoading ? (
        <p className="text-maroon-400">Loading…</p>
      ) : committees.length === 0 ? (
        <div className="card text-sm text-maroon-400 py-8 text-center">
          {query ? `No committees match "${query}".` : 'No committees registered yet.'}
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {committees.map((c) => (
            <div key={c.id} className="card flex flex-col gap-3">
              <div className="flex items-start justify-between gap-2">
                <div className="min-w-0">
                  <p className="font-semibold text-maroon-800 truncate">{c.name}</p>
                  <p className="text-xs text-maroon-400 font-mono">{c.tenantCode}</p>
                </div>
                <span className={`badge shrink-0 ${c.active ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'}`}>
                  {c.active ? 'Active' : 'Locked'}
                </span>
              </div>

              <div className="flex flex-wrap items-center gap-x-3 gap-y-1 text-xs text-maroon-500">
                <span>{[c.city, c.state].filter(Boolean).join(', ') || '—'}</span>
                <span>{c.memberCount} member{c.memberCount === 1 ? '' : 's'}</span>
              </div>

              <div className="flex items-center gap-2 mt-1">
                <button
                  onClick={() => setInspectTarget(c)}
                  disabled={!c.active}
                  title={c.active ? 'Inspect committee' : 'Unlock this committee to inspect it'}
                  className="btn-primary flex-1 inline-flex items-center justify-center gap-1.5 text-sm py-2 disabled:opacity-40 disabled:cursor-not-allowed"
                >
                  <Eye size={15} /> Inspect
                </button>
                <button
                  onClick={() => handleToggleLock(c)}
                  className="p-2 rounded-lg border border-saffron-100 text-maroon-400 hover:text-maroon-700 hover:bg-saffron-50 transition"
                  title={c.active ? 'Lock committee' : 'Unlock committee'}
                >
                  {c.active ? <Lock size={15} /> : <Unlock size={15} />}
                </button>
              </div>
            </div>
          ))}
        </div>
      )}

      <InspectCommitteeModal committee={inspectTarget} onClose={() => setInspectTarget(null)} />
    </DeveloperLayout>
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
