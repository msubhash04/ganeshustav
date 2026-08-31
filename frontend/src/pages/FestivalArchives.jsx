import React, { useEffect, useState } from 'react'
import {
  Archive, CheckCircle2, ArrowLeft, Wallet, TrendingDown, PiggyBank,
  Gift, Gavel, Coins, Receipt,
} from 'lucide-react'
import Layout from '../components/layout/Layout'
import SummaryCard from '../components/common/SummaryCard'
import ResponsiveTable, { TableCard } from '../components/common/ResponsiveTable'
import { festivalYearApi } from '../api/festivalYearApi'
import { reportApi } from '../api/reportApi'
import { formatINR, formatDate } from '../utils/format'

const LEDGER_TABS = [
  { key: 'donations', label: 'Collections' },
  { key: 'expenses', label: 'Expenses' },
  { key: 'auctionItems', label: 'Auction' },
  { key: 'generalSponsors', label: 'General Sponsors' },
  { key: 'annadanamSponsors', label: 'Annadanam Sponsors' },
]

export default function FestivalArchives() {
  const [years, setYears] = useState([])
  const [loading, setLoading] = useState(true)
  const [selectedId, setSelectedId] = useState(null)

  useEffect(() => {
    festivalYearApi.getAll().then(setYears).finally(() => setLoading(false))
  }, [])

  if (selectedId) {
    return (
      <Layout>
        <button
          onClick={() => setSelectedId(null)}
          className="inline-flex items-center gap-1.5 text-sm text-saffron-600 hover:text-saffron-700 font-medium mb-4"
        >
          <ArrowLeft size={16} /> Back to Festival Archives
        </button>
        <AuditReport festivalYearId={selectedId} />
      </Layout>
    )
  }

  return (
    <Layout>
      <h1 className="page-title mb-1">Festival Archives</h1>
      <p className="text-sm text-maroon-400 mb-6">
        Every festival year your committee has run, active or archived. Select one for a complete, read-only audit report.
      </p>

      {loading ? (
        <p className="text-maroon-400">Loading…</p>
      ) : years.length === 0 ? (
        <div className="card text-center py-10">
          <Archive className="mx-auto text-saffron-400 mb-2" size={32} />
          <p className="text-maroon-500 text-sm">No festival years set up yet.</p>
        </div>
      ) : (
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {years.map((y) => (
            <button
              key={y.id}
              onClick={() => setSelectedId(y.id)}
              className={`card text-left hover:shadow-lg hover:-translate-y-0.5 transition ${y.active ? 'ring-2 ring-saffron-400' : ''}`}
            >
              <div className="flex items-start justify-between gap-2 mb-2">
                <div>
                  <h3 className="font-semibold text-maroon-800">{y.label}</h3>
                  <p className="text-xs text-maroon-500">Year {y.year}</p>
                </div>
                {y.active ? (
                  <span className="badge bg-green-100 text-green-700 inline-flex items-center gap-1 shrink-0">
                    <CheckCircle2 size={12} /> Active
                  </span>
                ) : (
                  <span className="badge bg-gray-100 text-gray-500 shrink-0">Archived</span>
                )}
              </div>
              <p className="text-xs text-maroon-400">{formatDate(y.startDate)} · {y.durationDays} day{y.durationDays > 1 ? 's' : ''}</p>
              <p className="text-sm text-saffron-600 font-medium mt-3">View full audit report →</p>
            </button>
          ))}
        </div>
      )}
    </Layout>
  )
}

