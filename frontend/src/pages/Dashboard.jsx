import React, { useEffect, useState } from 'react'
import { Wallet, TrendingDown, PiggyBank, Landmark, Coins } from 'lucide-react'
import Layout from '../components/layout/Layout'
import SummaryCard from '../components/common/SummaryCard'
import ExpensePieChart from '../components/charts/ExpensePieChart'
import CollectionExpenseBarChart from '../components/charts/CollectionExpenseBarChart'
import ResponsiveTable, { TableCard } from '../components/common/ResponsiveTable'
import { dashboardApi } from '../api/reportApi'
import { formatINR, formatDate } from '../utils/format'

export default function Dashboard() {
  const [summary, setSummary] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    dashboardApi
      .getSummary()
      .then(setSummary)
      .catch(() => setError('Could not load dashboard data. Is the backend running?'))
      .finally(() => setLoading(false))
  }, [])

  return (
    <Layout>
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="page-title">Dashboard</h1>
          {summary?.activeFestivalYearLabel && (
            <p className="text-sm text-maroon-500">{summary.activeFestivalYearLabel}</p>
          )}
        </div>
      </div>

      {error && (
        <div className="card bg-maroon-50 border-maroon-200 text-maroon-700 mb-6">{error}</div>
      )}

      {loading ? (
        <p className="text-maroon-400">Loading…</p>
      ) : summary ? (
        <>
          {/* Summary cards */}
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4 mb-4">
            <SummaryCard title="Total Collection (This Year)" value={formatINR(summary.totalCollection)} icon={Wallet} tone="saffron" />
            <SummaryCard title="Total Expenses" value={formatINR(summary.totalExpenses)} icon={TrendingDown} tone="maroon" />
            <SummaryCard title="Balance Remaining" value={formatINR(summary.balanceRemaining)} icon={PiggyBank} tone="gold" />
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 mb-6">
            <SummaryCard title="Carry-Forward from Last Year" value={formatINR(summary.carryForwardBalance)} icon={Landmark} tone="gold" />
            <SummaryCard title="Grand Total Available Funds" value={formatINR(summary.grandTotalAvailableFunds)} icon={Coins} tone="saffron" />
          </div>

          {/* Charts */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 mb-6">
            <div className="card">
              <h3 className="font-semibold text-maroon-800 mb-2">Expense Breakdown by Category</h3>
              <ExpensePieChart data={summary.expenseByCategory} />
            </div>
            <div className="card">
              <h3 className="font-semibold text-maroon-800 mb-2">Collections vs Expenses Over Time</h3>
              <CollectionExpenseBarChart data={summary.monthlyTrend} />
            </div>
          </div>

          {/* Recent transactions */}
          <div className="card">
            <h3 className="font-semibold text-maroon-800 mb-3">Recent Transactions</h3>
            <ResponsiveTable
              data={summary.recentTransactions?.map((tx, i) => ({ ...tx, _key: i })) || []}
              keyField="_key"
              emptyMessage="No transactions yet. Add your first donation or expense!"
              renderCard={(tx) => {
                const isDonation = tx.type === 'COLLECTION'
                return (
                  <TableCard>
                    <div className="flex items-start justify-between gap-3">
                      <div className="min-w-0">
                        <span className={`badge ${isDonation ? 'bg-saffron-100 text-saffron-700' : 'bg-maroon-100 text-maroon-700'}`}>
                          {isDonation ? 'Collection' : 'Expense'}
                        </span>
                        <p className="text-sm text-maroon-700 mt-1.5 truncate">{tx.label}</p>
                        <p className="text-xs text-maroon-400">{formatDate(tx.date)}</p>
                      </div>
                      <p className={`text-base font-bold shrink-0 ${isDonation ? 'text-saffron-600' : 'text-maroon-700'}`}>
                        {isDonation ? '+' : '−'} {formatINR(tx.amount)}
                      </p>
                    </div>
                  </TableCard>
                )
              }}
            >
              <table className="w-full text-sm">
                <thead>
                  <tr className="text-left text-maroon-500 border-b border-saffron-100">
                    <th className="py-2 pr-4">Type</th>
                    <th className="py-2 pr-4">Details</th>
                    <th className="py-2 pr-4">Date</th>
                    <th className="py-2 pr-4 text-right">Amount</th>
                  </tr>
                </thead>
                <tbody>
                  {summary.recentTransactions.map((tx, i) => {
                    const isDonation = tx.type === 'COLLECTION'
                    return (
                      <tr key={i} className="border-b border-saffron-50 last:border-0">
                        <td className="py-2 pr-4">
                          <span className={`badge ${isDonation ? 'bg-saffron-100 text-saffron-700' : 'bg-maroon-100 text-maroon-700'}`}>
                            {isDonation ? 'Collection' : 'Expense'}
                          </span>
                        </td>
                        <td className="py-2 pr-4">{tx.label}</td>
                        <td className="py-2 pr-4 text-maroon-500">
                          {formatDate(tx.date)}
                        </td>
                        <td className={`py-2 pr-4 text-right font-semibold ${isDonation ? 'text-saffron-600' : 'text-maroon-700'}`}>
                          {isDonation ? '+' : '−'} {formatINR(tx.amount)}
                        </td>
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </ResponsiveTable>
          </div>
        </>
      ) : null}
    </Layout>
  )
}
