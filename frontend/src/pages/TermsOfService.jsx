import React from 'react'
import { Link } from 'react-router-dom'
import { ArrowLeft } from 'lucide-react'
import Footer from '../components/common/Footer'

export default function TermsOfService() {
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
        <h1 className="page-title mb-1">Terms of Service</h1>
        <p className="text-xs text-maroon-400 mb-6">Last updated: {new Date().getFullYear()}</p>

        <div className="card space-y-5 text-sm text-maroon-600 leading-relaxed">
          <section>
            <h2 className="font-semibold text-maroon-800 mb-1.5">Who this platform is for</h2>
            <p>
              This platform helps Ganesh Utsav committees track collections, expenses, sponsorships, auctions,
              and post-festival loans for a single festival year at a time, and publish a transparency view for
              their contributors.
            </p>
          </section>
          <section>
            <h2 className="font-semibold text-maroon-800 mb-1.5">Committee responsibilities</h2>
            <p>
              Each committee's President is responsible for accurately recording their committee's financial
              activity and for managing which staff (Treasurer, Secretary, Volunteer) have access to their
              account. Staff credentials should never be shared outside the committee.
            </p>
          </section>
          <section>
            <h2 className="font-semibold text-maroon-800 mb-1.5">One festival year at a time</h2>
            <p>
              A committee may only have one active festival year at a time, strictly scoped to the current
              calendar year. Creating a new year's festival automatically archives the previous year — archived
              years become read-only and cannot accept new records.
            </p>
          </section>
          <section>
            <h2 className="font-semibold text-maroon-800 mb-1.5">Public data</h2>
            <p>
              By using the public transparency and observation features, a committee agrees that its aggregate
              financial totals (never itemized donor or transaction details) may be viewed by anyone with its
              Ganesh Unique Code, without requiring a login.
            </p>
          </section>
          <section>
            <h2 className="font-semibold text-maroon-800 mb-1.5">No warranty</h2>
            <p>
              This platform is provided as-is, without warranty of any kind. Committees remain solely responsible
              for the accuracy of the financial records they enter.
            </p>
          </section>
          <section>
            <h2 className="font-semibold text-maroon-800 mb-1.5">Questions</h2>
            <p>
              For any question about these terms, reach out via the <Link to="/support" className="text-saffron-600 hover:underline">Support</Link> page.
            </p>
          </section>
        </div>
      </main>

      <Footer />
    </div>
  )
}
