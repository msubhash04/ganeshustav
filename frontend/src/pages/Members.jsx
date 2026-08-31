import React, { useEffect, useState } from 'react'
import { Plus, UserX, Trash2 } from 'lucide-react'
import Layout from '../components/layout/Layout'
import Modal from '../components/common/Modal'
import ResponsiveTable, { TableCard, CardActions, CardActionButton } from '../components/common/ResponsiveTable'
import { memberApi } from '../api/reportApi'
import { useAuth } from '../context/AuthContext'

const ROLES = ['PRESIDENT', 'TREASURER', 'SECRETARY', 'VOLUNTEER']
const ROLE_STYLES = {
  PRESIDENT: 'bg-maroon-100 text-maroon-700',
  TREASURER: 'bg-saffron-100 text-saffron-700',
  SECRETARY: 'bg-gold-500/10 text-gold-600',
  VOLUNTEER: 'bg-blue-100 text-blue-700',
}

const emptyForm = { name: '', phone: '', email: '', username: '', password: '', role: 'VOLUNTEER' }

export default function Members() {
  const { user } = useAuth()
  // Staff management stays off-limits during Tenant Inspection, in both
  // modes (see InspectionModeFilter) - a Developer inspecting can
  // observe the roster but not add/deactivate/remove anyone on it.
  const isInspecting = !!user?.isInspecting

  const [members, setMembers] = useState([])
  const [loading, setLoading] = useState(true)
  const [formOpen, setFormOpen] = useState(false)
  const [form, setForm] = useState(emptyForm)
  const [submitting, setSubmitting] = useState(false)

  const load = () => {
    setLoading(true)
    memberApi.getAll().then(setMembers).finally(() => setLoading(false))
  }

  useEffect(load, [])

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSubmitting(true)
    try {
      await memberApi.create(form)
      setFormOpen(false)
      setForm(emptyForm)
      load()
    } catch (err) {
      alert(err?.response?.data?.error || err?.response?.data || 'Failed to add member')
    } finally {
      setSubmitting(false)
    }
  }

  const handleDeactivate = async (m) => {
    if (!confirm(`Deactivate ${m.name}'s access?`)) return
    await memberApi.deactivate(m.id)
    load()
  }

  const handleDelete = async (m) => {
    if (!confirm(`Permanently remove ${m.name} from the committee list?`)) return
    await memberApi.remove(m.id)
    load()
  }

  return (
    <Layout>
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-6">
        <h1 className="page-title">Committee Members</h1>
        {!isInspecting && (
          <button onClick={() => setFormOpen(true)} className="btn-primary inline-flex items-center gap-2 w-fit">
            <Plus size={18} /> Add Member
          </button>
        )}
      </div>

      {isInspecting && (
        <div className="card bg-saffron-50 border-saffron-200 text-maroon-600 mb-6 text-sm">
          You're viewing this committee's staff roster during Tenant Inspection. Staff management stays off-limits from here, in both Read-Only and Admin Override mode.
        </div>
      )}

      <div className="card">
        {loading ? (
          <p className="text-maroon-400">Loading…</p>
        ) : (
          <ResponsiveTable
            data={members}
            emptyMessage="No committee members yet."
            renderCard={(m) => (
              <TableCard>
                <div className="flex items-start justify-between gap-3">
                  <div className="min-w-0">
                    <p className="font-semibold text-maroon-800 truncate">{m.name}</p>
                    <p className="text-xs text-maroon-400">{m.phone} · @{m.username}</p>
                  </div>
                  <span className={`badge shrink-0 ${ROLE_STYLES[m.role]}`}>{m.role}</span>
                </div>
                <div className="flex flex-wrap items-center gap-2 mt-2.5">
                  <span className={`badge ${m.active ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'}`}>
                    {m.active ? 'Active' : 'Inactive'}
                  </span>
                </div>
                {!isInspecting && (
                  <CardActions>
                    <CardActionButton onClick={() => handleDeactivate(m)} icon={UserX} label="Deactivate" />
                    <CardActionButton onClick={() => handleDelete(m)} icon={Trash2} label="Remove" tone="danger" />
                  </CardActions>
                )}
              </TableCard>
            )}
          >
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-maroon-500 border-b border-saffron-100">
                  <th className="py-2 pr-4">Name</th>
                  <th className="py-2 pr-4">Role</th>
                  <th className="py-2 pr-4">Phone</th>
                  <th className="py-2 pr-4">Username</th>
                  <th className="py-2 pr-4">Status</th>
                  {!isInspecting && <th className="py-2 pr-4 text-right">Actions</th>}
                </tr>
              </thead>
              <tbody>
                {members.map((m) => (
                  <tr key={m.id} className="border-b border-saffron-50 last:border-0">
                    <td className="py-2.5 pr-4 font-medium text-maroon-800">{m.name}</td>
                    <td className="py-2.5 pr-4"><span className={`badge ${ROLE_STYLES[m.role]}`}>{m.role}</span></td>
                    <td className="py-2.5 pr-4 text-maroon-500">{m.phone}</td>
                    <td className="py-2.5 pr-4 text-maroon-500">{m.username}</td>
                    <td className="py-2.5 pr-4">
                      <span className={`badge ${m.active ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'}`}>
                        {m.active ? 'Active' : 'Inactive'}
                      </span>
                    </td>
                    {!isInspecting && (
                      <td className="py-2.5 pr-4">
                        <div className="flex justify-end gap-2">
                          <button onClick={() => handleDeactivate(m)} className="text-maroon-400 hover:text-saffron-600" title="Deactivate">
                            <UserX size={16} />
                          </button>
                          <button onClick={() => handleDelete(m)} className="text-maroon-400 hover:text-maroon-700" title="Remove">
                            <Trash2 size={16} />
                          </button>
                        </div>
                      </td>
                    )}
                  </tr>
                ))}
              </tbody>
            </table>
          </ResponsiveTable>
        )}
      </div>

      <Modal open={formOpen} onClose={() => setFormOpen(false)} title="Add Committee Member">
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="label-text">Full Name *</label>
            <input className="input-field" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} required />
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="label-text">Phone *</label>
              <input className="input-field" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} required />
            </div>
            <div>
              <label className="label-text">Role *</label>
              <select className="input-field" value={form.role} onChange={(e) => setForm({ ...form, role: e.target.value })}>
                {ROLES.map((r) => <option key={r} value={r}>{r}</option>)}
              </select>
            </div>
          </div>
          <div>
            <label className="label-text">Email (optional)</label>
            <input type="email" className="input-field" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="label-text">Login Username *</label>
              <input className="input-field" value={form.username} onChange={(e) => setForm({ ...form, username: e.target.value })} required />
            </div>
            <div>
              <label className="label-text">Password *</label>
              <input type="password" className="input-field" value={form.password} onChange={(e) => setForm({ ...form, password: e.target.value })} required />
            </div>
          </div>
          <div className="flex gap-3 pt-2">
            <button type="submit" className="btn-primary flex-1" disabled={submitting}>
              {submitting ? 'Saving…' : 'Add Member'}
            </button>
            <button type="button" className="btn-secondary" onClick={() => setFormOpen(false)}>Cancel</button>
          </div>
        </form>
      </Modal>
    </Layout>
  )
}
