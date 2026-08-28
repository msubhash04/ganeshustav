import React, { useEffect, useState } from 'react'
import { Plus, CalendarDays, CheckCircle2, Pencil } from 'lucide-react'
import Layout from '../components/layout/Layout'
import Modal from '../components/common/Modal'
import { festivalYearApi } from '../api/festivalYearApi'
import { formatINR, formatDate } from '../utils/format'
import { useAuth } from '../context/AuthContext'

const emptyForm = {
  label: '',
  year: new Date().getFullYear(),
  startDate: new Date().toISOString().slice(0, 10),
  durationDays: 5,
  carryForwardBalance: 0,
}

export default function FestivalSetup() {
  const { user } = useAuth()
  const isPresident = user?.role === 'PRESIDENT'

  const [years, setYears] = useState([])
  const [loading, setLoading] = useState(true)
  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [form, setForm] = useState(emptyForm)
  const [submitting, setSubmitting] = useState(false)

  const load = () => {
    setLoading(true)
    festivalYearApi.getAll().then(setYears).finally(() => setLoading(false))
  }

  useEffect(load, [])

  const openAdd = () => { setEditing(null); setForm(emptyForm); setFormOpen(true) }
  const openEdit = (y) => {
    setEditing(y)
    setForm({
      label: y.label,
      year: y.year,
      startDate: y.startDate,
      durationDays: y.durationDays,
      carryForwardBalance: y.carryForwardBalance,
    })
    setFormOpen(true)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSubmitting(true)
    try {
      const payload = { ...form, year: Number(form.year), durationDays: Number(form.durationDays), carryForwardBalance: Number(form.carryForwardBalance) }
      if (editing) {
        await festivalYearApi.update(editing.id, payload)
      } else {
        await festivalYearApi.create(payload)
      }
      setFormOpen(false)
      load()
    } catch (err) {
      alert(err?.response?.data ? JSON.stringify(err.response.data) : 'Failed to save. Only the President can manage festival setup.')
    } finally {
      setSubmitting(false)
    }
  }

  const handleActivate = async (y) => {
    await festivalYearApi.activate(y.id)
    load()
  }

  return (
    <Layout>
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-6">
        <h1 className="page-title">Festival Setup</h1>
        {isPresident && (
          <button onClick={openAdd} className="btn-primary inline-flex items-center gap-2 w-fit">
            <Plus size={18} /> New Festival Year
          </button>
        )}
      </div>

      {!isPresident && (
        <div className="card bg-saffron-50 border-saffron-200 text-maroon-600 mb-6 text-sm">
          Only the President can create or edit festival years. You can view the list below.
        </div>
      )}

      {loading ? (
        <p className="text-maroon-400">Loading…</p>
      ) : years.length === 0 ? (
        <div className="card text-center py-10">
          <CalendarDays className="mx-auto text-saffron-400 mb-2" size={32} />
          <p className="text-maroon-500">No festival years set up yet.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          {years.map((y) => (
            <div key={y.id} className={`card ${y.active ? 'ring-2 ring-saffron-400' : ''}`}>
              <div className="flex items-start justify-between mb-3">
                <div>
                  <h3 className="font-semibold text-maroon-800">{y.label}</h3>
                  <p className="text-xs text-maroon-500">Year {y.year}</p>
                </div>
                {y.active ? (
                  <span className="badge bg-green-100 text-green-700 inline-flex items-center gap-1">
                    <CheckCircle2 size={12} /> Active
                  </span>
                ) : isPresident && (
                  <button onClick={() => handleActivate(y)} className="text-xs text-saffron-600 hover:underline">
                    Set Active
                  </button>
                )}
              </div>
              <div className="space-y-1.5 text-sm">
                <Row label="Start Date" value={formatDate(y.startDate)} />
                <Row label="Duration" value={`${y.durationDays} day${y.durationDays > 1 ? 's' : ''}`} />
                <Row label="Carry-Forward Balance" value={formatINR(y.carryForwardBalance)} />
              </div>
              {isPresident && (
                <button onClick={() => openEdit(y)} className="btn-secondary w-full mt-4 inline-flex items-center justify-center gap-2 text-sm py-2">
                  <Pencil size={14} /> Edit Date / Duration
                </button>
              )}
            </div>
          ))}
        </div>
      )}

      <Modal open={formOpen} onClose={() => setFormOpen(false)} title={editing ? 'Edit Festival Year' : 'New Festival Year'}>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="label-text">Label *</label>
            <input className="input-field" value={form.label} onChange={(e) => setForm({ ...form, label: e.target.value })}
                   placeholder="e.g. 2026 Ganesh Utsav" required />
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="label-text">Year *</label>
              <input type="number" className="input-field" value={form.year} onChange={(e) => setForm({ ...form, year: e.target.value })}
                     disabled={!!editing} required />
            </div>
            <div>
              <label className="label-text">Duration (days) *</label>
              <input type="number" min="1" className="input-field" value={form.durationDays}
                     onChange={(e) => setForm({ ...form, durationDays: e.target.value })} required />
            </div>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="label-text">Start Date *</label>
              <input type="date" className="input-field" value={form.startDate}
                     onChange={(e) => setForm({ ...form, startDate: e.target.value })} required />
            </div>
            <div>
              <label className="label-text">Carry-Forward Balance (₹) *</label>
              <input type="number" min="0" className="input-field" value={form.carryForwardBalance}
                     onChange={(e) => setForm({ ...form, carryForwardBalance: e.target.value })}
                     placeholder="Leftover funds from last year" required />
            </div>
          </div>
          <p className="text-xs text-maroon-400">
            Enter ₹0 if there's no leftover balance from the previous year's celebration.
          </p>
          <div className="flex gap-3 pt-2">
            <button type="submit" className="btn-primary flex-1" disabled={submitting}>
              {submitting ? 'Saving…' : editing ? 'Update Festival Year' : 'Create Festival Year'}
            </button>
            <button type="button" className="btn-secondary" onClick={() => setFormOpen(false)}>Cancel</button>
          </div>
        </form>
      </Modal>
    </Layout>
  )
}

function Row({ label, value }) {
  return (
    <div className="flex justify-between">
      <span className="text-maroon-500">{label}</span>
      <span className="font-medium text-maroon-800">{value}</span>
    </div>
  )
}
