import React from 'react'
import { Link } from 'react-router-dom'
import PublicPageHeader from '../components/common/PublicPageHeader'
import { Sparkles } from 'lucide-react'
import Footer from '../components/common/Footer'

const TEAM = [
  {
    name: 'Anand',
    role: 'CEO & Founder',
    initials: 'A',
    accent: 'from-saffron-500 to-saffron-600',
    bio: 'Leads product direction and Lenvy Technologies\' work with festival committees and community organizations.',
  },
  {
    name: 'M Subhash',
    role: 'CTO & Co-Founder',
    initials: 'MS',
    accent: 'from-maroon-600 to-maroon-800',
    bio: 'Leads engineering — the platform\'s multi-tenant architecture, security, and day-to-day reliability.',
  },
]

export default function About() {
  return (
    <div className="min-h-screen flex flex-col bg-cream">
      <PublicPageHeader />

      <main className="flex-1 max-w-3xl mx-auto px-4 py-8 md:py-12 w-full">
        <span className="badge bg-saffron-100 text-saffron-700 mb-4">About Us</span>
        <h1 className="text-3xl md:text-4xl font-display font-bold text-maroon-800 leading-tight mb-4">
          Built by Lenvy Technologies
        </h1>
        <p className="text-maroon-500 text-base leading-relaxed mb-10 max-w-xl">
          Lenvy Technologies designs and builds this platform — every module from Festival Setup to the public
          transparency pages your donors see. Ganesh Utsav Platform is our answer to a problem every committee
          knows well: keeping a full year's collections, expenses, and sponsorships honest and easy to follow.
        </p>

        <h2 className="text-lg font-display font-bold text-maroon-800 mb-4">The team</h2>
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-5 mb-10">
          {TEAM.map(({ name, role, initials, accent, bio }) => (
            <div key={name} className="card">
              <div className={`w-14 h-14 rounded-full bg-gradient-to-br ${accent} flex items-center justify-center text-white font-display font-bold text-lg mb-4`}>
                {initials}
              </div>
              <h3 className="font-display font-semibold text-maroon-800">{name}</h3>
              <p className="text-sm text-saffron-600 font-medium mb-2">{role}</p>
              <p className="text-sm text-maroon-500 leading-relaxed">{bio}</p>
            </div>
          ))}
        </div>

        <div className="card bg-maroon-900 text-saffron-100 flex items-center gap-3">
          <Sparkles size={18} className="text-gold-400 shrink-0" />
          <p className="text-sm">
            Have a question for the team, or want Lenvy Technologies to build something for your organization?
            Reach out via the <Link to="/support" className="text-white underline underline-offset-2">Support</Link> page.
          </p>
        </div>
      </main>

      <Footer />
    </div>
  )
}
