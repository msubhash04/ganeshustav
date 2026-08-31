import React, { useState } from 'react'
import { Gift, Tag, Users2, Soup } from 'lucide-react'
import Layout from '../components/layout/Layout'
import FestivalYearGate from '../components/common/FestivalYearGate'
import { useAuth } from '../context/AuthContext'
import CategoriesTab from '../components/sponsorships/CategoriesTab'
import GeneralSponsorsTab from '../components/sponsorships/GeneralSponsorsTab'
import AnnadanamSponsorsTab from '../components/sponsorships/AnnadanamSponsorsTab'

const TABS = [
  { key: 'categories', label: 'Categories', icon: Tag },
  { key: 'general', label: 'General Sponsors', icon: Users2 },
  { key: 'annadanam', label: 'Annadanam Sponsors', icon: Soup },
]

export default function Sponsorships() {
  const { user } = useAuth()
  const isPresident = user?.role === 'PRESIDENT'
  const [activeTab, setActiveTab] = useState('categories')

  if (!isPresident) {
    return (
      <Layout>
        <h1 className="page-title mb-6">Sponsorship Management</h1>
        <div className="card text-center py-10">
          <Gift className="mx-auto text-saffron-400 mb-2" size={32} />
          <p className="text-maroon-500">Sponsorship management is restricted to the President.</p>
        </div>
      </Layout>
    )
  }

  return (
    <Layout>
      <h1 className="page-title mb-6">Sponsorship Management</h1>

      <FestivalYearGate>
        {/* Tab bar - each tab is a distinct section per the module spec:
            Categories master page, General Sponsors, and a dedicated
            Annadanam Sponsors page tracked separately by festival day. */}
        <div className="flex gap-2 mb-6 border-b border-saffron-100 overflow-x-auto">
          {TABS.map(({ key, label, icon: Icon }) => (
            <button
              key={key}
              onClick={() => setActiveTab(key)}
              className={`flex items-center gap-2 px-4 py-2.5 text-sm font-medium border-b-2 whitespace-nowrap shrink-0 transition ${
                activeTab === key
                  ? 'border-saffron-500 text-saffron-600'
                  : 'border-transparent text-maroon-400 hover:text-maroon-600'
              }`}
            >
              <Icon size={16} /> {label}
            </button>
          ))}
        </div>

        <div className="card">
          {activeTab === 'categories' && <CategoriesTab />}
          {activeTab === 'general' && <GeneralSponsorsTab />}
          {activeTab === 'annadanam' && <AnnadanamSponsorsTab />}
        </div>
      </FestivalYearGate>
    </Layout>
  )
}
