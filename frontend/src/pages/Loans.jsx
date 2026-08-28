import React, { useEffect, useState } from 'react'
import { Plus, Landmark, ChevronDown, ChevronUp } from 'lucide-react'
import Layout from '../components/layout/Layout'
import Modal from '../components/common/Modal'
import ResponsiveTable, { TableCard } from '../components/common/ResponsiveTable'
import { loanApi } from '../api/loanApi'
import { formatINR, formatDate } from '../utils/format'
import { useAuth } from '../context/AuthContext'

const emptyLoanForm = { borrowerName: '', borrowerPhone: '', principalAmount: '', monthlyInterestRatePercent: 2, loanDate: new Date().toISOString().slice(0, 10) }
const emptyRepaymentForm = { paymentDate: new Date().toISOString().slice(0, 10), paymentAmount: '' }

export default function Loans() {
  const { user } = useAuth()
  const isPresident = user?.role === 'PRESIDENT'

  const [loans, setLoans] = useState([])
  const [outstandingTotal, setOutstandingTotal] = useState(0)
  const [loading, setLoading] = useState(true)
  const [expandedId, setExpandedId] = useState(null)

  const [loanFormOpen, setLoanFormOpen] = useState(false)
  const [loanForm, setLoanForm] = useState(emptyLoanForm)
  const [submittingLoan, setSubmittingLoan] = useState(false)

  const [repayLoanId, setRepayLoanId] = useState(null)
  const [repayForm, setRepayForm] = useState(emptyRepaymentForm)
  const [submittingRepay, setSubmittingRepay] = useState(false)

  const load = () => {
    setLoading(true)
    Promise.all([loanApi.getAll(), loanApi.getOutstandingTotal()])
      .then(([l, t]) => { setLoans(l); setOutstandingTotal(t) })
      .catch(() => {})
      .finally(() => setLoading(false))
  }

  useEffect(load, [])

  if (!isPresident) {
    return (
      <Layout>
        <h1 className="page-title mb-6">Post-Festival Loans</h1>
        <div className="card text-center py-10">
          <Landmark className="mx-auto text-saffron-400 mb-2" size={32} />
          <p className="text-maroon-500">Loan &amp; repayment management is restricted to the President.</p>
        </div>
      </Layout>
    )
  }

  const handleCreateLoan = async (e) => {
    e.preventDefault()
    setSubmittingLoan(true)
    try {
      await loanApi.create({
        ...loanForm,
        principalAmount: Number(loanForm.principalAmount),
        monthlyInterestRatePercent: Number(loanForm.monthlyInterestRatePercent),
      })
      setLoanFormOpen(false)
      setLoanForm(emptyLoanForm)
      load()
    } catch (err) {
      alert(err?.response?.data ? JSON.stringify(err.response.data) : 'Failed to create loan')
    } finally {
      setSubmittingLoan(false)
    }
  }

  const handleRepay = async (e) => {
    e.preventDefault()
    setSubmittingRepay(true)
    try {
      await loanApi.recordRepayment(repayLoanId, {
        ...repayForm,
        paymentAmount: Number(repayForm.paymentAmount),
      })
      setRepayLoanId(null)
      setRepayForm(emptyRepaymentForm)
      load()
    } catch (err) {
      alert(err?.response?.data ? JSON.stringify(err.response.data) : 'Failed to record repayment')
    } finally {
      setSubmittingRepay(false)
    }
  }

  return (
    <Layout>
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 mb-6">
        <h1 className="page-title">Post-Festival Loans</h1>
        <button onClick={() => setLoanFormOpen(true)} className="btn-primary inline-flex items-center gap-2 w-fit">
          <Plus size={18} /> New Loan
        </button>
      </div>

      <div className="card mb-6 bg-gradient-to-r from-maroon-700 to-maroon-800 text-white">
        <p className="text-sm opacity-90">Total Outstanding Principal</p>
        <p className="text-3xl font-bold">{formatINR(outstandingTotal)}</p>
      </div>

      {loading ? (
        <p className="text-maroon-400">Loading…</p>
      ) : loans.length === 0 ? (
        <div className="card text-center py-10">
          <Landmark className="mx-auto text-saffron-400 mb-2" size={32} />
          <p className="text-maroon-500">No loans recorded yet.</p>
        </div>
      ) : (
        <div className="space-y-3">
          {loans.map((loan) => (
            <div key={loan.id} className="card">
              <div className="flex items-center justify-between gap-3 cursor-pointer" onClick={() => setExpandedId(expandedId === loan.id ? null : loan.id)}>
                <div className="min-w-0">
                  <div className="flex items-center flex-wrap gap-2">
                    <h3 className="font-semibold text-maroon-800 truncate">{loan.borrowerName}</h3>
                    <span className={`badge shrink-0 ${loan.status === 'ACTIVE' ? 'bg-saffron-100 text-saffron-700' : 'bg-green-100 text-green-700'}`}>
                      {loan.status}
                    </span>
                  </div>
                  <p className="text-xs text-maroon-500">
                    {loan.borrowerPhone && `${loan.borrowerPhone} · `}
                    Borrowed {formatINR(loan.originalPrincipal)} on {formatDate(loan.loanDate)} @ {loan.monthlyInterestRatePercent}%/month
                  </p>
                </div>
                <div className="flex items-center gap-3 shrink-0">
                  <div className="text-right">
                    <p className="text-xs text-maroon-500 whitespace-nowrap">Current Principal</p>
                    <p className="font-bold text-maroon-800">{formatINR(loan.currentPrincipal)}</p>
                  </div>
                  {expandedId === loan.id ? <ChevronUp size={18} className="text-maroon-400" /> : <ChevronDown size={18} className="text-maroon-400" />}
                </div>
              </div>

              {expandedId === loan.id && (
                <div className="mt-4 pt-4 border-t border-saffron-100">
                  <div className="flex items-center justify-between mb-3">
                    <p className="text-sm text-maroon-500">
                      Interest accrued as of today: <span className="font-semibold text-maroon-700">{formatINR(loan.accruedInterestAsOfToday)}</span>
                    </p>
                    {loan.status === 'ACTIVE' && (
                      <button
                        onClick={(e) => { e.stopPropagation(); setRepayLoanId(loan.id); setRepayForm(emptyRepaymentForm) }}
                        className="btn-secondary text-sm py-1.5 px-3"
                      >
                        Record Repayment
                      </button>
                    )}
                  </div>

                  {loan.repayments?.length > 0 ? (
                    <ResponsiveTable
                      data={loan.repayments}
                      emptyMessage="No repayments recorded yet."
                      renderCard={(r) => (
                        <TableCard>
                          <div className="flex items-start justify-between gap-3">
                            <p className="text-sm text-maroon-500">{formatDate(r.paymentDate)}</p>
                            <p className="text-base font-bold text-maroon-800 shrink-0">{formatINR(r.paymentAmount)}</p>
                          </div>
                          <div className="grid grid-cols-2 gap-2 mt-2.5 text-xs">
                            <div className="flex justify-between">
                              <span className="text-maroon-400">Interest</span>
                              <span className="text-gold-600 font-medium">{formatINR(r.interestPortion)}</span>
                            </div>
                            <div className="flex justify-between">
                              <span className="text-maroon-400">Principal</span>
                              <span className="text-saffron-600 font-medium">{formatINR(r.principalPortion)}</span>
                            </div>
                            <div className="flex justify-between col-span-2 pt-1.5 border-t border-saffron-50">
                              <span className="text-maroon-400">Balance After</span>
                              <span className="text-maroon-700 font-semibold">{formatINR(r.remainingPrincipalAfter)}</span>
                            </div>
                          </div>
                        </TableCard>
                      )}
                    >
                      <table className="w-full text-sm">
                        <thead>
                          <tr className="text-left text-maroon-500 border-b border-saffron-100">
                            <th className="py-2 pr-4">Date</th>
                            <th className="py-2 pr-4 text-right">Payment</th>
                            <th className="py-2 pr-4 text-right">Interest Portion</th>
                            <th className="py-2 pr-4 text-right">Principal Portion</th>
                            <th className="py-2 pr-4 text-right">Balance After</th>
                          </tr>
                        </thead>
                        <tbody>
                          {loan.repayments.map((r) => (
                            <tr key={r.id} className="border-b border-saffron-50 last:border-0">
                              <td className="py-2 pr-4 text-maroon-500">{formatDate(r.paymentDate)}</td>
                              <td className="py-2 pr-4 text-right font-medium text-maroon-800">{formatINR(r.paymentAmount)}</td>
                              <td className="py-2 pr-4 text-right text-gold-600">{formatINR(r.interestPortion)}</td>
                              <td className="py-2 pr-4 text-right text-saffron-600">{formatINR(r.principalPortion)}</td>
                              <td className="py-2 pr-4 text-right font-semibold text-maroon-700">{formatINR(r.remainingPrincipalAfter)}</td>
                            </tr>
                          ))}
                        </tbody>
                      </table>
                    </ResponsiveTable>
                  ) : (
                    <p className="text-sm text-maroon-400">No repayments recorded yet.</p>
                  )}
                </div>
              )}
            </div>
          ))}
        </div>
      )}

      {/* New Loan modal */}
      <Modal open={loanFormOpen} onClose={() => setLoanFormOpen(false)} title="New Loan">
        <form onSubmit={handleCreateLoan} className="space-y-4">
          <div>
            <label className="label-text">Borrower Name *</label>
            <input className="input-field" value={loanForm.borrowerName} onChange={(e) => setLoanForm({ ...loanForm, borrowerName: e.target.value })} required />
          </div>
          <div>
            <label className="label-text">Borrower Phone (optional)</label>
            <input className="input-field" value={loanForm.borrowerPhone} onChange={(e) => setLoanForm({ ...loanForm, borrowerPhone: e.target.value })} />
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            <div>
              <label className="label-text">Principal Amount (₹) *</label>
              <input type="number" min="1" className="input-field" value={loanForm.principalAmount}
                     onChange={(e) => setLoanForm({ ...loanForm, principalAmount: e.target.value })} required />
            </div>
            <div>
              <label className="label-text">Monthly Interest (%) *</label>
              <input type="number" min="0" step="0.01" className="input-field" value={loanForm.monthlyInterestRatePercent}
                     onChange={(e) => setLoanForm({ ...loanForm, monthlyInterestRatePercent: e.target.value })} required />
            </div>
          </div>
          <div>
            <label className="label-text">Loan Date *</label>
            <input type="date" className="input-field" value={loanForm.loanDate} onChange={(e) => setLoanForm({ ...loanForm, loanDate: e.target.value })} required />
          </div>
          <div className="flex gap-3 pt-2">
            <button type="submit" className="btn-primary flex-1" disabled={submittingLoan}>
              {submittingLoan ? 'Saving…' : 'Create Loan'}
            </button>
            <button type="button" className="btn-secondary" onClick={() => setLoanFormOpen(false)}>Cancel</button>
          </div>
        </form>
      </Modal>

      {/* Record Repayment modal */}
      <Modal open={!!repayLoanId} onClose={() => setRepayLoanId(null)} title="Record Repayment">
        <form onSubmit={handleRepay} className="space-y-4">
          <div>
            <label className="label-text">Payment Date *</label>
            <input type="date" className="input-field" value={repayForm.paymentDate}
                   onChange={(e) => setRepayForm({ ...repayForm, paymentDate: e.target.value })} required />
          </div>
          <div>
            <label className="label-text">Payment Amount (₹) *</label>
            <input type="number" min="1" className="input-field" value={repayForm.paymentAmount}
                   onChange={(e) => setRepayForm({ ...repayForm, paymentAmount: e.target.value })} required />
          </div>
          <p className="text-xs text-maroon-400">
            Interest accrued since the last payment (or loan start) will automatically be deducted first, and the remainder applied to reduce the principal.
          </p>
          <div className="flex gap-3 pt-2">
            <button type="submit" className="btn-primary flex-1" disabled={submittingRepay}>
              {submittingRepay ? 'Recording…' : 'Record Repayment'}
            </button>
            <button type="button" className="btn-secondary" onClick={() => setRepayLoanId(null)}>Cancel</button>
          </div>
        </form>
      </Modal>
    </Layout>
  )
}
