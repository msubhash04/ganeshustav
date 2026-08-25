import React, { useEffect, useState } from 'react'
import { Wallet, TrendingDown, PiggyBank, Users } from 'lucide-react'
import SummaryCard from '../components/common/SummaryCard'
import ExpensePieChart from '../components/charts/ExpensePieChart'
import { publicApi } from '../api/reportApi'
import { formatINR } from '../utils/format'

export default function PublicTransparency() {
  const [data, setData] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    publicApi.getTransparency().then(setData).catch(() => setError('Could not load transparency data.'))
  }, [])

  return (
    <div className="min-h-screen bg-cream">
      <header className="bg-gradient-to-r from-saffron-500 to-maroon-700 text-white px-4 py-8 text-center">
        <div className="text-4xl mb-2">🐘</div>
        <h1 className="text-2xl font-bold font-display">Ganesh Utsav — Fund Transparency</h1>
        <p className="text-saffron-100 mt-1">Public view of collections &amp; expenses. Donor details are kept private.</p>
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
              This page is published by the Ganesh Utsav Committee for transparency with all contributors. 🙏
            </p>
          </>
        )}
      </main>
    </div>
  )
}
