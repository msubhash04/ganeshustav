import React from 'react'
import { Link } from 'react-router-dom'
import { ArrowLeft } from 'lucide-react'

// Shared by every public, unauthenticated page (Observation Dashboard,
// Privacy, Terms, Support, About). Logo on the left, a compact icon-only
// "back" button on the right - on a narrow phone screen a full "← Back
// to Home" text link and a "Ganesh Utsav" logo compete for the same row
// and can end up looking misaligned; two fixed anchors (a small round
// button and a short logo) hold their positions cleanly at any width.
export default function PublicPageHeader() {
  return (
    <header className="px-4 md:px-8 py-4 flex items-center justify-between">
      <div className="flex items-center gap-2 text-maroon-800 font-display font-bold">
        <span className="text-xl">🐘</span> Ganesh Utsav
      </div>
      <Link
        to="/"
        aria-label="Back to Home"
        title="Back to Home"
        className="group inline-flex items-center justify-center w-11 h-11 rounded-full bg-white border border-saffron-200
                   text-maroon-600 shadow-card hover:bg-saffron-50 hover:border-saffron-300 hover:scale-105 active:scale-95
                   transition-all animate-heartbeat hover:animate-none"
      >
        <ArrowLeft size={19} className="group-hover:-translate-x-0.5 transition-transform" />
      </Link>
    </header>
  )
}
