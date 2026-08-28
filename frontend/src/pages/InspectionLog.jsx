import React, { useEffect, useState } from 'react'
import { ScrollText, Eye, ShieldAlert } from 'lucide-react'
import DeveloperLayout from '../components/layout/DeveloperLayout'
import { inspectionApi } from '../api/committeeApi'

function formatDateTime(value) {
  if (!value) return ''
  const d = new Date(value)
  return d.toLocaleString('en-IN', { day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit' })
}

const EVENT_LABELS = {
  SESSION_START: 'Started inspecting',
  SESSION_END: 'Exited inspection',
  ACTION: 'Made a change',
}

export default function InspectionLog() {
  const [entries, setEntries] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [query, setQuery] = useState('')

  useEffect(() => {
    inspectionApi.history()
      .then(setEntries)
      .catch(() => setError('Could not load the inspection log. Is the backend running?'))
      .finally(() => setLoading(false))
  }, [])

  const filtered = entries.filter((e) => {
    if (!query) return true
    const q = query.toLowerCase()
    return (e.tenantCode || '').toLowerCase().includes(q) || (e.developerUsername || '').toLowerCase().includes(q)
  })

  return (
    <DeveloperLayout>
      <div className="mb-6">
        <h1 className="page-title flex items-center gap-2">
          <ScrollText size={22} className="text-maroon-500" /> Inspection Log
        </h1>
        <p className="text-sm text-maroon-400 mt-0.5">
          Every Tenant Inspection session, and every change made under Admin Override, in one accountable trail.
        </p>
      </div>

      <input
        className="input-field mb-4 max-w-sm"
        placeholder="Filter by committee code or developer…"
        value={query}
        onChange={(e) => setQuery(e.target.value)}
      />

      {error && <div className="card bg-maroon-50 border-maroon-200 text-maroon-700 mb-6">{error}</div>}

      <div className="card">
        {loading ? (
          <p className="text-maroon-400">Loading…</p>
        ) : filtered.length === 0 ? (
          <p className="text-sm text-maroon-400 py-8 text-center">No inspection activity recorded yet.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-maroon-500 border-b border-saffron-100">
                  <th className="py-2 pr-4">When</th>
                  <th className="py-2 pr-4">Developer</th>
                  <th className="py-2 pr-4">Committee</th>
                  <th className="py-2 pr-4">Mode</th>
                  <th className="py-2 pr-4">Event</th>
                  <th className="py-2 pr-4">Request</th>
                </tr>
              </thead>
              <tbody>
                {filtered.map((e) => (
                  <tr key={e.id} className="border-b border-saffron-50 last:border-0">
                    <td className="py-2.5 pr-4 text-maroon-500 whitespace-nowrap">{formatDateTime(e.occurredAt)}</td>
                    <td className="py-2.5 pr-4 text-maroon-800 font-medium">{e.developerUsername}</td>
                    <td className="py-2.5 pr-4 text-maroon-500 font-mono text-xs">{e.tenantCode}</td>
                    <td className="py-2.5 pr-4">
                      <span className={`badge ${e.mode === 'ADMIN_OVERRIDE' ? 'bg-amber-100 text-amber-700' : 'bg-maroon-50 text-maroon-600'}`}>
                        {e.mode === 'ADMIN_OVERRIDE'
                          ? <span className="inline-flex items-center gap-1"><ShieldAlert size={12} /> Override</span>
                          : <span className="inline-flex items-center gap-1"><Eye size={12} /> Read-Only</span>}
                      </span>
                    </td>
                    <td className="py-2.5 pr-4 text-maroon-600">{EVENT_LABELS[e.eventType] || e.eventType}</td>
                    <td className="py-2.5 pr-4 text-maroon-400 font-mono text-xs">
                      {e.httpMethod ? `${e.httpMethod} ${e.path}` : '—'}
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </DeveloperLayout>
  )
}
