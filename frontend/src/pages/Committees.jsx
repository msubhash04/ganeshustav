import React, { useEffect, useState } from 'react'
import { Plus, Search, RefreshCw, Lock, Unlock, Eye } from 'lucide-react'
import DeveloperLayout from '../components/layout/DeveloperLayout'
import Modal from '../components/common/Modal'
import InspectCommitteeModal from '../components/common/InspectCommitteeModal'
import { committeeApi } from '../api/committeeApi'
import { formatDate } from '../utils/format'

const emptyForm = {
  name: '', city: '', state: '', address: '',
  presidentName: '', presidentPhone: '', presidentUsername: '', presidentPassword: '',
}

export default function Committees() {
  const [committees, setCommittees] = useState([])
  const [loading, setLoading] = useState(true)
  const [filters, setFilters] = useState({ query: '', city: '', state: '' })

  const [formOpen, setFormOpen] = useState(false)
  const [form, setForm] = useState(emptyForm)
  const [submitting, setSubmitting] = useState(false)
  const [newCodeInfo, setNewCodeInfo] = useState(null)
  const [inspectTarget, setInspectTarget] = useState(null)

  const load = (appliedFilters) => {
    setLoading(true)
    const params = {}
    Object.entries(appliedFilters || {}).forEach(([k, v]) => { if (v) params[k] = v })
    committeeApi.search(params).then(setCommittees).finally(() => setLoading(false))
  }

  useEffect(() => load(filters), []) // eslint-disable-line

  const handleFilterSubmit = (e) => { e.preventDefault(); load(filters) }

  const handleCreate = async (e) => {
    e.preventDefault()
    setSubmitting(true)
    try {
      const created = await committeeApi.create(form)
      setFormOpen(false)
      setForm(emptyForm)
      setNewCodeInfo(created)
      load(filters)
    } catch (err) {
      alert(err?.response?.data ? JSON.stringify(err.response.data) : 'Failed to create committee')
    } finally {
      setSubmitting(false)
    }
  }

  const handleRegenerateCode = async (c) => {
    if (!confirm(`Regenerate the Ganesh Unique Code for "${c.name}"? The old code will stop working immediately.`)) return
    const updated = await committeeApi.regenerateCode(c.id)
    setNewCodeInfo(updated)
    load(filters)
  }

  const handleToggleLock = async (c) => {
    if (c.active) {
      if (!confirm(`Lock "${c.name}"? Its committee members will be signed out immediately and won't be able to log back in until unlocked.`)) return
      await committeeApi.lock(c.id)
    } else {
      await committeeApi.unlock(c.id)
    }
    load(filters)
  }

  return (
    <DeveloperLayout>
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-6">
        <div>
          <h1 className="page-title">All Ganesh Committees</h1>
          <p className="text-sm text-maroon-400 mt-0.5">{committees.length} committee{committees.length === 1 ? '' : 's'} on the platform</p>
        </div>
        <button onClick={() => setFormOpen(true)} className="btn-primary inline-flex items-center gap-2 w-fit">
          <Plus size={18} /> Register New Committee
        </button>
      </div>

      <form onSubmit={handleFilterSubmit} className="card mb-6 grid grid-cols-1 sm:grid-cols-4 gap-3 items-end">
        <div>
          <label className="label-text">Search (name or code)</label>
          <input className="input-field" value={filters.query} onChange={(e) => setFilters({ ...filters, query: e.target.value })} />
        </div>
        <div>
          <label className="label-text">City</label>
          <input className="input-field" value={filters.city} onChange={(e) => setFilters({ ...filters, city: e.target.value })} />
        </div>
        <div>
          <label className="label-text">State</label>
          <input className="input-field" value={filters.state} onChange={(e) => setFilters({ ...filters, state: e.target.value })} />
        </div>
        <button type="submit" className="btn-secondary inline-flex items-center justify-center gap-2">
          <Search size={16} /> Filter
        </button>
      </form>

      <div className="card">
        {loading ? (
          <p className="text-maroon-400">Loading…</p>
        ) : committees.length === 0 ? (
          <p className="text-sm text-maroon-400 py-8 text-center">No committees registered yet.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-maroon-500 border-b border-saffron-100">
                  <th className="py-2 pr-4">Committee</th>
                  <th className="py-2 pr-4">Code</th>
                  <th className="py-2 pr-4">City / State</th>
                  <th className="py-2 pr-4">Members</th>
                  <th className="py-2 pr-4">Registered</th>
                  <th className="py-2 pr-4">Status</th>
                  <th className="py-2 pr-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody>
                {committees.map((c) => (
                  <tr key={c.id} className="border-b border-saffron-50 last:border-0 hover:bg-saffron-50/40 transition">
                    <td className="py-2.5 pr-4 font-medium text-maroon-800">{c.name}</td>
                    <td className="py-2.5 pr-4 text-maroon-500 font-mono text-xs">{c.tenantCode}</td>
                    <td className="py-2.5 pr-4 text-maroon-500">{[c.city, c.state].filter(Boolean).join(', ') || '—'}</td>
                    <td className="py-2.5 pr-4 text-maroon-500">{c.memberCount}</td>
                    <td className="py-2.5 pr-4 text-maroon-500">{formatDate(c.createdAt)}</td>
                    <td className="py-2.5 pr-4">
                      <span className={`badge ${c.active ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'}`}>
                        {c.active ? 'Active' : 'Locked'}
                      </span>
                    </td>
                    <td className="py-2.5 pr-4">
                      <div className="flex justify-end items-center gap-2">
                        <button
                          onClick={() => setInspectTarget(c)}
                          disabled={!c.active}
                          title={c.active ? 'Inspect committee' : 'Unlock this committee to inspect it'}
                          className="inline-flex items-center gap-1.5 px-3 py-1.5 rounded-lg bg-saffron-50 text-saffron-700 hover:bg-saffron-100 font-medium text-xs transition disabled:opacity-40 disabled:cursor-not-allowed"
                        >
                          <Eye size={14} /> Inspect
                        </button>
                        <button onClick={() => handleRegenerateCode(c)} className="p-1.5 rounded-lg text-maroon-400 hover:text-saffron-600 hover:bg-saffron-50" title="Regenerate code">
                          <RefreshCw size={16} />
                        </button>
                        <button onClick={() => handleToggleLock(c)} className="p-1.5 rounded-lg text-maroon-400 hover:text-maroon-700 hover:bg-saffron-50"
                                title={c.active ? 'Lock committee' : 'Unlock committee'}>
                          {c.active ? <Lock size={16} /> : <Unlock size={16} />}
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Create committee modal */}
      <Modal open={formOpen} onClose={() => setFormOpen(false)} title="Register New Ganesh Committee" maxWidth="max-w-xl">
        <form onSubmit={handleCreate} className="space-y-4">
          <p className="text-xs text-maroon-500">A Ganesh Unique Code will be generated automatically, and the President account below will be created together with the committee.</p>
          <div>
            <label className="label-text">Committee Name *</label>
            <input className="input-field" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })}
                   placeholder="e.g. Shivaji Nagar Ganesh Mandal" required />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="label-text">City *</label>
              <input className="input-field" value={form.city} onChange={(e) => setForm({ ...form, city: e.target.value })} required />
            </div>
            <div>
              <label className="label-text">State *</label>
              <input className="input-field" value={form.state} onChange={(e) => setForm({ ...form, state: e.target.value })} required />
            </div>
          </div>
          <div>
            <label className="label-text">Address (optional)</label>
            <input className="input-field" value={form.address} onChange={(e) => setForm({ ...form, address: e.target.value })} />
          </div>
          <hr className="border-saffron-100" />
          <p className="text-sm font-semibold text-maroon-700">Initial President Account</p>
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="label-text">President Name *</label>
              <input className="input-field" value={form.presidentName} onChange={(e) => setForm({ ...form, presidentName: e.target.value })} required />
            </div>
            <div>
              <label className="label-text">President Phone *</label>
              <input className="input-field" value={form.presidentPhone} onChange={(e) => setForm({ ...form, presidentPhone: e.target.value })} required />
            </div>
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="label-text">Login Username *</label>
              <input className="input-field" value={form.presidentUsername} onChange={(e) => setForm({ ...form, presidentUsername: e.target.value })} required />
            </div>
            <div>
              <label className="label-text">Login Password *</label>
              <input type="password" className="input-field" value={form.presidentPassword} onChange={(e) => setForm({ ...form, presidentPassword: e.target.value })} required />
            </div>
          </div>
          <div className="flex gap-3 pt-2">
            <button type="submit" className="btn-primary flex-1" disabled={submitting}>
              {submitting ? 'Creating…' : 'Register Committee'}
            </button>
            <button type="button" className="btn-secondary" onClick={() => setFormOpen(false)}>Cancel</button>
          </div>
        </form>
      </Modal>

      {/* Show the (re)generated Ganesh Unique Code once, since it's the only time it's surfaced this prominently */}
      <Modal open={!!newCodeInfo} onClose={() => setNewCodeInfo(null)} title="Ganesh Unique Code">
        {newCodeInfo && (
          <div className="space-y-3">
            <p className="text-sm text-maroon-600">Share this code with <strong>{newCodeInfo.name}</strong>'s President so they can identify their committee. It's also shown publicly on their donor transparency page, so it isn't a login credential — staff accounts are added by the President from the Committee Members page, not by self-registering with this code.</p>
            <div className="bg-saffron-50 border border-saffron-200 rounded-xl px-4 py-3 text-center">
              <p className="text-2xl font-bold font-mono text-maroon-800">{newCodeInfo.tenantCode}</p>
            </div>
            <button onClick={() => setNewCodeInfo(null)} className="btn-primary w-full">Done</button>
          </div>
        )}
      </Modal>

      <InspectCommitteeModal committee={inspectTarget} onClose={() => setInspectTarget(null)} />
    </DeveloperLayout>
  )
}
