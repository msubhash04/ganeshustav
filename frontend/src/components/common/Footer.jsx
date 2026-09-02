import React from 'react'
import { Link } from 'react-router-dom'
import { Sparkles, Mail, MapPin } from 'lucide-react'

const LINK_GROUPS = [
  {
    heading: 'Get Started',
    links: [
      { to: '/login', label: 'Log In' },
      { to: '/#public-search', label: 'Public Search' },
    ],
  },
  {
    heading: 'Legal',
    links: [
      { to: '/privacy', label: 'Privacy Policy' },
      { to: '/terms', label: 'Terms of Service' },
    ],
  },
  {
    heading: 'Company',
    links: [
      { to: '/about', label: 'About Us' },
      { to: '/support', label: 'Support' },
    ],
  },
]

export default function Footer() {
  const year = new Date().getFullYear()
  return (
    <footer className="mt-auto bg-maroon-900 text-saffron-100">
      <div className="h-1 bg-gradient-to-r from-saffron-500 via-gold-500 to-maroon-600" />
      <div className="max-w-6xl mx-auto px-4 md:px-8 py-12 grid grid-cols-2 sm:grid-cols-4 gap-8">
        <div className="col-span-2 sm:col-span-1">
          <div className="flex items-center gap-2 text-white font-display font-bold text-lg mb-2">
            <span className="text-xl">🐘</span> Ganesh Utsav
          </div>
          <p className="text-sm text-saffron-200/80 leading-relaxed max-w-[22ch]">
            One home for every committee's collections, expenses, and transparency.
          </p>
        </div>
        {LINK_GROUPS.map(({ heading, links }) => (
          <div key={heading}>
            <p className="text-xs font-semibold text-saffron-300/70 mb-3">{heading}</p>
            <ul className="space-y-2.5">
              {links.map(({ to, label }) => (
                <li key={label}>
                  <Link to={to} className="text-sm text-saffron-100/90 hover:text-white transition">{label}</Link>
                </li>
              ))}
            </ul>
          </div>
        ))}
      </div>

      {/* Lenvy Technologies contact - the platform's developer, reachable
          directly for support requests that need a human */}
      <div className="border-t border-white/10">
        <div className="max-w-6xl mx-auto px-4 md:px-8 py-6 flex flex-col sm:flex-row sm:items-center gap-4 sm:gap-8">
          <a
            href="mailto:help@lenvytechnologies.in"
            className="inline-flex items-center gap-2 px-4 py-2 rounded-full border border-white/20 text-sm font-medium text-white hover:bg-white/10 transition w-fit"
          >
            <Mail size={15} className="text-saffron-300" /> help@lenvytechnologies.in
          </a>
          <p className="flex items-start gap-2 text-sm text-saffron-200/80 leading-relaxed">
            <MapPin size={15} className="text-saffron-300 mt-0.5 shrink-0" />
            7th Floor, Summit B, Brigade Metropolis, Whitefield Main Rd, Mahadevapura, Bengaluru, Karnataka 560048, India
          </p>
        </div>
      </div>

      <div className="border-t border-white/10">
        <div className="max-w-6xl mx-auto px-4 md:px-8 py-5 flex flex-col sm:flex-row sm:items-center justify-between gap-3 text-xs text-saffron-200/70">
          <p>© {year} Ganesh Utsav Platform. All rights reserved.</p>
          <Link
            to="/about"
            className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-white/10 text-white font-medium w-fit hover:bg-white/15 transition"
          >
            <Sparkles size={12} className="text-gold-400" /> Designed &amp; Developed by Lenvy Technologies
          </Link>
        </div>
      </div>
    </footer>
  )
}
