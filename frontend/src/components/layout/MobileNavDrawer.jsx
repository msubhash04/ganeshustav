import React, { useEffect } from 'react'

/**
 * A true overlay/drawer for mobile navigation - fixed on top of the page,
 * never part of the document flow, so it can never push page content
 * down or reflow the layout underneath it (unlike the previous
 * implementation, which rendered the nav inline right below the header).
 *
 * - position: fixed + h-screen (100vh) + a high z-index (above the sticky
 *   header and the Tenant Inspection banner) so it always floats cleanly
 *   above everything else.
 * - A semi-transparent backdrop sits behind the panel; clicking it closes
 *   the drawer.
 * - Body scroll is locked while open, restored on close/unmount.
 * - The panel is ALWAYS mounted (just translated off-screen when closed)
 *   so the closing slide-out transition actually gets to play, instead
 *   of the drawer just vanishing instantly.
 */
export default function MobileNavDrawer({ open, onClose, children, panelClassName = 'bg-maroon-800 text-white', side = 'right' }) {
  useEffect(() => {
    if (open) {
      const previousOverflow = document.body.style.overflow
      document.body.style.overflow = 'hidden'
      return () => { document.body.style.overflow = previousOverflow }
    }
  }, [open])

  const sideClass = side === 'right' ? 'right-0' : 'left-0'
  const closedTransform = side === 'right' ? 'translate-x-full' : '-translate-x-full'

  return (
    <>
      {/* Backdrop - dims the page behind the drawer, tap to close */}
      <div
        onClick={onClose}
        aria-hidden="true"
        className={`md:hidden fixed inset-0 z-40 bg-black/50 transition-opacity duration-300 ease-in-out ${
          open ? 'opacity-100 pointer-events-auto' : 'opacity-0 pointer-events-none'
        }`}
      />

      {/* Sliding panel */}
      <div
        role="dialog"
        aria-modal="true"
        className={`md:hidden fixed top-0 ${sideClass} z-50 h-screen w-72 max-w-[85vw] overflow-y-auto shadow-2xl
                    transition-transform duration-300 ease-in-out ${panelClassName}
                    ${open ? 'translate-x-0' : closedTransform}`}
      >
        {children}
      </div>
    </>
  )
}
