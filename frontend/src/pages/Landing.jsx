import React, { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import {
  Search, LogIn, CalendarCheck, HandCoins, Gavel, Gift,
  FileBarChart, Loader2, ShieldCheck, Receipt,
} from 'lucide-react'
import Footer from '../components/common/Footer'
import { publicApi } from '../api/reportApi'

const FEATURES = [
  {
    icon: CalendarCheck,
    accent: 'saffron',
    title: 'Festival Setup Locks',
    desc: 'One festival per calendar year, strictly enforced — every module stays locked until this year\'s setup exists.',
  },
  {
    icon: HandCoins,
    accent: 'maroon',
    title: 'Real-Time Collections & Expenses',
    desc: 'Every donation and expense logged instantly, scoped to the exact festival year it belongs to.',
  },
  {
    icon: Gavel,
    accent: 'gold',
    title: 'Live Auction Management',
    desc: 'Track Velampata items, winning bids, and payment status as the auction happens.',
  },
  {
    icon: Gift,
    accent: 'maroon',
    title: 'Sponsorship Dashboards',
    desc: 'General and Annadanam sponsors organized by category, day, and contribution.',
  },
  {
    icon: FileBarChart,
    accent: 'saffron',
    title: 'Historical Year Audit Reports',
    desc: 'Every past festival automatically archived into a complete, read-only audit report — surplus, deficit, and full ledger.',
  },
  {
    icon: ShieldCheck,
    accent: 'gold',
    title: 'Role-Based Access',
    desc: 'President, Treasurer, Secretary, and Volunteer accounts each see exactly what their role needs — nothing more.',
  },
]

const ACCENTS = {
  saffron: { badge: 'bg-saffron-500', bar: 'bg-saffron-500' },
  maroon: { badge: 'bg-maroon-700', bar: 'bg-maroon-700' },
  gold: { badge: 'bg-gold-500', bar: 'bg-gold-500' },
}

export default function Landing() {
  const navigate = useNavigate()
  const [code, setCode] = useState('')
  const [status, setStatus] = useState('idle') // idle | loading | invalid

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
      {/* Sticky header */}
      <header className="sticky top-0 z-40 bg-cream/80 backdrop-blur-md border-b border-saffron-100/80">
        <div className="max-w-6xl mx-auto px-4 md:px-8 py-3.5 flex items-center justify-between">
          <div className="flex items-center gap-2 text-maroon-800 font-display font-bold text-lg">
            <span className="text-2xl leading-none">🐘</span> Ganesh Utsav
          </div>
          <nav className="flex items-center gap-2 sm:gap-4">
            <a href="#public-search" className="hidden sm:inline text-sm font-medium text-maroon-600 hover:text-saffron-600 transition">
              Find a Committee
            </a>
            <Link to="/login" className="btn-secondary inline-flex items-center gap-2 !py-2 !px-4">
              <LogIn size={16} /> Log In
            </Link>
          </nav>
        </div>
      </header>

      {/* Hero */}
      <section className="relative overflow-hidden">
        <div className="max-w-6xl mx-auto px-4 md:px-8 pt-14 pb-16 md:pt-20 md:pb-24 grid grid-cols-1 lg:grid-cols-[1.1fr_0.9fr] gap-12 items-center">
          <div>
            <span className="badge bg-saffron-100 text-saffron-700 mb-5">Festival Committee Management</span>
            <h1 className="text-4xl md:text-5xl font-display font-bold text-maroon-800 leading-[1.1] mb-5 max-w-lg">
              Every Ganesh Utsav committee's money, tracked in one place.
            </h1>
            <p className="text-maroon-500 text-base md:text-lg mb-8 max-w-md leading-relaxed">
              Collections, expenses, auctions, and sponsorships — organized year by year, with a public
              transparency view every donor can trust.
            </p>
            <div className="flex flex-col sm:flex-row gap-3">
              <Link to="/login" className="btn-primary inline-flex items-center justify-center gap-2">
                <LogIn size={18} /> Log In as President / Admin
              </Link>
              <a href="#public-search" className="btn-secondary inline-flex items-center justify-center gap-2">
                <Search size={18} /> Find a Committee
              </a>
            </div>
          </div>

          {/* Product-grounded hero visual: a stylized receipt, not a decoration */}
          <div className="relative mx-auto w-full max-w-sm">
            <div className="absolute -inset-6 bg-gradient-to-br from-saffron-200/50 to-transparent rounded-[2rem] -z-10" />
            <div className="card !shadow-xl border-saffron-200/70 rotate-1">
              <div className="flex items-center gap-2 pb-3 mb-3 border-b border-dashed border-saffron-200">
                <Receipt size={18} className="text-saffron-500" />
                <span className="font-display font-semibold text-maroon-800 text-sm">Donation Receipt</span>
              </div>
              <dl className="space-y-2.5 text-sm">
                <div className="flex justify-between"><dt className="text-maroon-400">Receipt#</dt><dd className="font-mono text-maroon-700">GU-2026-84502</dd></div>
                <div className="flex justify-between"><dt className="text-maroon-400">Donor</dt><dd className="text-maroon-700 font-medium">Raghu Patil</dd></div>
                <div className="flex justify-between"><dt className="text-maroon-400">Mode</dt><dd className="text-maroon-700">Cash</dd></div>
                <div className="flex justify-between"><dt className="text-maroon-400">Date</dt><dd className="text-maroon-700">01 Sep 2026</dd></div>
              </dl>
              <div className="flex items-center justify-between mt-4 pt-4 border-t border-saffron-100">
                <span className="text-xs text-maroon-400">Amount Collected</span>
                <span className="text-2xl font-bold font-display text-saffron-600">₹300</span>
              </div>
            </div>
            <div className="card !shadow-lg border-saffron-200/70 absolute -bottom-6 -left-6 w-40 -rotate-3 hidden sm:block">
              <p className="text-xs text-maroon-400">Net Surplus</p>
              <p className="text-lg font-bold font-display text-maroon-800">₹42,180</p>
            </div>
          </div>
        </div>
      </section>

      {/* Feature highlights */}
      <section className="px-4 md:px-8 py-14 md:py-20 bg-white/60 border-y border-saffron-100/70">
        <div className="max-w-6xl mx-auto">
          <div className="max-w-lg mb-10">
            <h2 className="text-2xl md:text-3xl font-display font-bold text-maroon-800 mb-3">
              What the platform handles for you
            </h2>
            <p className="text-maroon-500">Every module a committee needs, scoped correctly and locked down by role.</p>
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
            {FEATURES.map(({ icon: Icon, title, desc, accent }) => (
              <div key={title} className="card relative overflow-hidden">
                <div className={`absolute top-0 left-0 right-0 h-1 ${ACCENTS[accent].bar}`} />
                <div className={`w-11 h-11 rounded-xl ${ACCENTS[accent].badge} flex items-center justify-center text-white mb-4`}>
                  <Icon size={20} />
                </div>
                <h3 className="font-display font-semibold text-maroon-800 mb-1.5">{title}</h3>
                <p className="text-sm text-maroon-500 leading-relaxed">{desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Public committee search */}
      <section id="public-search" className="px-4 md:px-8 py-14 md:py-20 scroll-mt-16">
        <div className="max-w-4xl mx-auto rounded-3xl overflow-hidden shadow-card grid grid-cols-1 md:grid-cols-[0.85fr_1.15fr]">
          <div className="bg-maroon-800 text-white p-8 md:p-10 flex flex-col justify-center">
            <Search size={28} className="text-saffron-300 mb-4" />
            <h2 className="text-xl md:text-2xl font-display font-bold mb-3">Find a committee</h2>
            <p className="text-maroon-100 text-sm leading-relaxed">
              Enter a committee's Ganesh Unique Code to view its live festival — donations, expenses, and
              sponsorships, all read-only. No login needed.
            </p>
          </div>
          <div className="bg-white p-8 md:p-10 flex flex-col justify-center">
            <form onSubmit={handleSearch} className="flex flex-col gap-3">
              <label className="label-text">Ganesh Unique Code</label>
              <input
                className="input-field font-mono"
                placeholder="e.g. GU-MH-PUN-0001"
                value={code}
                onChange={(e) => { setCode(e.target.value); setStatus('idle') }}
              />
              <button type="submit" className="btn-primary inline-flex items-center justify-center gap-2" disabled={status === 'loading'}>
                {status === 'loading' ? <><Loader2 size={16} className="animate-spin" /> Loading…</> : <><Search size={16} /> View Committee</>}
              </button>
            </form>
            {status === 'invalid' && (
              <p className="text-sm text-maroon-600 bg-maroon-50 rounded-lg px-3 py-2 mt-3">
                Invalid Code — no committee found with that Ganesh Unique Code. Double-check it and try again.
              </p>
            )}
          </div>
        </div>
      </section>

      <Footer />
    </div>
  )
}
