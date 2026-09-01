import React, { useState, useEffect } from 'react'
import { EXPENSE_CATEGORIES } from '../../api/expenseApi'

const PAYMENT_MODES = ['CASH', 'UPI', 'BANK_TRANSFER', 'CHEQUE']

const emptyForm = {
  description: '',
  category: 'MISCELLANEOUS',
  amount: '',
  paidTo: '',
  expenseDate: new Date().toISOString().slice(0, 10),
  paymentMode: 'CASH',
  dayNumber: '',
  note: '',
}

export default function ExpenseForm({ initialData, onSubmit, onCancel, submitting }) {
  const [form, setForm] = useState(emptyForm)
  const [billFile, setBillFile] = useState(null)
  const [errors, setErrors] = useState({})

  useEffect(() => {
    if (initialData) {
      setForm({
        description: initialData.description || '',
        category: initialData.category || 'MISCELLANEOUS',
        amount: initialData.amount || '',
        paidTo: initialData.paidTo || '',
        expenseDate: initialData.expenseDate || emptyForm.expenseDate,
        paymentMode: initialData.paymentMode || 'CASH',
        dayNumber: initialData.dayNumber || '',
        note: initialData.note || '',
      })
    }
  }, [initialData])

  const handleChange = (field) => (e) => setForm({ ...form, [field]: e.target.value })

  const validate = () => {
    const errs = {}
    if (!form.description.trim()) errs.description = 'Description is required'
    if (!form.paidTo.trim()) errs.paidTo = 'Paid To / Vendor name is required'
    if (!form.amount || Number(form.amount) <= 0) errs.amount = 'Enter an amount greater than zero'
    if (!form.expenseDate) errs.expenseDate = 'Date is required'
    if (form.category === 'MISCELLANEOUS' && !form.note.trim()) {
      errs.note = 'A note is required for Gift Distribution / Miscellaneous expenses'
    }
    setErrors(errs)
    return Object.keys(errs).length === 0
  }

  const handleSubmit = (e) => {
    e.preventDefault()
    if (!validate()) return
    onSubmit({ ...form, amount: Number(form.amount) }, billFile)
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <label className="label-text">Description *</label>
        <input className="input-field" value={form.description} onChange={handleChange('description')} placeholder="e.g. Idol purchase, tent rental" />
        {errors.description && <p className="text-xs text-maroon-600 mt-1">{errors.description}</p>}
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
        <div>
          <label className="label-text">Category *</label>
          <select className="input-field" value={form.category} onChange={handleChange('category')}>
            {EXPENSE_CATEGORIES.map((c) => (
              <option key={c.value} value={c.value}>{c.label}</option>
            ))}
          </select>
        </div>
        <div>
          <label className="label-text">Amount (₹) *</label>
          <input type="number" min="1" className="input-field" value={form.amount} onChange={handleChange('amount')} placeholder="5000" />
          {errors.amount && <p className="text-xs text-maroon-600 mt-1">{errors.amount}</p>}
        </div>
      </div>

      <div>
        <label className="label-text">Paid To / Vendor Name *</label>
        <input className="input-field" value={form.paidTo} onChange={handleChange('paidTo')} placeholder="e.g. Shree Decorators" />
        {errors.paidTo && <p className="text-xs text-maroon-600 mt-1">{errors.paidTo}</p>}
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
        <div>
          <label className="label-text">Payment Mode *</label>
          <select className="input-field" value={form.paymentMode} onChange={handleChange('paymentMode')}>
            {PAYMENT_MODES.map((m) => (
              <option key={m} value={m}>{m.replace('_', ' ')}</option>
            ))}
          </select>
        </div>
        <div className="min-w-0">
          <label className="label-text">Date *</label>
          <input type="date" className="input-field" value={form.expenseDate} onChange={handleChange('expenseDate')} />
          {errors.expenseDate && <p className="text-xs text-maroon-600 mt-1">{errors.expenseDate}</p>}
        </div>
      </div>

      <div>
        <label className="label-text">Festival Day (optional)</label>
        <input type="number" min="1" className="input-field" value={form.dayNumber} onChange={handleChange('dayNumber')}
               placeholder="e.g. 3 — leave blank if not day-specific" />
      </div>

      {form.category === 'MISCELLANEOUS' && (
        <div>
          <label className="label-text">Note — why was this amount spent? *</label>
          <textarea className="input-field" rows={2} value={form.note} onChange={handleChange('note')}
                     placeholder="e.g. Gifts distributed to volunteers on Day 5" />
          {errors.note && <p className="text-xs text-maroon-600 mt-1">{errors.note}</p>}
        </div>
      )}

      <div>
        <label className="label-text">Bill / Invoice Upload (optional)</label>
        <input type="file" accept="image/*,application/pdf" className="input-field"
               onChange={(e) => setBillFile(e.target.files?.[0] || null)} />
      </div>

      <div className="flex gap-3 pt-2">
        <button type="submit" className="btn-primary flex-1" disabled={submitting}>
          {submitting ? 'Saving…' : initialData ? 'Update Expense' : 'Add Expense'}
        </button>
        <button type="button" className="btn-secondary" onClick={onCancel}>Cancel</button>
      </div>
    </form>
  )
}
