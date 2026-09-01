import React, { useEffect, useState } from 'react'
import { FileDown, FileSpreadsheet } from 'lucide-react'
import Layout from '../components/layout/Layout'
import FestivalYearGate from '../components/common/FestivalYearGate'
import { reportApi, dashboardApi, downloadBlob } from '../api/reportApi'
import { formatINR } from '../utils/format'
import ExpensePieChart from '../components/charts/ExpensePieChart'

export default function Reports() {
  const [range, setRange] = useState({ startDate: '', endDate: '' })
  const [summary, setSummary] = useState(null)
  const [downloading, setDownloading] = useState('')

  useEffect(() => {
    dashboardApi.getSummary().then(setSummary).catch(() => {})
  }, [])

  const handleDownload = async (type) => {
    setDownloading(type)
    try {
      const res = type === 'pdf'
        ? await reportApi.downloadPdf(range.startDate || undefined, range.endDate || undefined)
        : await reportApi.downloadExcel(range.startDate || undefined, range.endDate || undefined)
      downloadBlob(res, `ganesh-utsav-report.${type === 'pdf' ? 'pdf' : 'xlsx'}`)
    } catch (err) {
      alert('Failed to generate report. Please try again.')
    } finally {
      setDownloading('')
    }
  }

  return (
    <Layout>
      <h1 className="page-title mb-6">Reports</h1>

      <FestivalYearGate>
        <div className="card mb-6">
          <h3 className="font-semibold text-maroon-800 mb-4">Generate Custom Report</h3>
          <div className="grid grid-cols-1 sm:grid-cols-4 gap-3 items-end mb-2">
            <div className="min-w-0">
              <label className="label-text">From Date</label>
              <input type="date" className="input-field" value={range.startDate}
                     onChange={(e) => setRange({ ...range, startDate: e.target.value })} />
            </div>
            <div className="min-w-0">
              <label className="label-text">To Date</label>
              <input type="date" className="input-field" value={range.endDate}
                     onChange={(e) => setRange({ ...range, endDate: e.target.value })} />
            </div>
            <button onClick={() => handleDownload('pdf')} disabled={downloading === 'pdf'}
                    className="btn-primary inline-flex items-center justify-center gap-2">
              <FileDown size={16} /> {downloading === 'pdf' ? 'Generating…' : 'Download PDF'}
            </button>
            <button onClick={() => handleDownload('excel')} disabled={downloading === 'excel'}
                    className="btn-secondary inline-flex items-center justify-center gap-2">
              <FileSpreadsheet size={16} /> {downloading === 'excel' ? 'Generating…' : 'Download Excel'}
            </button>
          </div>
          <p className="text-xs text-maroon-400">
            Leave dates blank to generate a report for all transactions to date. Reports include the full income &amp; expense statement,
            category-wise expense summary, and final balance sheet — suitable for sharing with donors for transparency.
          </p>
        </div>

        {summary && (
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
            <div className="card">
              <h3 className="font-semibold text-maroon-800 mb-3">Balance Sheet Snapshot</h3>
              <div className="space-y-3">
                <Row label="Total Collection" value={formatINR(summary.totalCollection)} />
                <Row label="Total Expenses" value={formatINR(summary.totalExpenses)} />
                <hr className="border-saffron-100" />
                <Row label="Balance Remaining" value={formatINR(summary.balanceRemaining)} bold />
              </div>
            </div>
            <div className="card">
              <h3 className="font-semibold text-maroon-800 mb-2">Category-wise Expense Summary</h3>
              <ExpensePieChart data={summary.expenseByCategory} />
            </div>
          </div>
        )}
      </FestivalYearGate>
    </Layout>
  )
}

function Row({ label, value, bold }) {
  return (
    <div className="flex justify-between">
      <span className={bold ? 'font-semibold text-maroon-800' : 'text-maroon-500'}>{label}</span>
      <span className={bold ? 'font-bold text-saffron-600 text-lg' : 'font-medium text-maroon-800'}>{value}</span>
    </div>
  )
}
