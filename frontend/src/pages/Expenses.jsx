import React, { useEffect, useState, useCallback } from 'react'
import { Plus, Search } from 'lucide-react'
import Layout from '../components/layout/Layout'
import Modal from '../components/common/Modal'
import FestivalYearGate from '../components/common/FestivalYearGate'
import ExpenseForm from '../components/expenses/ExpenseForm'
import ExpenseTable from '../components/expenses/ExpenseTable'
import { expenseApi, EXPENSE_CATEGORIES } from '../api/expenseApi'
import { formatINR } from '../utils/format'

export default function Expenses() {
  const [expenses, setExpenses] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [total, setTotal] = useState(0)

  const [formOpen, setFormOpen] = useState(false)
  const [editing, setEditing] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  const [filters, setFilters] = useState({ category: '', startDate: '', endDate: '' })

  const loadExpenses = useCallback((appliedFilters) => {
    setLoading(true)
    const params = {}
    Object.entries(appliedFilters || {}).forEach(([k, v]) => { if (v) params[k] = v })
    expenseApi
      .search(params)
      .then(setExpenses)
      .catch(() => setError('Could not load expenses.'))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    loadExpenses(filters)
    expenseApi.getTotal().then(setTotal).catch(() => {})
  }, []) // eslint-disable-line

  const handleFilterSubmit = (e) => {
    e.preventDefault()
    loadExpenses(filters)
  }

  const handleAdd = () => { setEditing(null); setFormOpen(true) }
  const handleEdit = (e) => { setEditing(e); setFormOpen(true) }

  const handleSubmit = async (formData, billFile) => {
    setSubmitting(true)
    try {
      if (editing) {
        await expenseApi.update(editing.id, formData, billFile)
      } else {
        await expenseApi.create(formData, billFile)
      }
      setFormOpen(false)
      loadExpenses(filters)
      expenseApi.getTotal().then(setTotal)
    } catch (err) {
      alert(err?.response?.data ? JSON.stringify(err.response.data) : 'Failed to save expense')
    } finally {
      setSubmitting(false)
    }
  }

  const handleDelete = async (e) => {
    if (!confirm(`Delete expense "${e.description}"?`)) return
    await expenseApi.remove(e.id)
    loadExpenses(filters)
    expenseApi.getTotal().then(setTotal)
  }

  return (
    <Layout>
      <h1 className="page-title mb-6">Expenses</h1>

      <FestivalYearGate>
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-6">
          <p className="text-sm text-maroon-500">Every rupee spent this festival, itemized by category and day.</p>
          <button onClick={handleAdd} className="btn-primary inline-flex items-center gap-2 w-fit">
            <Plus size={18} /> Add Expense
          </button>
        </div>

        <div className="card mb-6 bg-gradient-to-r from-maroon-700 to-maroon-800 text-white">
          <p className="text-sm opacity-90">Total Expenses</p>
          <p className="text-3xl font-bold">{formatINR(total)}</p>
        </div>

        <form onSubmit={handleFilterSubmit} className="card mb-6 grid grid-cols-1 sm:grid-cols-4 gap-3 items-end">
          <div>
            <label className="label-text">Category</label>
            <select className="input-field" value={filters.category}
                    onChange={(e) => setFilters({ ...filters, category: e.target.value })}>
              <option value="">All Categories</option>
              {EXPENSE_CATEGORIES.map((c) => (
                <option key={c.value} value={c.value}>{c.label}</option>
              ))}
            </select>
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
          <button type="submit" className="btn-secondary inline-flex items-center justify-center gap-2">
            <Search size={16} /> Filter
          </button>
        </form>

        {error && <div className="card bg-maroon-50 border-maroon-200 text-maroon-700 mb-6">{error}</div>}

        <div className="card">
          {loading ? (
            <p className="text-maroon-400">Loading…</p>
          ) : (
            <ExpenseTable expenses={expenses} onEdit={handleEdit} onDelete={handleDelete} />
          )}
        </div>

        <Modal open={formOpen} onClose={() => setFormOpen(false)} title={editing ? 'Edit Expense' : 'Add New Expense'}>
          <ExpenseForm initialData={editing} onSubmit={handleSubmit} onCancel={() => setFormOpen(false)} submitting={submitting} />
        </Modal>
      </FestivalYearGate>
    </Layout>
  )
}
