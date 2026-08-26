import React from 'react'
import { Routes, Route } from 'react-router-dom'
import ProtectedRoute from './components/common/ProtectedRoute'
import Login from './pages/Login'
import Dashboard from './pages/Dashboard'
import Collections from './pages/Collections'
import Expenses from './pages/Expenses'
import Reports from './pages/Reports'
import Members from './pages/Members'
import PublicTransparency from './pages/PublicTransparency'
import FestivalSetup from './pages/FestivalSetup'
import Auction from './pages/Auction'
import Loans from './pages/Loans'
import Sponsorships from './pages/Sponsorships'
import DeveloperDashboard from './pages/DeveloperDashboard'
import Committees from './pages/Committees'
import { useAuth } from './context/AuthContext'

// The Developer (Super Admin) role has no committee, so every
// committee-scoped page below would 403 for them. RoleRoot sends a
// Developer straight to their own dashboard instead of the normal one.
function RoleRoot() {
  const { user } = useAuth()
  return user?.role === 'DEVELOPER' ? <DeveloperDashboard /> : <Dashboard />
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/public/:tenantCode" element={<PublicTransparency />} />

      <Route path="/" element={<ProtectedRoute><RoleRoot /></ProtectedRoute>} />
      <Route path="/committees" element={<ProtectedRoute><Committees /></ProtectedRoute>} />
      <Route path="/festival-setup" element={<ProtectedRoute><FestivalSetup /></ProtectedRoute>} />
      <Route path="/collections" element={<ProtectedRoute><Collections /></ProtectedRoute>} />
      <Route path="/expenses" element={<ProtectedRoute><Expenses /></ProtectedRoute>} />
      <Route path="/sponsorships" element={<ProtectedRoute><Sponsorships /></ProtectedRoute>} />
      <Route path="/auction" element={<ProtectedRoute><Auction /></ProtectedRoute>} />
      <Route path="/loans" element={<ProtectedRoute><Loans /></ProtectedRoute>} />
      <Route path="/reports" element={<ProtectedRoute><Reports /></ProtectedRoute>} />
      <Route path="/members" element={<ProtectedRoute><Members /></ProtectedRoute>} />
    </Routes>
  )
}