function AuditReport({ festivalYearId }) {
  const [report, setReport] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [activeTab, setActiveTab] = useState('donations')

  useEffect(() => {
    setLoading(true)
    setError('')
    reportApi.getFestivalAudit(festivalYearId)
      .then(setReport)
      .catch(() => setError('Could not load the audit report for this festival year.'))
      .finally(() => setLoading(false))
  }, [festivalYearId])

  if (loading) return <p className="text-maroon-400">Loading audit report…</p>
  if (error) return <div className="card bg-maroon-50 border-maroon-200 text-maroon-700">{error}</div>
  if (!report) return null

  const isSurplus = report.netSurplusOrDeficit >= 0

  return (
    <div>
      {/* Header */}
      <div className="flex flex-wrap items-center justify-between gap-2 mb-6">
        <div>
          <h1 className="page-title mb-0.5">{report.label}</h1>
          <p className="text-sm text-maroon-400">
            {formatDate(report.startDate)} · {report.durationDays} day{report.durationDays > 1 ? 's' : ''}
          </p>
        </div>
        <span className={`badge ${report.active ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'}`}>
          {report.active ? 'Active' : 'Archived'}
        </span>
      </div>

      {/* Financial Summary */}
      <h2 className="text-lg font-display font-bold text-maroon-800 mb-3">Financial Summary</h2>
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 mb-3">
        <SummaryCard title="Carry-Forward Balance" value={formatINR(report.carryForwardBalance)} icon={PiggyBank} tone="gold" />
        <SummaryCard title="Total Collections" value={formatINR(report.totalCollections)} icon={Wallet} tone="saffron" />
        <SummaryCard title="Total Expenses" value={formatINR(report.totalExpenses)} icon={TrendingDown} tone="maroon" />
        <SummaryCard title="Total Sponsorships" value={formatINR(report.totalSponsorships)} icon={Gift} tone="saffron" />
        <SummaryCard title="Total Auction Earnings" value={formatINR(report.totalAuctionEarnings)} icon={Gavel} tone="gold" />
        <SummaryCard
          title={isSurplus ? 'Net Surplus' : 'Net Deficit'}
          value={formatINR(Math.abs(report.netSurplusOrDeficit))}
          icon={Coins}
          tone={isSurplus ? 'saffron' : 'maroon'}
        />
      </div>
      <p className="text-xs text-maroon-400 mb-8">
        Net {isSurplus ? 'surplus' : 'deficit'} = carry-forward + collections + sponsorships + auction earnings − expenses.
      </p>

      {/* Category breakdown */}
      <h2 className="text-lg font-display font-bold text-maroon-800 mb-3">Breakdown by Category</h2>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-8">
        <div className="card">
          <h3 className="font-semibold text-maroon-800 mb-3 flex items-center gap-2"><Receipt size={16} /> Expenses by Category</h3>
          {Object.keys(report.expenseByCategory || {}).length === 0 ? (
            <p className="text-sm text-maroon-400">No expenses recorded.</p>
          ) : (
            <div className="space-y-2">
              {Object.entries(report.expenseByCategory).map(([category, amount]) => (
                <div key={category} className="flex justify-between text-sm">
                  <span className="text-maroon-600">{category}</span>
                  <span className="font-medium text-maroon-800">{formatINR(amount)}</span>
                </div>
              ))}
            </div>
          )}
        </div>
        <div className="card">
          <h3 className="font-semibold text-maroon-800 mb-3 flex items-center gap-2"><Gift size={16} /> Sponsorships</h3>
          <div className="space-y-2 text-sm">
            <div className="flex justify-between">
              <span className="text-maroon-600">General Sponsors</span>
              <span className="font-medium text-maroon-800">{formatINR(report.generalSponsorshipTotal)}</span>
            </div>
            <div className="flex justify-between">
              <span className="text-maroon-600">Annadanam Sponsors</span>
              <span className="font-medium text-maroon-800">{formatINR(report.annadanamSponsorshipTotal)}</span>
            </div>
            <div className="flex justify-between pt-2 border-t border-saffron-50">
              <span className="text-maroon-500">Total</span>
              <span className="font-semibold text-saffron-600">{formatINR(report.totalSponsorships)}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Full Audit Trail / Ledger */}
      <h2 className="text-lg font-display font-bold text-maroon-800 mb-3">Full Audit Trail & Ledger</h2>
      <div className="flex gap-2 mb-4 border-b border-saffron-100 overflow-x-auto">
        {LEDGER_TABS.map(({ key, label }) => (
          <button
            key={key}
            onClick={() => setActiveTab(key)}
            className={`px-4 py-2.5 text-sm font-medium border-b-2 whitespace-nowrap shrink-0 transition ${
              activeTab === key ? 'border-saffron-500 text-saffron-600' : 'border-transparent text-maroon-400 hover:text-maroon-600'
            }`}
          >
            {label} <span className="text-xs text-maroon-300">({(report[key] || []).length})</span>
          </button>
        ))}
      </div>

      <div className="card">
        {activeTab === 'donations' && <DonationsLedger data={report.donations} />}
        {activeTab === 'expenses' && <ExpensesLedger data={report.expenses} />}
        {activeTab === 'auctionItems' && <AuctionLedger data={report.auctionItems} />}
        {activeTab === 'generalSponsors' && <GeneralSponsorsLedger data={report.generalSponsors} />}
        {activeTab === 'annadanamSponsors' && <AnnadanamSponsorsLedger data={report.annadanamSponsors} />}
      </div>
    </div>
  )
}

