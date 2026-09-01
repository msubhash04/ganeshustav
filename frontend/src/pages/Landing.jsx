import React, { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import {
  Search, LogIn, Sparkles, CalendarCheck, HandCoins, Gavel, Gift,
  FileBarChart, ArrowRight, Loader2, Code2,
} from 'lucide-react'
import Footer from '../components/common/Footer'
import CodeViewerDrawer, { DEMO_CHIP_KEYS } from '../components/common/CodeViewerDrawer'
import { publicApi } from '../api/reportApi'

const FEATURES = [
  {
    icon: CalendarCheck,
    title: 'Festival Setup Locks',
    desc: 'One festival per calendar year, strictly enforced — every module stays locked until this year\'s setup exists.',
  },
  {
    icon: HandCoins,
    title: 'Real-Time Collections & Expenses',
    desc: 'Every donation and expense logged instantly, scoped to the exact festival year it belongs to.',
  },
  {
    icon: Gavel,
    title: 'Live Auction Management',
    desc: 'Track Velampata items, winning bids, and payment status as the auction happens.',
  },
  {
    icon: Gift,
    title: 'Sponsorship Dashboards',
    desc: 'General and Annadanam sponsors organized by category, day, and contribution.',
  },
  {
    icon: FileBarChart,
    title: 'Historical Year Audit Reports',
    desc: 'Every past festival automatically archived into a complete, read-only audit report — surplus, deficit, and full ledger.',
  },
]

const DEMO_CHIPS = DEMO_CHIP_KEYS

export default function Landing() {
  const navigate = useNavigate()
  const [code, setCode] = useState('')
  const [status, setStatus] = useState('idle') // idle | loading | invalid
  const [drawerOpen, setDrawerOpen] = useState(false)
  const [drawerKey, setDrawerKey] = useState('yearGuard')

  const handleSearch = async (e) => {
    e.preventDefault()
    const trimmed = code.trim()
    if (!trimmed) return
    setStatus('loading')
    try {
      // Any successful lookup (active festival or not) is a valid code -
      // the observer page itself handles "live" vs "no active festival,
      // browse archives instead" once it gets there.
      await publicApi.observeActive(trimmed)
      navigate(`/observe/${encodeURIComponent(trimmed)}`)
    } catch (err) {
      setStatus('invalid')
    }
  }

  return (
    <div className="min-h-screen flex flex-col bg-cream">
      {/* Top bar */}
      <header className="px-4 md:px-8 py-4 flex items-center justify-between">
        <div className="flex items-center gap-2 text-maroon-800 font-display font-bold text-lg">
          <span className="text-2xl">🐘</span> Ganesh Utsav
        </div>
        <Link to="/login" className="btn-secondary inline-flex items-center gap-2">
          <LogIn size={16} /> Log In
        </Link>
      </header>

      {/* Hero */}
      <section className="px-4 md:px-8 py-12 md:py-20 text-center max-w-3xl mx-auto">
        <span className="badge bg-saffron-100 text-saffron-700 mb-4">Festival Committee Management Platform</span>
        <h1 className="text-3xl md:text-5xl font-display font-bold text-maroon-800 leading-tight mb-4">
          Every Ganesh Utsav committee's money, tracked in one place.
        </h1>
        <p className="text-maroon-500 text-base md:text-lg mb-8">
          Collections, expenses, auctions, and sponsorships — organized year by year, with a public transparency
          view every donor can trust.
        </p>
        <div className="flex flex-col sm:flex-row gap-3 justify-center">
          <Link to="/login" className="btn-primary inline-flex items-center justify-center gap-2">
            <LogIn size={18} /> Log In (President / Admin)
          </Link>
          <a href="#public-search" className="btn-secondary inline-flex items-center justify-center gap-2">
            <Search size={18} /> Access Public Features
          </a>
        </div>
      </section>

      {/* Feature highlights */}
      <section className="px-4 md:px-8 py-10 max-w-5xl mx-auto w-full">
        <h2 className="text-xl md:text-2xl font-display font-bold text-maroon-800 text-center mb-8">
          What the platform handles for you
        </h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {FEATURES.map(({ icon: Icon, title, desc }) => (
            <div key={title} className="card">
              <div className="w-11 h-11 rounded-xl bg-gradient-to-br from-saffron-500 to-maroon-600 flex items-center justify-center text-white mb-3">
                <Icon size={20} />
              </div>
              <h3 className="font-semibold text-maroon-800 mb-1">{title}</h3>
              <p className="text-sm text-maroon-500">{desc}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Public committee search */}
      <section id="public-search" className="px-4 md:px-8 py-10 max-w-xl mx-auto w-full scroll-mt-6">
        <div className="card">
          <h2 className="text-lg font-display font-bold text-maroon-800 mb-1">Access Public Features</h2>
          <p className="text-sm text-maroon-500 mb-4">
            Enter a committee's Ganesh Unique Code to view its live festival — no login needed.
          </p>
          <form onSubmit={handleSearch} className="flex flex-col sm:flex-row gap-2">
            <input
              className="input-field font-mono"
              placeholder="e.g. GU-MH-PUN-0001"
              value={code}
              onChange={(e) => { setCode(e.target.value); setStatus('idle') }}
            />
            <button type="submit" className="btn-primary inline-flex items-center justify-center gap-2 shrink-0" disabled={status === 'loading'}>
              {status === 'loading' ? <><Loader2 size={16} className="animate-spin" /> Loading…</> : <><Search size={16} /> View Committee</>}
            </button>
          </form>
          {status === 'invalid' && (
            <p className="text-sm text-maroon-600 bg-maroon-50 rounded-lg px-3 py-2 mt-3">
              Invalid Code — no committee found with that Ganesh Unique Code. Double-check it and try again.
            </p>
          )}
        </div>
      </section>

      {/* Demo showcase */}
      <section className="px-4 md:px-8 py-10 max-w-3xl mx-auto w-full">
        <div className="card">
          <h2 className="text-lg font-display font-bold text-maroon-800 mb-1 flex items-center gap-2">
            <Code2 size={18} className="text-saffron-500" /> Developer / Demo Showcase
          </h2>
          <p className="text-sm text-maroon-500 mb-4">
            Curious how the platform enforces its rules? Tap a chip to see the real logic behind it.
          </p>
          <div className="flex flex-wrap gap-2">
            {DEMO_CHIPS.map(({ key, label }) => (
              <button
                key={key}
                onClick={() => { setDrawerKey(key); setDrawerOpen(true) }}
                className="inline-flex items-center gap-1.5 px-3.5 py-2 rounded-full text-sm font-medium bg-saffron-50 text-saffron-700 hover:bg-saffron-100 transition"
              >
                {label} <ArrowRight size={13} />
              </button>
            ))}
          </div>
        </div>
      </section>

      <Footer />
      <CodeViewerDrawer open={drawerOpen} onClose={() => setDrawerOpen(false)} initialKey={drawerKey} />
    </div>
  )
}
