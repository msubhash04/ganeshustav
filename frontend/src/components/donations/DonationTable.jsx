import React from 'react'
import { Printer, Pencil, Trash2 } from 'lucide-react'
import { formatINR, formatDate } from '../../utils/format'
import ResponsiveTable, { TableCard, CardActions, CardActionButton } from '../common/ResponsiveTable'

const MODE_STYLES = {
  CASH: 'bg-gold-500/10 text-gold-600',
  UPI: 'bg-saffron-100 text-saffron-700',
  BANK_TRANSFER: 'bg-maroon-100 text-maroon-700',
  CHEQUE: 'bg-blue-100 text-blue-700',
}

export default function DonationTable({ donations, onEdit, onDelete, onPrint }) {
  return (
    <ResponsiveTable
      data={donations}
      emptyMessage="No donations found. Try adjusting your filters or add a new one."
      renderCard={(d) => (
        <TableCard>
          <div className="flex items-start justify-between gap-3">
            <div className="min-w-0">
              <p className="font-semibold text-maroon-800 truncate">{d.donorName}</p>
              <p className="text-xs text-maroon-400 font-mono">{d.receiptNumber}</p>
            </div>
            <p className="text-lg font-bold text-saffron-600 shrink-0">{formatINR(d.amount)}</p>
          </div>
          <div className="flex flex-wrap items-center gap-2 mt-2.5">
            <span className={`badge ${MODE_STYLES[d.paymentMode] || 'bg-gray-100 text-gray-700'}`}>
              {d.paymentMode?.replace('_', ' ')}
            </span>
            <span className="text-xs text-maroon-500">{formatDate(d.donationDate)}</span>
            {d.phoneNumber && <span className="text-xs text-maroon-500">{d.phoneNumber}</span>}
          </div>
          <CardActions>
            <CardActionButton onClick={() => onPrint(d)} icon={Printer} label="Print" />
            <CardActionButton onClick={() => onEdit(d)} icon={Pencil} label="Edit" />
            <CardActionButton onClick={() => onDelete(d)} icon={Trash2} label="Delete" tone="danger" />
          </CardActions>
        </TableCard>
      )}
    >
      <table className="w-full text-sm">
        <thead>
          <tr className="text-left text-maroon-500 border-b border-saffron-100">
            <th className="py-2 pr-4">Receipt#</th>
            <th className="py-2 pr-4">Donor</th>
            <th className="py-2 pr-4">Phone</th>
            <th className="py-2 pr-4">Date</th>
            <th className="py-2 pr-4">Mode</th>
            <th className="py-2 pr-4 text-right">Amount</th>
            <th className="py-2 pr-4 text-right">Actions</th>
          </tr>
        </thead>
        <tbody>
          {donations.map((d) => (
            <tr key={d.id} className="border-b border-saffron-50 last:border-0 hover:bg-saffron-50/50">
              <td className="py-2.5 pr-4 text-maroon-500">{d.receiptNumber}</td>
              <td className="py-2.5 pr-4 font-medium text-maroon-800">{d.donorName}</td>
              <td className="py-2.5 pr-4 text-maroon-500">{d.phoneNumber}</td>
              <td className="py-2.5 pr-4 text-maroon-500">{formatDate(d.donationDate)}</td>
              <td className="py-2.5 pr-4">
                <span className={`badge ${MODE_STYLES[d.paymentMode] || 'bg-gray-100 text-gray-700'}`}>
                  {d.paymentMode?.replace('_', ' ')}
                </span>
              </td>
              <td className="py-2.5 pr-4 text-right font-semibold text-saffron-600">{formatINR(d.amount)}</td>
              <td className="py-2.5 pr-4">
                <div className="flex justify-end gap-2">
                  <button onClick={() => onPrint(d)} className="text-maroon-400 hover:text-saffron-600" title="Print receipt">
                    <Printer size={16} />
                  </button>
                  <button onClick={() => onEdit(d)} className="text-maroon-400 hover:text-saffron-600" title="Edit">
                    <Pencil size={16} />
                  </button>
                  <button onClick={() => onDelete(d)} className="text-maroon-400 hover:text-maroon-700" title="Delete">
                    <Trash2 size={16} />
                  </button>
                </div>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </ResponsiveTable>
  )
}
