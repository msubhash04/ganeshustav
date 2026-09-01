import React from 'react'
import { Link } from 'react-router-dom'
import { Sparkles } from 'lucide-react'

export default function Footer() {
  const year = new Date().getFullYear()
  return (
    <footer className="border-t border-saffron-100 bg-white/60 mt-auto">
      <div className="max-w-5xl mx-auto px-4 py-8">
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-6">
          <div className="flex items-center gap-2 text-maroon-700 font-display font-bold">
            <span className="text-xl">🐘</span> Ganesh Utsav Platform
          </div>
          <nav className="flex flex-wrap gap-x-6 gap-y-2 text-sm text-maroon-500">
            <Link to="/#public-search" className="hover:text-saffron-600 transition">Public Search</Link>
            <Link to="/privacy" className="hover:text-saffron-600 transition">Privacy Policy</Link>
            <Link to="/terms" className="hover:text-saffron-600 transition">Terms of Service</Link>
            <Link to="/support" className="hover:text-saffron-600 transition">Support</Link>
          </nav>
        </div>
        <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3 pt-6 border-t border-saffron-100 text-xs text-maroon-400">
          <p>© {year} Festival Committee Management Platform. All rights reserved.</p>
          <a
            href="#"
            onClick={(e) => e.preventDefault()}
            className="inline-flex items-center gap-1.5 px-3 py-1 rounded-full bg-gradient-to-r from-saffron-500 to-maroon-600 text-white font-medium"
          >
            <Sparkles size={12} /> Designed &amp; Developed by Lenvy Technologies
          </a>
        </div>
      </div>
    </footer>
  )
}
