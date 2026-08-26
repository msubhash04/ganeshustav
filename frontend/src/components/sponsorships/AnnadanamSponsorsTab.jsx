import React, { useEffect, useState } from 'react'
import { Plus, Pencil, Trash2, Soup } from 'lucide-react'
import Modal from '../common/Modal'
import { annadanamSponsorApi } from '../../api/sponsorshipApi'
import { festivalYearApi } from '../../api/festivalYearApi'
import { formatINR } from '../../utils/format'

const emptyForm = { sponsorName: '', contactInfo: '', dayNumber: '', mealSlot: '', contributionAmount: '', contributionDetails: '' }

export default function AnnadanamSponsorsTab() {
  const [festivalYear, setFestivalYear] = useState(null)
  const [sponsors, setSponsors] = useState([])
  const [loading, setLoading] = useState(true)
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState(emptyForm)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    festivalYearApi.getActive().then((y) => {
      setFestivalYear(y)
      if (y) loadSponsors(y.id)
      else setLoading(false)
    })
  }, [])

  const loadSponsors = (yearId) => {
    setLoading(true)
    annadanamSponsorApi.getByFestivalYear(yearId).then(setSponsors).finally(() => setLoading(false))
  }

  const openAdd = () => { setEditing(null); setForm(emptyForm); setFormOpen(true) }
  const openEdit = (s) => {
    setEditing(s)
    setForm({
      sponsorName: s.sponsorName,
      contactInfo: s.contactInfo || '',
      dayNumber: s.dayNumber,
      mealSlot: s.mealSlot || '',
      contributionAmount: s.contributionAmount || '',
      contributionDetails: s.contributionDetails || '',
    })
    setFormOpen(true)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!festivalYear) return
    setSubmitting(true)
    try {
      const payload = {
        ...form,
        dayNumber: Number(form.dayNumber),
        contributionAmount: form.contributionAmount ? Number(form.contributionAmount) : null,
        festivalYearId: festivalYear.id,
      }
      if (editing) {
        await annadanamSponsorApi.update(editing.id, payload)
      } else {
        await annadanamSponsorApi.create(payload)
      }
      setFormOpen(false)
      loadSponsors(festivalYear.id)
    } catch (err) {
      alert(err?.response?.data ? JSON.stringify(err.response.data) : 'Failed to save Annadanam sponsor')
    } finally {
      setSubmitting(false)
    }
  }

  const handleDelete = async (s) => {
    if (!confirm(`Remove Annadanam sponsor "${s.sponsorName}"?`)) return
    await annadanamSponsorApi.remove(s.id)
    loadSponsors(festivalYear.id)
  }

  if (!loading && !festivalYear) {
    return (
      <div className="text-center py-10">
        <Soup className="mx-auto text-saffron-400 mb-2" size={28} />
        <p className="text-maroon-500 text-sm">No active festival year set up yet. Create one under Festival Setup first.</p>
      </div>
    )
  }

  return (
    <div>
      <div className="flex items-center justify-between mb-4">
        <p className="text-sm text-maroon-500">
          Annadanam (food distribution) sponsors, tracked separately by festival day{festivalYear ? ` — ${festivalYear.label}` : ''}.
        </p>
        <button onClick={openAdd} className="btn-primary inline-flex items-center gap-2 text-sm py-2 px-3 shrink-0 ml-3">
          <Plus size={16} /> Add Sponsor
        </button>
      </div>

      {loading ? (
        <p className="text-maroon-400">Loading…</p>
      ) : sponsors.length === 0 ? (
        <div className="text-center py-10">
          <Soup className="mx-auto text-saffron-400 mb-2" size={28} />
          <p className="text-maroon-500 text-sm">No Annadanam sponsors recorded yet.</p>
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="text-left text-maroon-500 border-b border-saffron-100">
                <th className="py-2 pr-4">Day</th>
                <th className="py-2 pr-4">Sponsor</th>
                <th className="py-2 pr-4">Meal</th>
                <th className="py-2 pr-4">Contact</th>
                <th className="py-2 pr-4 text-right">Amount</th>
                <th className="py-2 pr-4 text-right">Actions</th>
              </tr>
            </thead>
            <tbody>
              {sponsors.map((s) => (
                <tr key={s.id} className="border-b border-saffron-50 last:border-0">
                  <td className="py-2.5 pr-4">
                    <span className="badge bg-gold-500/10 text-gold-600">Day {s.dayNumber}</span>
                  </td>
                  <td className="py-2.5 pr-4 font-medium text-maroon-800">{s.sponsorName}</td>
                  <td className="py-2.5 pr-4 text-maroon-500">{s.mealSlot || '—'}</td>
                  <td className="py-2.5 pr-4 text-maroon-500">{s.contactInfo || '—'}</td>
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

      <Modal open={formOpen} onClose={() => setFormOpen(false)} title={editing ? 'Edit Annadanam Sponsor' : 'Add Annadanam Sponsor'}>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="label-text">Sponsor Name *</label>
            <input className="input-field" value={form.sponsorName} onChange={(e) => setForm({ ...form, sponsorName: e.target.value })} required />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="label-text">Festival Day *</label>
              <input type="number" min="1" max={festivalYear?.durationDays} className="input-field" value={form.dayNumber}
                     onChange={(e) => setForm({ ...form, dayNumber: e.target.value })} placeholder="e.g. 3" required />
            </div>
            <div>
              <label className="label-text">Meal Slot</label>
              <input className="input-field" value={form.mealSlot} onChange={(e) => setForm({ ...form, mealSlot: e.target.value })}
                     placeholder="e.g. Lunch, Dinner, All Day" />
            </div>
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
                      placeholder="e.g. Sponsoring lunch for ~500 people" />
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
