import React from 'react'
import { formatINR, formatDate } from '../../utils/format'

export default function Receipt({ donation, festivalName = 'Ganesh Utsav Committee' }) {
  if (!donation) return null

  return (
    <div id="printable-receipt" className="border-2 border-saffron-400 rounded-xl p-6 bg-white">
      <div className="text-center mb-4">
        <div className="text-3xl mb-1">🐘</div>
        <h2 className="text-lg font-bold text-maroon-800">{festivalName}</h2>
        <p className="text-xs text-maroon-500">Donation Receipt</p>
      </div>

      <div className="flex justify-between text-sm mb-3">
        <span className="text-maroon-500">Receipt No.</span>
        <span className="font-semibold text-maroon-800">{donation.receiptNumber}</span>
      </div>
      <div className="flex justify-between text-sm mb-3">
        <span className="text-maroon-500">Date</span>
        <span className="font-semibold text-maroon-800">{formatDate(donation.donationDate)}</span>
      </div>

      <hr className="border-saffron-200 my-3" />

      <div className="space-y-2 text-sm">
        <Row label="Donor Name" value={donation.donorName} />
        <Row label="Phone Number" value={donation.phoneNumber} />
        {donation.address && <Row label="Address" value={donation.address} />}
        <Row label="Payment Mode" value={donation.paymentMode?.replace('_', ' ')} />
      </div>

      <hr className="border-saffron-200 my-3" />

      <div className="flex justify-between items-center">
        <span className="font-semibold text-maroon-700">Amount Received</span>
        <span className="text-xl font-bold text-saffron-600">{formatINR(donation.amount)}</span>
      </div>

      <p className="text-center text-xs text-maroon-400 mt-5">
        Thank you for your generous contribution towards Ganesh Utsav celebrations. 🙏
      </p>
    </div>
  )
}

function Row({ label, value }) {
  return (
    <div className="flex justify-between">
      <span className="text-maroon-500">{label}</span>
      <span className="text-maroon-800 font-medium text-right">{value}</span>
    </div>
  )
}
