import React, { useEffect, useState } from 'react'
import { useParams, Link } from 'react-router-dom'
import {
  Wallet, TrendingDown, PiggyBank, Gift, Gavel, Coins, Eye, ArrowLeft,
  Archive, CheckCircle2, Loader2,
} from 'lucide-react'
import Footer from '../components/common/Footer'
import SummaryCard from '../components/common/SummaryCard'
import ExpensePieChart from '../components/charts/ExpensePieChart'
import { publicApi } from '../api/reportApi'
import { formatINR, formatDate } from '../utils/format'

// Public, unauthenticated "Read-Only Observation Dashboard" - reachable
// via the landing page's committee-code search, or a shared link like
// /observe/GU-MH-PUN-0001. Deliberately built from read-only display
// components only (SummaryCard, ExpensePieChart) - there is no form,
// button, or code path here that could create/edit/delete anything, so
// "Restricted Actions" is true by construction, not by disabling buttons
// that could otherwise exist.
export default function PublicObserve() {
  const { tenantCode } = useParams()
  const [state, setState] = useState('loading') // loading | live | no-active | invalid
  const [summary, setSummary] = useState(null)
  const [years, setYears] = useState([])
  const [yearsLoaded, setYearsLoaded] = useState(false)

  useEffect(() => {
    if (!tenantCode) {
      setState('invalid')
      return
    }
    setState('loading')
    publicApi.observeActive(tenantCode)
      .then((data) => {
        setSummary(data)
        setState(data.found ? 'live' : 'no-active')
      })
      .catch(() => setState('invalid'))
  }, [tenantCode])

  const loadYears = () => {
    if (yearsLoaded) return
    publicApi.getYearOptions(tenantCode).then((list) => {
      setYears(list)
      setYearsLoaded(true)
    })
  }

  const viewYear = (festivalYearId) => {
    setState('loading')
    publicApi.getYearSummary(tenantCode, festivalYearId)
      .then((data) => { setSummary(data); setState('live') })
      .catch(() => setState('invalid'))
  }

  return (
    <div className="min-h-screen flex flex-col bg-cream">
      {/* Prominent Read-Only Observer banner, exactly as specified */}
      <div className="bg-maroon-800 text-white px-4 py-2.5 text-center text-sm font-medium flex items-center justify-center gap-2 flex-wrap">
        <Eye size={15} className="shrink-0" />
        Viewing in Read-Only Observer Mode
        {summary?.committeeName && (
          <span className="opacity-90">— {summary.committeeName}{summary.label ? ` | ${summary.label}` : ''}</span>
        )}
      </div>

      <header className="px-4 md:px-8 py-4 flex items-center justify-between">
        <Link to="/" className="inline-flex items-center gap-1.5 text-sm text-maroon-500 hover:text-maroon-700 transition">
          <ArrowLeft size={16} /> Back to Home
        </Link>
        <div className="flex items-center gap-2 text-maroon-800 font-display font-bold">
          <span className="text-xl">🐘</span> Ganesh Utsav
        </div>
      </header>

      <main className="flex-1 max-w-4xl mx-auto px-4 py-6 w-full">
        {state === 'loading' && (
          <div className="card text-center py-12 text-maroon-400 flex flex-col items-center gap-2">
            <Loader2 size={24} className="animate-spin" />
            Loading Committee Data…
          </div>
        )}

        {state === 'invalid' && (
          <div className="card text-center py-12">
            <p className="text-maroon-700 font-semibold mb-1">Invalid Code</p>
            <p className="text-sm text-maroon-500">No committee was found with code "{tenantCode}". Double-check it and try again.</p>
            <Link to="/#public-search" className="btn-primary inline-flex mt-4">Try Another Code</Link>
          </div>
        )}

        {(state === 'live' || state === 'no-active') && summary && (
          <>
            <div className="mb-6">
              <h1 className="page-title mb-0.5">{summary.committeeName}</h1>
              <p className="text-xs text-maroon-400 font-mono">{summary.tenantCode}</p>
            </div>

            {state === 'no-active' && (
              <div className="card bg-saffron-50 border-saffron-200 text-maroon-600 mb-6 text-sm">
                Festival Not Found — this committee doesn't have a live festival for the current year right now.
                Browse its past festivals below instead.
              </div>
            )}

            {state === 'live' && (
              <>
                <div className="flex items-center gap-2 mb-4">
                  <h2 className="text-lg font-display font-bold text-maroon-800">{summary.label}</h2>
                  <span className={`badge ${summary.active ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-500'}`}>
                    {summary.active ? 'Active' : 'Archived'}
                  </span>
                </div>
                <p className="text-xs text-maroon-400 mb-6">
                  {formatDate(summary.startDate)} · {summary.durationDays} day{summary.durationDays > 1 ? 's' : ''}
                </p>

                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 mb-6">
                  <SummaryCard title="Total Collections" value={formatINR(summary.totalCollections)} icon={Wallet} tone="saffron" />
                  <SummaryCard title="Total Expenses" value={formatINR(summary.totalExpenses)} icon={TrendingDown} tone="maroon" />
                  <SummaryCard title="Total Sponsorships" value={formatINR(summary.totalSponsorships)} icon={Gift} tone="saffron" />
                  <SummaryCard title="Auction Earnings" value={formatINR(summary.totalAuctionEarnings)} icon={Gavel} tone="gold" />
                  <SummaryCard title="Total Contributors" value={summary.totalDonorCount} icon={PiggyBank} tone="gold" />
                  <SummaryCard
                    title={summary.netSurplusOrDeficit >= 0 ? 'Net Surplus' : 'Net Deficit'}
                    value={formatINR(Math.abs(summary.netSurplusOrDeficit))}
                    icon={Coins}
                    tone={summary.netSurplusOrDeficit >= 0 ? 'saffron' : 'maroon'}
                  />
                </div>

                {Object.keys(summary.expenseByCategory || {}).length > 0 && (
                  <div className="card mb-6">
                    <h3 className="font-semibold text-maroon-800 mb-2">How Funds Were Used</h3>
                    <ExpensePieChart data={summary.expenseByCategory} />
                  </div>
                )}

                <p className="text-center text-xs text-maroon-400 mb-6">
                  Published by {summary.committeeName} for transparency with all contributors. Donor and transaction
                  details are kept private — only aggregate totals are shown here. 🙏
                </p>
              </>
            )}

            {/* Past Festivals / Festival Archives - available to every visitor, no login required */}
            <div className="card">
              <button onClick={loadYears} className="w-full flex items-center justify-between text-left">
                <span className="font-semibold text-maroon-800 flex items-center gap-2">
                  <Archive size={16} /> Past Festivals / Festival Archives
                </span>
                <span className="text-xs text-saffron-600 font-medium">{yearsLoaded ? '' : 'Tap to browse →'}</span>
              </button>

              {yearsLoaded && (
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 mt-4">
                  {years.map((y) => (
                    <button
                      key={y.id}
                      onClick={() => viewYear(y.id)}
                      className={`text-left px-4 py-3 rounded-xl border transition hover:border-saffron-400 hover:bg-saffron-50 ${
                        summary?.festivalYearId === y.id ? 'border-saffron-400 bg-saffron-50' : 'border-saffron-100'
                      }`}
                    >
                      <div className="flex items-center justify-between gap-2">
                        <span className="font-medium text-maroon-800">{y.label}</span>
                        {y.active ? (
                          <span className="badge bg-green-100 text-green-700 inline-flex items-center gap-1 shrink-0">
                            <CheckCircle2 size={11} /> Active
                          </span>
                        ) : (
                          <span className="badge bg-gray-100 text-gray-500 shrink-0">Archived</span>
                        )}
                      </div>
                      <span className="text-xs text-maroon-400">Year {y.year} · View audit summary →</span>
                    </button>
                  ))}
                  {years.length === 0 && (
                    <p className="text-sm text-maroon-400 sm:col-span-2">No festival years recorded for this committee yet.</p>
                  )}
                </div>
              )}
            </div>
          </>
        )}
      </main>

      <Footer />
    </div>
  )
}
