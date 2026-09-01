import React from 'react'
import { Link } from 'react-router-dom'
import { ArrowLeft } from 'lucide-react'
import Footer from '../components/common/Footer'

export default function PrivacyPolicy() {
  return (
    <div className="min-h-screen flex flex-col bg-cream">
      <header className="px-4 md:px-8 py-4 flex items-center justify-between">
        <Link to="/" className="inline-flex items-center gap-1.5 text-sm text-maroon-500 hover:text-maroon-700 transition">
          <ArrowLeft size={16} /> Back to Home
        </Link>
        <div className="flex items-center gap-2 text-maroon-800 font-display font-bold">
          <span className="text-xl">🐘</span> Ganesh Utsav
        </div>
      </header>

      <main className="flex-1 max-w-2xl mx-auto px-4 py-8 w-full">
        <h1 className="page-title mb-1">Privacy Policy</h1>
        <p className="text-xs text-maroon-400 mb-6">Last updated: {new Date().getFullYear()}</p>

        <div className="card space-y-5 text-sm text-maroon-600 leading-relaxed">
          <section>
            <h2 className="font-semibold text-maroon-800 mb-1.5">What we collect</h2>
            <p>
              Committee members provide names, phone numbers, and login credentials to manage their own
              committee's records. Donors and sponsors provide names, phone numbers, and contribution amounts
              when a committee records a donation, sponsorship, or auction payment on their behalf.
            </p>
          </section>
          <section>
            <h2 className="font-semibold text-maroon-800 mb-1.5">What the public can see</h2>
            <p>
              Public transparency and observation pages (reachable via a committee's Ganesh Unique Code) only
              ever show aggregate totals — total collections, total expenses, category breakdowns, and surplus
              or deficit. Donor names, phone numbers, receipt numbers, and other itemized ledger details are
              never shown on any public, unauthenticated page.
            </p>
          </section>
          <section>
            <h2 className="font-semibold text-maroon-800 mb-1.5">Who can see itemized records</h2>
            <p>
              Full itemized records (donor names, phone numbers, individual transactions) are visible only to
              that committee's own logged-in President, Treasurer, Secretary, and Volunteer accounts, and to the
              platform's Developer administrators for support and auditing purposes — every such access is
              logged.
            </p>
          </section>
          <section>
            <h2 className="font-semibold text-maroon-800 mb-1.5">Data retention</h2>
            <p>
              Festival year records are archived, not deleted, once a new festival year begins — this preserves
              each committee's historical audit trail for transparency with its contributors.
            </p>
          </section>
          <section>
            <h2 className="font-semibold text-maroon-800 mb-1.5">Questions</h2>
            <p>
              For any privacy question or data request, reach out via the <Link to="/support" className="text-saffron-600 hover:underline">Support</Link> page.
            </p>
          </section>
        </div>
      </main>

      <Footer />
    </div>
  )
}
