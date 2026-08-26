import React from 'react'
import { PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer } from 'recharts'
import { formatINR } from '../../utils/format'

const COLORS = ['#ff9933', '#7a1f18', '#d4af37', '#c95a0c', '#a52a20', '#e8c468', '#833c12', '#dd5f52', '#f2760f', '#5c1712']

export default function ExpensePieChart({ data }) {
  const chartData = Object.entries(data || {}).map(([name, value]) => ({ name, value: Number(value) }))

  if (chartData.length === 0) {
    return <p className="text-sm text-maroon-400 text-center py-10">No expenses recorded yet.</p>
  }

  return (
    <ResponsiveContainer width="100%" height={300}>
      <PieChart>
        <Pie
          data={chartData}
          dataKey="value"
          nameKey="name"
          cx="50%"
          cy="50%"
          outerRadius={100}
          label={({ percent }) => `${(percent * 100).toFixed(0)}%`}
        >
          {chartData.map((_, index) => (
            <Cell key={index} fill={COLORS[index % COLORS.length]} />
          ))}
        </Pie>
        <Tooltip formatter={(value) => formatINR(value)} />
        <Legend wrapperStyle={{ fontSize: 12 }} />
      </PieChart>
    </ResponsiveContainer>
  )
}
