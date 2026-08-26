import React from 'react'
import { Pencil, Trash2, Paperclip } from 'lucide-react'
import { formatINR, formatDate } from '../../utils/format'
import { EXPENSE_CATEGORIES } from '../../api/expenseApi'

const categoryLabel = (value) => EXPENSE_CATEGORIES.find((c) => c.value === value)?.label || value

export default function ExpenseTable({ expenses, onEdit, onDelete }) {
  if (!expenses || expenses.length === 0) {
    return <p className="text-sm text-maroon-400 py-8 text-center">No expenses found. Try adjusting your filters or add a new one.</p>
  }

  return (
    <div className="overflow-x-auto">
      <table className="w-full text-sm">
        <thead>
          <tr className="text-left text-maroon-500 border-b border-saffron-100">
            <th className="py-2 pr-4">Description</th>
            <th className="py-2 pr-4">Category</th>
            <th className="py-2 pr-4">Paid To</th>
            <th className="py-2 pr-4">Date</th>
            <th className="py-2 pr-4 text-right">Amount</th>
            <th className="py-2 pr-4 text-right">Actions</th>
          </tr>
        </thead>
        <tbody>
          {expenses.map((e) => (
            <tr key={e.id} className="border-b border-saffron-50 last:border-0 hover:bg-saffron-50/50">
              <td className="py-2.5 pr-4 font-medium text-maroon-800">
                <div className="flex items-center gap-1.5">
                  {e.description}
                  {e.billFilePath && <Paperclip size={13} className="text-maroon-400" />}
                </div>
              </td>
              <td className="py-2.5 pr-4">
                <span className="badge bg-maroon-100 text-maroon-700">{categoryLabel(e.category)}</span>
              </td>
              <td className="py-2.5 pr-4 text-maroon-500">{e.paidTo}</td>
              <td className="py-2.5 pr-4 text-maroon-500">{formatDate(e.expenseDate)}</td>
              <td className="py-2.5 pr-4 text-right font-semibold text-maroon-700">{formatINR(e.amount)}</td>
              <td className="py-2.5 pr-4">
                <div className="flex justify-end gap-2">
                  <button onClick={() => onEdit(e)} className="text-maroon-400 hover:text-saffron-600" title="Edit">
                    <Pencil size={16} />
                  </button>
                  <button onClick={() => onDelete(e)} className="text-maroon-400 hover:text-maroon-700" title="Delete">
                    <Trash2 size={16} />
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
