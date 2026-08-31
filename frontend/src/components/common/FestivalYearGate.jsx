import React, { useEffect, useState } from 'react'
import { Lock, CalendarPlus } from 'lucide-react'
import { Link } from 'react-router-dom'
import { festivalYearApi } from '../../api/festivalYearApi'
import { useAuth } from '../../context/AuthContext'

/**
 * Active Festival Guard - wraps the content of every festival-year-scoped
 * page (Collections, Expenses, Auction, Sponsorships, Reports). Renders
 * children only once an active festival year exists for the committee;
 * otherwise shows a clear locked message instead, per the business rule:
 * "First create the Festival year to unlock these features."
 *
 * Deliberately wraps just the page BODY, not <Layout> itself - the
 * sidebar/nav still renders normally so the person isn't stranded, and
 * Layout separately shows a lock icon next to these nav items (see
 * Layout.jsx) so the restriction is visible before they even click in.
 *
 * This does not replace or alter each page's own data-fetching logic -
 * it only controls what's rendered, matching the "wrap, don't rewrite"
 * instruction. The backend (FestivalYearGuard) is the actual source of
 * truth and independently rejects any write against a missing/archived
 * festival year regardless of what the UI shows.
 */
export default function FestivalYearGate({ children }) {
  const { user } = useAuth()
  const isPresident = user?.role === 'PRESIDENT'
  const [status, setStatus] = useState('loading') // 'loading' | 'unlocked' | 'locked'

  useEffect(() => {
    let cancelled = false
    festivalYearApi.getActive()
      .then((year) => { if (!cancelled) setStatus(year ? 'unlocked' : 'locked') })
      .catch(() => { if (!cancelled) setStatus('locked') })
    return () => { cancelled = true }
  }, [])

  if (status === 'loading') {
    return <p className="text-maroon-400">Loading…</p>
  }

  if (status === 'locked') {
    return (
      <div className="card text-center py-12 max-w-lg mx-auto">
        <Lock className="mx-auto text-saffron-400 mb-3" size={32} />
        <p className="text-maroon-700 font-semibold text-lg mb-1.5">
          First create the Festival year to unlock these features.
        </p>
        <p className="text-sm text-maroon-500 mb-5">
          {isPresident
            ? "Set up this year's festival details to start recording collections, expenses, sponsorships, auctions, and reports."
            : "Ask your committee's President to set up this year's Festival before this section becomes available."}
        </p>
        {isPresident && (
          <Link to="/festival-setup" className="btn-primary inline-flex items-center gap-2">
            <CalendarPlus size={18} /> Go to Festival Setup
          </Link>
        )}
      </div>
    )
  }

  return children
}
