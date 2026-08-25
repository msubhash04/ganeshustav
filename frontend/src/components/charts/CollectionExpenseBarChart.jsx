import React from 'react'
import { BarChart, Bar, XAxis, YAxis, Tooltip, Legend, ResponsiveContainer, CartesianGrid } from 'recharts'
import { formatINR } from '../../utils/format'

export default function CollectionExpenseBarChart({ data }) {
  if (!data || data.length === 0) {
    return <p className="text-sm text-maroon-400 text-center py-10">No transactions yet.</p>
  }

  return (
    <ResponsiveContainer width="100%" height={300}>
      <BarChart data={data} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
        <CartesianGrid strokeDasharray="3 3" stroke="#ffe4c4" />
        <XAxis dataKey="period" tick={{ fontSize: 12 }} />
        <YAxis tick={{ fontSize: 12 }} tickFormatter={(v) => `₹${v / 1000}k`} />
        <Tooltip formatter={(value) => formatINR(value)} />
        <Legend wrapperStyle={{ fontSize: 12 }} />
        <Bar dataKey="collections" name="Collections" fill="#ff9933" radius={[6, 6, 0, 0]} />
        <Bar dataKey="expenses" name="Expenses" fill="#7a1f18" radius={[6, 6, 0, 0]} />
      </BarChart>
    </ResponsiveContainer>
  )
}
