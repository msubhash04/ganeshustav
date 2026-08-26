import React, { useEffect, useState } from 'react'
import { Plus, Gavel, Trash2 } from 'lucide-react'
import Layout from '../components/layout/Layout'
import Modal from '../components/common/Modal'
import { auctionApi } from '../api/auctionApi'
import { festivalYearApi } from '../api/festivalYearApi'
import { formatINR } from '../utils/format'

const PAYMENT_MODES = ['CASH', 'UPI', 'BANK_TRANSFER', 'CHEQUE']

const emptyForm = { dayNumber: '', itemName: '', winnerName: '', bidAmount: '', paymentStatus: 'PENDING', paymentMode: '' }

export default function Auction() {
  const [festivalYear, setFestivalYear] = useState(null)
  const [items, setItems] = useState([])
  const [total, setTotal] = useState(0)
  const [loading, setLoading] = useState(true)
  const [formOpen, setFormOpen] = useState(false)
  const [form, setForm] = useState(emptyForm)
  const [submitting, setSubmitting] = useState(false)

  useEffect(() => {
    festivalYearApi.getActive().then((y) => {
      setFestivalYear(y)
      if (y) {
        loadItems(y.id)
      } else {
        setLoading(false)
      }
    })
  }, [])

  const loadItems = (yearId) => {
    setLoading(true)
    Promise.all([auctionApi.getByFestivalYear(yearId), auctionApi.getTotal(yearId)])
      .then(([itemsData, totalData]) => { setItems(itemsData); setTotal(totalData) })
      .finally(() => setLoading(false))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!festivalYear) return
    setSubmitting(true)
    try {
      const payload = {
        ...form,
        dayNumber: form.dayNumber ? Number(form.dayNumber) : null,
        bidAmount: Number(form.bidAmount),
        paymentMode: form.paymentStatus === 'PAID' ? (form.paymentMode || 'CASH') : null,
      }
      await auctionApi.create(festivalYear.id, payload)
      setFormOpen(false)
      setForm(emptyForm)
      loadItems(festivalYear.id)
    } catch (err) {
      alert(err?.response?.data ? JSON.stringify(err.response.data) : 'Failed to save auction item')
    } finally {
      setSubmitting(false)
    }
  }

  const handleDelete = async (item) => {
    if (!confirm(`Remove auction entry for "${item.itemName}"?`)) return
    await auctionApi.remove(item.id)
    loadItems(festivalYear.id)
  }

  if (!loading && !festivalYear) {
    return (
      <Layout>
        <h1 className="page-title mb-6">Auction / Velampata</h1>
        <div className="card text-center py-10">
          <Gavel className="mx-auto text-saffron-400 mb-2" size={32} />
          <p className="text-maroon-500">No active festival year set up yet. Ask the President to create one under Festival Setup first.</p>
        </div>
      </Layout>
    )
  }

  return (
    <Layout>
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-6">
        <div>
          <h1 className="page-title">Auction / Velampata</h1>
          {festivalYear && <p className="text-sm text-maroon-500">{festivalYear.label}</p>}
        </div>
        <button onClick={() => setFormOpen(true)} className="btn-primary inline-flex items-center gap-2 w-fit">
          <Plus size={18} /> Add Auction Item
        </button>
      </div>

      <div className="card mb-6 bg-gradient-to-r from-gold-500 to-saffron-600 text-white">
        <p className="text-sm opacity-90">Total Auction Amount</p>
        <p className="text-3xl font-bold">{formatINR(total)}</p>
      </div>

      <div className="card">
        {loading ? (
          <p className="text-maroon-400">Loading…</p>
        ) : items.length === 0 ? (
          <p className="text-sm text-maroon-400 py-8 text-center">No auction items recorded yet.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-sm">
              <thead>
                <tr className="text-left text-maroon-500 border-b border-saffron-100">
                  <th className="py-2 pr-4">Day</th>
                  <th className="py-2 pr-4">Item</th>
                  <th className="py-2 pr-4">Winner</th>
                  <th className="py-2 pr-4">Status</th>
                  <th className="py-2 pr-4 text-right">Bid Amount</th>
                  <th className="py-2 pr-4 text-right">Actions</th>
                </tr>
              </thead>
              <tbody>
                {items.map((item) => (
                  <tr key={item.id} className="border-b border-saffron-50 last:border-0">
                    <td className="py-2.5 pr-4 text-maroon-500">{item.dayNumber ? `Day ${item.dayNumber}` : 'Final Day'}</td>
                    <td className="py-2.5 pr-4 font-medium text-maroon-800">{item.itemName}</td>
                    <td className="py-2.5 pr-4 text-maroon-500">{item.winnerName}</td>
                    <td className="py-2.5 pr-4">
                      <span className={`badge ${item.paymentStatus === 'PAID' ? 'bg-green-100 text-green-700' : 'bg-gold-500/10 text-gold-600'}`}>
                        {item.paymentStatus}{item.paymentMode ? ` · ${item.paymentMode.replace('_', ' ')}` : ''}
                      </span>
                    </td>
                    <td className="py-2.5 pr-4 text-right font-semibold text-saffron-600">{formatINR(item.bidAmount)}</td>
                    <td className="py-2.5 pr-4 text-right">
                      <button onClick={() => handleDelete(item)} className="text-maroon-400 hover:text-maroon-700">
                        <Trash2 size={16} />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      <Modal open={formOpen} onClose={() => setFormOpen(false)} title="Add Auction Item">
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="label-text">Item Name *</label>
            <input className="input-field" value={form.itemName} onChange={(e) => setForm({ ...form, itemName: e.target.value })}
                   placeholder="e.g. Modak Laddu" required />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="label-text">Winner's Name *</label>
              <input className="input-field" value={form.winnerName} onChange={(e) => setForm({ ...form, winnerName: e.target.value })} required />
            </div>
            <div>
              <label className="label-text">Bid Amount (₹) *</label>
              <input type="number" min="1" className="input-field" value={form.bidAmount} onChange={(e) => setForm({ ...form, bidAmount: e.target.value })} required />
            </div>
          </div>
          <div>
            <label className="label-text">Day (optional — leave blank for final day / not day-specific)</label>
            <input type="number" min="1" max={festivalYear?.durationDays} className="input-field" value={form.dayNumber}
                   onChange={(e) => setForm({ ...form, dayNumber: e.target.value })} placeholder="e.g. 3" />
          </div>
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="label-text">Payment Status *</label>
              <select className="input-field" value={form.paymentStatus} onChange={(e) => setForm({ ...form, paymentStatus: e.target.value })}>
                <option value="PENDING">Pending</option>
                <option value="PAID">Paid</option>
              </select>
            </div>
            {form.paymentStatus === 'PAID' && (
              <div>
                <label className="label-text">Payment Mode</label>
                <select className="input-field" value={form.paymentMode} onChange={(e) => setForm({ ...form, paymentMode: e.target.value })}>
                  <option value="">Select…</option>
                  {PAYMENT_MODES.map((m) => <option key={m} value={m}>{m.replace('_', ' ')}</option>)}
                </select>
              </div>
            )}
          </div>
          <div className="flex gap-3 pt-2">
            <button type="submit" className="btn-primary flex-1" disabled={submitting}>
              {submitting ? 'Saving…' : 'Add Item'}
            </button>
            <button type="button" className="btn-secondary" onClick={() => setFormOpen(false)}>Cancel</button>
          </div>
        </form>
      </Modal>
    </Layout>
  )
}
