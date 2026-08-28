import React, { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import Modal from './Modal'
import { useAuth } from '../../context/AuthContext'

// Shared by DeveloperDashboard and Committees so "Inspect Committee" looks
// and behaves identically no matter where the Developer starts from.
export default function InspectCommitteeModal({ committee, onClose }) {
  const { startInspection } = useAuth()
  const navigate = useNavigate()
  const [mode, setMode] = useState('READ_ONLY')
  const [submitting, setSubmitting] = useState(false)

  const handleStart = async () => {
    if (!committee) return
    setSubmitting(true)
    try {
      await startInspection(committee.id, mode)
      onClose()
      navigate('/')
    } catch (err) {
      alert(err?.response?.data?.error || err?.response?.data || 'Could not start inspection')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <Modal open={!!committee} onClose={onClose} title="Inspect Committee">
      {committee && (
        <div className="space-y-4">
          <p className="text-sm text-maroon-600">
            You're about to view <strong>{committee.name}</strong>'s full dashboard exactly as their President
            sees it. Staff management and committee settings stay off-limits either way — exit inspection to
            change those from here.
          </p>
          <div className="space-y-2">
            <label className={`flex items-start gap-3 p-3 rounded-xl border cursor-pointer ${
              mode === 'READ_ONLY' ? 'border-saffron-400 bg-saffron-50' : 'border-saffron-100'
            }`}>
              <input type="radio" name="inspectMode" className="mt-1" checked={mode === 'READ_ONLY'}
                     onChange={() => setMode('READ_ONLY')} />
              <span>
                <span className="block font-semibold text-maroon-800">Read-Only Mode</span>
                <span className="block text-xs text-maroon-500">View every screen and record. Any attempt to add, edit, or delete is blocked.</span>
              </span>
            </label>
            <label className={`flex items-start gap-3 p-3 rounded-xl border cursor-pointer ${
              mode === 'ADMIN_OVERRIDE' ? 'border-amber-400 bg-amber-50' : 'border-saffron-100'
            }`}>
              <input type="radio" name="inspectMode" className="mt-1" checked={mode === 'ADMIN_OVERRIDE'}
                     onChange={() => setMode('ADMIN_OVERRIDE')} />
              <span>
                <span className="block font-semibold text-maroon-800">Admin Override Mode</span>
                <span className="block text-xs text-maroon-500">Correct or assist with data entry on this committee's behalf. Every change is written to the audit log.</span>
              </span>
            </label>
          </div>
          <div className="flex gap-3 pt-1">
            <button onClick={handleStart} className="btn-primary flex-1" disabled={submitting}>
              {submitting ? 'Starting…' : 'Start Inspection'}
            </button>
            <button type="button" className="btn-secondary" onClick={onClose}>Cancel</button>
          </div>
        </div>
      )}
    </Modal>
  )
}
