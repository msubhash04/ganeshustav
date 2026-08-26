import React from 'react'

export default function SummaryCard({ title, value, icon: Icon, tone = 'saffron' }) {
  const tones = {
    saffron: 'from-saffron-500 to-saffron-600',
    maroon: 'from-maroon-600 to-maroon-700',
    gold: 'from-gold-500 to-gold-600',
  }

  return (
    <div className="card flex items-center gap-4">
      <div className={`w-12 h-12 rounded-xl bg-gradient-to-br ${tones[tone]} flex items-center justify-center text-white shrink-0`}>
        {Icon && <Icon size={22} />}
      </div>
      <div className="min-w-0">
        <p className="text-sm text-maroon-500 font-medium truncate">{title}</p>
        <p className="text-xl md:text-2xl font-bold text-maroon-800 truncate">{value}</p>
      </div>
    </div>
  )
}
