import React from 'react'
import { X } from 'lucide-react'

export default function Modal({ open, onClose, title, children, maxWidth = 'max-w-lg' }) {
  if (!open) return null
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-maroon-900/40 backdrop-blur-sm">
      <div className={`bg-white rounded-2xl shadow-xl w-full ${maxWidth} max-h-[90vh] overflow-y-auto`}>
        <div className="flex items-center justify-between px-5 py-4 border-b border-saffron-100 sticky top-0 bg-white rounded-t-2xl">
          <h3 className="text-lg font-semibold text-maroon-800">{title}</h3>
          <button onClick={onClose} className="text-maroon-400 hover:text-maroon-700 transition">
            <X size={20} />
          </button>
        </div>
        <div className="p-5">{children}</div>
      </div>
    </div>
  )
}
