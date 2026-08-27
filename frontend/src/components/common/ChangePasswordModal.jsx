import React, { useState } from 'react'
import Modal from './Modal'
import { authApi } from '../../api/authApi'

const emptyForm = { currentPassword: '', newPassword: '', confirmPassword: '' }

export default function ChangePasswordModal({ open, onClose }) {
  const [form, setForm] = useState(emptyForm)
  const [error, setError] = useState('')
  const [success, setSuccess] = useState('')
  const [submitting, setSubmitting] = useState(false)

  const handleClose = () => {
    setForm(emptyForm)
    setError('')
    setSuccess('')
    onClose()
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setSuccess('')

    if (form.newPassword !== form.confirmPassword) {
      setError('New password and confirmation do not match')
      return
    }
    if (form.newPassword.length < 8) {
      setError('New password must be at least 8 characters long')
      return
    }

    setSubmitting(true)
    try {
      await authApi.changePassword(form.currentPassword, form.newPassword)
      setSuccess('Password updated successfully')
      setForm(emptyForm)
    } catch (err) {
      setError(err?.response?.data || 'Failed to update password')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Modal open={open} onClose={handleClose} title="Change Password">
      <form onSubmit={handleSubmit} className="space-y-4">
        <div>
          <label className="label-text">Current Password *</label>
          <input
            type="password"
            className="input-field"
            value={form.currentPassword}
            onChange={(e) => setForm({ ...form, currentPassword: e.target.value })}
            required
          />
        </div>
        <div>
          <label className="label-text">New Password *</label>
          <input
            type="password"
            className="input-field"
            value={form.newPassword}
            onChange={(e) => setForm({ ...form, newPassword: e.target.value })}
            minLength={8}
            required
          />
        </div>
        <div>
          <label className="label-text">Confirm New Password *</label>
          <input
            type="password"
            className="input-field"
            value={form.confirmPassword}
            onChange={(e) => setForm({ ...form, confirmPassword: e.target.value })}
            minLength={8}
            required
          />
        </div>

        {error && <p className="text-sm text-maroon-600 bg-maroon-50 rounded-lg px-3 py-2">{error}</p>}
        {success && <p className="text-sm text-green-700 bg-green-50 rounded-lg px-3 py-2">{success}</p>}

        <div className="flex gap-3 pt-2">
          <button type="submit" className="btn-primary flex-1" disabled={submitting}>
            {submitting ? 'Updating…' : 'Update Password'}
          </button>
          <button type="button" className="btn-secondary" onClick={handleClose}>Close</button>
        </div>
      </form>
    </Modal>
  )
}