function DonationsLedger({ data }) {
  return (
    <ResponsiveTable
      data={data}
      keyField="receiptNumber"
      emptyMessage="No collections recorded for this festival year."
      renderCard={(d) => (
        <TableCard>
          <div className="flex items-start justify-between gap-3">
            <div className="min-w-0">
              <p className="font-semibold text-maroon-800 truncate">{d.donorName}</p>
              <p className="text-xs text-maroon-400 font-mono">{d.receiptNumber}</p>
            </div>
            <p className="text-base font-bold text-saffron-600 shrink-0">{formatINR(d.amount)}</p>
          </div>
          <p className="text-xs text-maroon-500 mt-2">{formatDate(d.donationDate)} · {d.paymentMode} · {d.phoneNumber}</p>
        </TableCard>
      )}
    >
      <table className="w-full text-sm">
        <thead>
          <tr className="text-left text-maroon-500 border-b border-saffron-100">
            <th className="py-2 pr-4">Receipt#</th>
            <th className="py-2 pr-4">Donor</th>
            <th className="py-2 pr-4">Phone</th>
            <th className="py-2 pr-4">Date</th>
            <th className="py-2 pr-4">Mode</th>
            <th className="py-2 pr-4">Recorded By</th>
            <th className="py-2 pr-4 text-right">Amount</th>
          </tr>
        </thead>
        <tbody>
          {data.map((d) => (
            <tr key={d.receiptNumber} className="border-b border-saffron-50 last:border-0">
              <td className="py-2 pr-4 text-maroon-500 font-mono text-xs">{d.receiptNumber}</td>
              <td className="py-2 pr-4 font-medium text-maroon-800">{d.donorName}</td>
              <td className="py-2 pr-4 text-maroon-500">{d.phoneNumber}</td>
              <td className="py-2 pr-4 text-maroon-500">{formatDate(d.donationDate)}</td>
              <td className="py-2 pr-4 text-maroon-500">{d.paymentMode}</td>
              <td className="py-2 pr-4 text-maroon-400">{d.recordedByName}</td>
              <td className="py-2 pr-4 text-right font-semibold text-saffron-600">{formatINR(d.amount)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </ResponsiveTable>
  )
}

function ExpensesLedger({ data }) {
  return (
    <ResponsiveTable
      data={data.map((e, i) => ({ ...e, _key: i }))}
      keyField="_key"
      emptyMessage="No expenses recorded for this festival year."
      renderCard={(e) => (
        <TableCard>
          <div className="flex items-start justify-between gap-3">
            <div className="min-w-0">
              <p className="font-semibold text-maroon-800 truncate">{e.description}</p>
              <span className="badge bg-maroon-100 text-maroon-700 mt-1">{e.category}</span>
            </div>
            <p className="text-base font-bold text-maroon-700 shrink-0">{formatINR(e.amount)}</p>
          </div>
          <p className="text-xs text-maroon-500 mt-2">
            {formatDate(e.expenseDate)}{e.dayNumber ? ` · Day ${e.dayNumber}` : ''}{e.paidTo ? ` · Paid to ${e.paidTo}` : ''}
          </p>
        </TableCard>
      )}
    >
      <table className="w-full text-sm">
        <thead>
          <tr className="text-left text-maroon-500 border-b border-saffron-100">
            <th className="py-2 pr-4">Day</th>
            <th className="py-2 pr-4">Description</th>
            <th className="py-2 pr-4">Category</th>
            <th className="py-2 pr-4">Paid To</th>
            <th className="py-2 pr-4">Date</th>
            <th className="py-2 pr-4">Recorded By</th>
            <th className="py-2 pr-4 text-right">Amount</th>
          </tr>
        </thead>
        <tbody>
          {data.map((e, i) => (
            <tr key={i} className="border-b border-saffron-50 last:border-0">
              <td className="py-2 pr-4 text-maroon-500">{e.dayNumber ?? '—'}</td>
              <td className="py-2 pr-4 font-medium text-maroon-800">{e.description}</td>
              <td className="py-2 pr-4"><span className="badge bg-maroon-100 text-maroon-700">{e.category}</span></td>
              <td className="py-2 pr-4 text-maroon-500">{e.paidTo || '—'}</td>
              <td className="py-2 pr-4 text-maroon-500">{formatDate(e.expenseDate)}</td>
              <td className="py-2 pr-4 text-maroon-400">{e.recordedByName}</td>
              <td className="py-2 pr-4 text-right font-semibold text-maroon-700">{formatINR(e.amount)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </ResponsiveTable>
  )
}

function AuctionLedger({ data }) {
  return (
    <ResponsiveTable
      data={data.map((a, i) => ({ ...a, _key: i }))}
      keyField="_key"
      emptyMessage="No auction items recorded for this festival year."
      renderCard={(a) => (
        <TableCard>
          <div className="flex items-start justify-between gap-3">
            <div className="min-w-0">
              <p className="font-semibold text-maroon-800 truncate">{a.itemName}</p>
              <p className="text-xs text-maroon-400">{a.dayNumber ? `Day ${a.dayNumber}` : 'Final Day'} · Winner: {a.winnerName}</p>
            </div>
            <p className="text-base font-bold text-saffron-600 shrink-0">{formatINR(a.bidAmount)}</p>
          </div>
          <span className={`badge mt-2 ${a.paymentStatus === 'PAID' ? 'bg-green-100 text-green-700' : 'bg-gold-500/10 text-gold-600'}`}>
            {a.paymentStatus}
          </span>
        </TableCard>
      )}
    >
      <table className="w-full text-sm">
        <thead>
          <tr className="text-left text-maroon-500 border-b border-saffron-100">
            <th className="py-2 pr-4">Day</th>
            <th className="py-2 pr-4">Item</th>
            <th className="py-2 pr-4">Winner</th>
            <th className="py-2 pr-4">Status</th>
            <th className="py-2 pr-4">Recorded By</th>
            <th className="py-2 pr-4 text-right">Bid Amount</th>
          </tr>
        </thead>
        <tbody>
          {data.map((a, i) => (
            <tr key={i} className="border-b border-saffron-50 last:border-0">
              <td className="py-2 pr-4 text-maroon-500">{a.dayNumber ? `Day ${a.dayNumber}` : 'Final Day'}</td>
              <td className="py-2 pr-4 font-medium text-maroon-800">{a.itemName}</td>
              <td className="py-2 pr-4 text-maroon-500">{a.winnerName}</td>
              <td className="py-2 pr-4">
                <span className={`badge ${a.paymentStatus === 'PAID' ? 'bg-green-100 text-green-700' : 'bg-gold-500/10 text-gold-600'}`}>
                  {a.paymentStatus}
                </span>
              </td>
              <td className="py-2 pr-4 text-maroon-400">{a.recordedByName}</td>
              <td className="py-2 pr-4 text-right font-semibold text-saffron-600">{formatINR(a.bidAmount)}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </ResponsiveTable>
  )
}

function GeneralSponsorsLedger({ data }) {
  return (
    <ResponsiveTable
      data={data.map((s, i) => ({ ...s, _key: i }))}
      keyField="_key"
      emptyMessage="No general sponsors recorded for this festival year."
      renderCard={(s) => (
        <TableCard>
          <div className="flex items-start justify-between gap-3">
            <div className="min-w-0">
              <p className="font-semibold text-maroon-800 truncate">{s.sponsorName}</p>
              <span className="badge bg-saffron-100 text-saffron-700 mt-1">{s.categoryName}</span>
            </div>
            {s.contributionAmount ? <p className="text-base font-bold text-saffron-600 shrink-0">{formatINR(s.contributionAmount)}</p> : null}
          </div>
          {s.contactInfo && <p className="text-xs text-maroon-500 mt-2">{s.contactInfo}</p>}
        </TableCard>
      )}
    >
      <table className="w-full text-sm">
        <thead>
          <tr className="text-left text-maroon-500 border-b border-saffron-100">
            <th className="py-2 pr-4">Sponsor</th>
            <th className="py-2 pr-4">Category</th>
            <th className="py-2 pr-4">Contact</th>
            <th className="py-2 pr-4 text-right">Amount</th>
          </tr>
        </thead>
        <tbody>
          {data.map((s, i) => (
            <tr key={i} className="border-b border-saffron-50 last:border-0">
              <td className="py-2 pr-4 font-medium text-maroon-800">{s.sponsorName}</td>
              <td className="py-2 pr-4"><span className="badge bg-saffron-100 text-saffron-700">{s.categoryName}</span></td>
              <td className="py-2 pr-4 text-maroon-500">{s.contactInfo || '—'}</td>
              <td className="py-2 pr-4 text-right font-semibold text-saffron-600">
                {s.contributionAmount ? formatINR(s.contributionAmount) : '—'}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </ResponsiveTable>
  )
}

function AnnadanamSponsorsLedger({ data }) {
  return (
    <ResponsiveTable
      data={data.map((s, i) => ({ ...s, _key: i }))}
      keyField="_key"
      emptyMessage="No Annadanam sponsors recorded for this festival year."
      renderCard={(s) => (
        <TableCard>
          <div className="flex items-start justify-between gap-3">
            <div className="min-w-0">
              <p className="font-semibold text-maroon-800 truncate">{s.sponsorName}</p>
              <div className="flex items-center gap-1.5 mt-1">
                <span className="badge bg-gold-500/10 text-gold-600">Day {s.dayNumber}</span>
                {s.mealSlot && <span className="text-xs text-maroon-400">{s.mealSlot}</span>}
              </div>
            </div>
            {s.contributionAmount ? <p className="text-base font-bold text-saffron-600 shrink-0">{formatINR(s.contributionAmount)}</p> : null}
          </div>
          {s.contactInfo && <p className="text-xs text-maroon-500 mt-2">{s.contactInfo}</p>}
        </TableCard>
      )}
    >
      <table className="w-full text-sm">
        <thead>
          <tr className="text-left text-maroon-500 border-b border-saffron-100">
            <th className="py-2 pr-4">Day</th>
            <th className="py-2 pr-4">Sponsor</th>
            <th className="py-2 pr-4">Meal</th>
            <th className="py-2 pr-4">Contact</th>
            <th className="py-2 pr-4 text-right">Amount</th>
          </tr>
        </thead>
        <tbody>
          {data.map((s, i) => (
            <tr key={i} className="border-b border-saffron-50 last:border-0">
              <td className="py-2 pr-4"><span className="badge bg-gold-500/10 text-gold-600">Day {s.dayNumber}</span></td>
              <td className="py-2 pr-4 font-medium text-maroon-800">{s.sponsorName}</td>
              <td className="py-2 pr-4 text-maroon-500">{s.mealSlot || '—'}</td>
              <td className="py-2 pr-4 text-maroon-500">{s.contactInfo || '—'}</td>
              <td className="py-2 pr-4 text-right font-semibold text-saffron-600">
                {s.contributionAmount ? formatINR(s.contributionAmount) : '—'}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </ResponsiveTable>
  )
}
