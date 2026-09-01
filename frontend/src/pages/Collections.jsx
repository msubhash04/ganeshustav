import React, { useEffect, useState, useCallback } from 'react'
import { Plus, Search, Printer } from 'lucide-react'
import Layout from '../components/layout/Layout'
import Modal from '../components/common/Modal'
import FestivalYearGate from '../components/common/FestivalYearGate'
import DonationForm from '../components/donations/DonationForm'
import DonationTable from '../components/donations/DonationTable'
import Receipt from '../components/donations/Receipt'
import { donationApi } from '../api/donationApi'
import { formatINR } from '../utils/format'

export default function Collections() {
  const [donations, setDonations] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [total, setTotal] = useState(0)

  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  const [receiptDonation, setReceiptDonation] = useState(null)

  const [filters, setFilters] = useState({ name: '', startDate: '', endDate: '', minAmount: '', maxAmount: '' })

  const loadDonations = useCallback((appliedFilters) => {
    setLoading(true)
    const params = {}
    Object.entries(appliedFilters || {}).forEach(([k, v]) => { if (v) params[k] = v })
    donationApi
      .search(params)
      .then(setDonations)
      .catch(() => setError('Could not load donations.'))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    loadDonations(filters)
    donationApi.getTotal().then(setTotal).catch(() => {})
  }, []) // eslint-disable-line

  const handleFilterSubmit = (e) => {
    e.preventDefault()
    loadDonations(filters)
  }

  const handleAdd = () => { setEditing(null); setFormOpen(true) }
  const handleEdit = (d) => { setEditing(d); setFormOpen(true) }

  const handleSubmit = async (formData) => {
    setSubmitting(true)
    try {
      if (editing) {
        await donationApi.update(editing.id, formData)
      } else {
        const created = await donationApi.create(formData)
        setReceiptDonation(created) // auto-show printable receipt after adding
      }
      setFormOpen(false)
      loadDonations(filters)
      donationApi.getTotal().then(setTotal)
    } catch (err) {
      alert(err?.response?.data ? JSON.stringify(err.response.data) : 'Failed to save donation')
    } finally {
      setSubmitting(false)
    }
  }

  const handleDelete = async (d) => {
    if (!confirm(`Delete donation from ${d.donorName}?`)) return
    await donationApi.remove(d.id)
    loadDonations(filters)
    donationApi.getTotal().then(setTotal)
  }

  const handlePrint = (d) => {
    setReceiptDonation(d)
    setTimeout(() => window.print(), 200)
  }

  return (
    <Layout>
      <h1 className="page-title mb-6">Collections / Donations</h1>

      <FestivalYearGate>
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-6">
          <p className="text-sm text-maroon-500">Every donation received this festival, in one place.</p>
          <button onClick={handleAdd} className="btn-primary inline-flex items-center gap-2 w-fit">
            <Plus size={18} /> Add Donation
          </button>
        </div>

        {/* Total collected */}
        <div className="card mb-6 bg-gradient-to-r from-saffron-500 to-maroon-600 text-white">
          <p className="text-sm opacity-90">Total Collected</p>
          <p className="text-3xl font-bold">{formatINR(total)}</p>
        </div>

        {/* Filters */}
        <form onSubmit={handleFilterSubmit} className="card mb-6 grid grid-cols-1 sm:grid-cols-5 gap-3 items-end">
          <div>
            <label className="label-text">Search by Name</label>
            <input className="input-field" placeholder="Donor name" value={filters.name}
                   onChange={(e) => setFilters({ ...filters, name: e.target.value })} />
          </div>
          <div className="min-w-0">
            <label className="label-text">From Date</label>
            <input type="date" className="input-field" value={filters.startDate}
                   onChange={(e) => setFilters({ ...filters, startDate: e.target.value })} />
          </div>
          <div className="min-w-0">
            <label className="label-text">To Date</label>
            <input type="date" className="input-field" value={filters.endDate}
                   onChange={(e) => setFilters({ ...filters, endDate: e.target.value })} />
          </div>
          <div>
            <label className="label-text">Min Amount</label>
            <input type="number" className="input-field" placeholder="0" value={filters.minAmount}
                   onChange={(e) => setFilters({ ...filters, minAmount: e.target.value })} />
          </div>
          <button type="submit" className="btn-secondary inline-flex items-center justify-center gap-2">
            <Search size={16} /> Filter
          </button>
        </form>

        {error && <div className="card bg-maroon-50 border-maroon-200 text-maroon-700 mb-6">{error}</div>}

        <div className="card">
          {loading ? (
            <p className="text-maroon-400">Loading…</p>
          ) : (
            <DonationTable donations={donations} onEdit={handleEdit} onDelete={handleDelete} onPrint={handlePrint} />
          )}
        </div>

        {/* Add/Edit modal */}
        <Modal open={formOpen} onClose={() => setFormOpen(false)} title={editing ? 'Edit Donation' : 'Add New Donation'}>
          <DonationForm initialData={editing} onSubmit={handleSubmit} onCancel={() => setFormOpen(false)} submitting={submitting} />
        </Modal>

        {/* Receipt modal */}
        <Modal open={!!receiptDonation} onClose={() => setReceiptDonation(null)} title="Donation Receipt">
          <Receipt donation={receiptDonation} />
          <button onClick={() => window.print()} className="btn-primary w-full mt-4 inline-flex items-center justify-center gap-2">
            <Printer size={16} /> Print Receipt
          </button>
        </Modal>
      </FestivalYearGate>
    </Layout>
  )
}
