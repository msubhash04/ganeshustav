import React from 'react'

/**
 * Wraps a data table so it behaves as a proper <table> from the `sm:`
 * breakpoint up, and as a stacked list of cards below it. Used across
 * every data listing in the app (Donations, Expenses, Auction, Members,
 * Committees, Loans, Sponsorships, Inspection Log) to avoid the classic
 * mobile failure mode: a wide table squeezed into a narrow horizontal-
 * scroll strip, with cell text wrapping into three lines and headers
 * getting cut off mid-word.
 *
 * Usage:
 *   <ResponsiveTable data={rows} emptyMessage="..." renderCard={(row) => (...)}>
 *     <table>...the exact same table markup you'd write normally...</table>
 *   </ResponsiveTable>
 *
 * `renderCard` gets full control of the mobile markup per row - this is
 * deliberately NOT auto-generated from the table columns, because a good
 * mobile card usually reorders/emphasizes fields differently than a
 * table row does (e.g. name + amount as the header line, everything
 * else as supporting detail).
 */
export default function ResponsiveTable({ data, keyField = 'id', emptyMessage, renderCard, children }) {
  if (!data || data.length === 0) {
    return <p className="text-sm text-maroon-400 py-8 text-center">{emptyMessage}</p>
  }

  return (
    <>
      <div className="sm:hidden space-y-3">
        {data.map((row) => (
          <React.Fragment key={row[keyField]}>{renderCard(row)}</React.Fragment>
        ))}
      </div>
      <div className="hidden sm:block overflow-x-auto">
        {children}
      </div>
    </>
  )
}

/** Shared card shell so every mobile row looks consistent app-wide. */
export function TableCard({ children, className = '' }) {
  return <div className={`rounded-xl border border-saffron-100 p-3.5 ${className}`}>{children}</div>
}

/** A single label/value line inside a TableCard's detail area. */
export function CardField({ label, value, className = '' }) {
  return (
    <div className={`flex items-baseline justify-between gap-3 ${className}`}>
      <span className="text-xs text-maroon-400 shrink-0">{label}</span>
      <span className="text-sm text-maroon-700 text-right truncate">{value}</span>
    </div>
  )
}

/** Row of icon+label action buttons along the bottom of a TableCard. */
export function CardActions({ children }) {
  return <div className="flex items-center gap-4 mt-3 pt-3 border-t border-saffron-50">{children}</div>
}

export function CardActionButton({ onClick, icon: Icon, label, tone = 'default' }) {
  const toneClass = tone === 'danger' ? 'text-maroon-600' : 'text-maroon-500'
  return (
    <button onClick={onClick} className={`flex items-center gap-1.5 text-xs font-medium ${toneClass}`}>
      <Icon size={14} /> {label}
    </button>
  )
}
