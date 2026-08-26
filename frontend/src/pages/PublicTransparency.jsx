import React, { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { Wallet, TrendingDown, PiggyBank, Users } from 'lucide-react'
import SummaryCard from '../components/common/SummaryCard'
import ExpensePieChart from '../components/charts/ExpensePieChart'
import { publicApi } from '../api/reportApi'
import { formatINR } from '../utils/format'

// One public, read-only page PER COMMITTEE, addressed by its Ganesh
// Unique Code in the URL (e.g. /public/GU-MH-PUN-0001). This is what
// keeps one committee's transparency page from ever showing another
// committee's totals.
export default function PublicTransparency() {
  const { tenantCode } = useParams()
  const [data, setData] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!tenantCode) {
      setError('No committee code was provided in the link.')
      return
    }
    publicApi
      .getTransparency(tenantCode)
      .then(setData)
      .catch(() => setError('No committee found with this code, or it could not be loaded.'))
  }, [tenantCode])

  return (
    <div className="min-h-screen bg-cream">
      <header className="bg-gradient-to-r from-saffron-500 to-maroon-700 text-white px-4 py-8 text-center">
        <div className="text-4xl mb-2">🐘</div>
        <h1 className="text-2xl font-bold font-display">
          {data ? data.committeeName : 'Ganesh Utsav'} — Fund Transparency
        </h1>
        <p className="text-saffron-100 mt-1">Public view of collections &amp; expenses. Donor details are kept private.</p>
        {data?.tenantCode && (
          <p className="text-xs text-saffron-200 mt-2 font-mono">Committee Code: {data.tenantCode}</p>
        )}
      </header>

      <main className="max-w-4xl mx-auto px-4 py-8">
        {error && <div className="card bg-maroon-50 border-maroon-200 text-maroon-700 mb-6">{error}</div>}

        {data && (
          <>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 mb-8">
              <SummaryCard title="Total Collection" value={formatINR(data.totalCollection)} icon={Wallet} tone="saffron" />
              <SummaryCard title="Total Expenses" value={formatINR(data.totalExpenses)} icon={TrendingDown} tone="maroon" />
              <SummaryCard title="Balance Remaining" value={formatINR(data.balanceRemaining)} icon={PiggyBank} tone="gold" />
              <SummaryCard title="Total Contributors" value={data.totalDonors} icon={Users} tone="saffron" />
            </div>

            <div className="card">
              <h3 className="font-semibold text-maroon-800 mb-2">How Funds Were Used</h3>
              <ExpensePieChart data={data.expenseByCategory} />
            </div>

            <p className="text-center text-xs text-maroon-400 mt-8">
              This page is published by {data.committeeName} for transparency with all contributors. 🙏
            </p>
          </>
        )}
      </main>
    </div>
  )
}
