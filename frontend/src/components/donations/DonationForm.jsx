import React, { useState, useEffect } from 'react'

const PAYMENT_MODES = ['CASH', 'UPI', 'BANK_TRANSFER', 'CHEQUE']

const emptyForm = {
  donorName: '',
  phoneNumber: '',
  address: '',
  amount: '',
  paymentMode: 'CASH',
  donationDate: new Date().toISOString().slice(0, 10),
}

export default function DonationForm({ initialData, onSubmit, onCancel, submitting }) {
  const [form, setForm] = useState(emptyForm)
  const [errors, setErrors] = useState({})

  useEffect(() => {
    if (initialData) {
      setForm({
        donorName: initialData.donorName || '',
        phoneNumber: initialData.phoneNumber || '',
        address: initialData.address || '',
        amount: initialData.amount || '',
        paymentMode: initialData.paymentMode || 'CASH',
        donationDate: initialData.donationDate || emptyForm.donationDate,
      })
    }
  }, [initialData])

  const handleChange = (field) => (e) => setForm({ ...form, [field]: e.target.value })

  const validate = () => {
    const errs = {}
    if (!form.donorName.trim()) errs.donorName = 'Donor name is required'
    if (!form.phoneNumber.trim()) errs.phoneNumber = 'Phone number is required'
    else if (!/^\d{10}$/.test(form.phoneNumber.trim())) errs.phoneNumber = 'Enter a valid 10-digit phone number'
    if (!form.amount || Number(form.amount) <= 0) errs.amount = 'Enter an amount greater than zero'
    if (!form.donationDate) errs.donationDate = 'Date is required'
    setErrors(errs)
    return Object.keys(errs).length === 0
  }

  const handleSubmit = (e) => {
    e.preventDefault()
    if (!validate()) return
    onSubmit({ ...form, amount: Number(form.amount) })
  }

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      <div>
        <label className="label-text">Donor Name *</label>
        <input className="input-field" value={form.donorName} onChange={handleChange('donorName')} placeholder="e.g. Ramesh Kulkarni" />
        {errors.donorName && <p className="text-xs text-maroon-600 mt-1">{errors.donorName}</p>}
      </div>

      <div className="grid grid-cols-2 gap-3">
        <div>
          <label className="label-text">Phone Number *</label>
          <input className="input-field" value={form.phoneNumber} onChange={handleChange('phoneNumber')} placeholder="9876543210" maxLength={10} />
          {errors.phoneNumber && <p className="text-xs text-maroon-600 mt-1">{errors.phoneNumber}</p>}
        </div>
        <div>
          <label className="label-text">Amount (₹) *</label>
          <input type="number" min="1" className="input-field" value={form.amount} onChange={handleChange('amount')} placeholder="1100" />
          {errors.amount && <p className="text-xs text-maroon-600 mt-1">{errors.amount}</p>}
        </div>
      </div>

      <div>
        <label className="label-text">Address (optional)</label>
        <input className="input-field" value={form.address} onChange={handleChange('address')} placeholder="Street, area, city" />
      </div>

      <div className="grid grid-cols-2 gap-3">
        <div>
          <label className="label-text">Payment Mode *</label>
          <select className="input-field" value={form.paymentMode} onChange={handleChange('paymentMode')}>
            {PAYMENT_MODES.map((m) => (
              <option key={m} value={m}>{m.replace('_', ' ')}</option>
            ))}
          </select>
        </div>
        <div>
          <label className="label-text">Date *</label>
          <input type="date" className="input-field" value={form.donationDate} onChange={handleChange('donationDate')} />
          {errors.donationDate && <p className="text-xs text-maroon-600 mt-1">{errors.donationDate}</p>}
        </div>
      </div>

      <div className="flex gap-3 pt-2">
        <button type="submit" className="btn-primary flex-1" disabled={submitting}>
          {submitting ? 'Saving…' : initialData ? 'Update Donation' : 'Add Donation'}
        </button>
        <button type="button" className="btn-secondary" onClick={onCancel}>Cancel</button>
      </div>
    </form>
  )
}
