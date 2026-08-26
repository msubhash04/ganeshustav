import React, { useEffect, useState } from 'react'
import { Plus, Pencil, Trash2, Users2 } from 'lucide-react'
import Modal from '../common/Modal'
import { generalSponsorApi, sponsorshipCategoryApi } from '../../api/sponsorshipApi'
import { formatINR } from '../../utils/format'

const emptyForm = { sponsorName: '', contactInfo: '', contributionAmount: '', contributionDetails: '', categoryId: '' }

export default function GeneralSponsorsTab() {
  const [sponsors, setSponsors] = useState([])
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState(emptyForm)
  const [submitting, setSubmitting] = useState(false)

  const load = () => {
    setLoading(true)
    Promise.all([generalSponsorApi.getAll(), sponsorshipCategoryApi.getActive()])
      .then(([s, c]) => { setSponsors(s); setCategories(c) })
      .finally(() => setLoading(false))
  }

  useEffect(load, [])

  // re-fetch the active category list fresh every time the form opens, so it
  // always reflects the latest state of the Sponsorship Categories master page
  const openAdd = () => {
    setEditing(null)
    setForm(emptyForm)
    sponsorshipCategoryApi.getActive().then(setCategories)
    setFormOpen(true)
  }
  const openEdit = (s) => {
    setEditing(s)
    setForm({
      sponsorName: s.sponsorName,
      contactInfo: s.contactInfo || '',
      contributionAmount: s.contributionAmount || '',
      contributionDetails: s.contributionDetails || '',
      categoryId: s.categoryId,
    })
    sponsorshipCategoryApi.getActive().then(setCategories)
    setFormOpen(true)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSubmitting(true)
    try {
      const payload = {
        ...form,
        categoryId: Number(form.categoryId),
        contributionAmount: form.contributionAmount ? Number(form.contributionAmount) : null,
      }
      if (editing) {
        await generalSponsorApi.update(editing.id, payload)
      } else {
        await generalSponsorApi.create(payload)
      }
      setFormOpen(false)
      load()
    } catch (err) {
      alert(err?.response?.data ? JSON.stringify(err.response.data) : 'Failed to save sponsor')
    } finally {
      setSubmitting(false)
    }
  }

  const handleDelete = async (s) => {
    if (!confirm(`Remove sponsor "${s.sponsorName}"?`)) return
    await generalSponsorApi.remove(s.id)
    load()
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <p className="text-sm text-maroon-500">General sponsors, each assigned to a sponsorship category.</p>
        <button onClick={openAdd} className="btn-primary inline-flex items-center gap-2 text-sm py-2 px-3 shrink-0 ml-3">
          <Plus size={16} /> Add Sponsor
        </button>
      </div>

      {categories.length === 0 && !loading && (
        <div className="bg-saffron-50 border border-saffron-200 text-maroon-600 text-sm rounded-xl px-4 py-3 mb-4">
          No active sponsorship categories yet — set one up in the Categories tab first.
        </div>
      )}

      {loading ? (
        <p className="text-maroon-400">Loading…</p>
      ) : sponsors.length === 0 ? (
        <div className="text-center py-10">
          <Users2 className="mx-auto text-saffron-400 mb-2" size={28} />
          <p className="text-maroon-500 text-sm">No general sponsors recorded yet.</p>
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-maroon-500 border-b border-saffron-100">
                <th className="py-2 pr-4">Sponsor</th>
                <th className="py-2 pr-4">Category</th>
                <th className="py-2 pr-4">Contact</th>
                <th className="py-2 pr-4">Details</th>
                <th className="py-2 pr-4 text-right">Amount</th>
                <th className="py-2 pr-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody>
              {sponsors.map((s) => (
                <tr key={s.id} className="border-b border-saffron-50 last:border-0">
                  <td className="py-2.5 pr-4 font-medium text-maroon-800">{s.sponsorName}</td>
                  <td className="py-2.5 pr-4">
                    <span className="badge bg-saffron-100 text-saffron-700">{s.categoryName}</span>
                  </td>
                  <td className="py-2.5 pr-4 text-maroon-500">{s.contactInfo || '—'}</td>
                  <td className="py-2.5 pr-4 text-maroon-500 max-w-[220px] truncate" title={s.contributionDetails}>
                    {s.contributionDetails || '—'}
                  </td>
                  <td className="py-2.5 pr-4 text-right font-semibold text-saffron-600">
                    {s.contributionAmount ? formatINR(s.contributionAmount) : '—'}
                  </td>
                  <td className="py-2.5 pr-4">
                    <div className="flex justify-end gap-2">
                      <button onClick={() => openEdit(s)} className="text-maroon-400 hover:text-saffron-600">
                        <Pencil size={16} />
                      </button>
                      <button onClick={() => handleDelete(s)} className="text-maroon-400 hover:text-maroon-700">
                        <Trash2 size={16} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <Modal open={formOpen} onClose={() => setFormOpen(false)} title={editing ? 'Edit Sponsor' : 'Add General Sponsor'}>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="label-text">Sponsor Name *</label>
            <input className="input-field" value={form.sponsorName} onChange={(e) => setForm({ ...form, sponsorName: e.target.value })} required />
          </div>
          <div>
            <label className="label-text">Sponsorship Category *</label>
            <select className="input-field" value={form.categoryId} onChange={(e) => setForm({ ...form, categoryId: e.target.value })} required>
              <option value="">Select a category…</option>
              {categories.map((c) => (
                <option key={c.id} value={c.id}>{c.name}</option>
              ))}
            </select>
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="label-text">Contact Info</label>
              <input className="input-field" value={form.contactInfo} onChange={(e) => setForm({ ...form, contactInfo: e.target.value })} placeholder="Phone or email" />
            </div>
            <div>
              <label className="label-text">Contribution Amount (₹)</label>
              <input type="number" min="0" className="input-field" value={form.contributionAmount}
                     onChange={(e) => setForm({ ...form, contributionAmount: e.target.value })} placeholder="Optional" />
            </div>
          </div>
          <div>
            <label className="label-text">Contribution Details</label>
            <textarea className="input-field" rows={2} value={form.contributionDetails}
                      onChange={(e) => setForm({ ...form, contributionDetails: e.target.value })}
                      placeholder="e.g. Sponsored idol decoration flowers for all 5 days" />
          </div>
          <div className="flex gap-3 pt-2">
            <button type="submit" className="btn-primary flex-1" disabled={submitting}>
              {submitting ? 'Saving…' : editing ? 'Update Sponsor' : 'Add Sponsor'}
            </button>
            <button type="button" className="btn-secondary" onClick={() => setFormOpen(false)}>Cancel</button>
          </div>
        </form>
      </Modal>
    </div>
  )
}
