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
import InspectionLog from './pages/InspectionLog'
import FestivalArchives from './pages/FestivalArchives'
import Landing from './pages/Landing'
import PublicObserve from './pages/PublicObserve'
import PrivacyPolicy from './pages/PrivacyPolicy'
import TermsOfService from './pages/TermsOfService'
import Support from './pages/Support'
import { useAuth } from './context/AuthContext'

// The Developer (Super Admin) role has no committee, so every
// committee-scoped page below would 403 for them. RoleRoot sends a
// Developer straight to their own dashboard instead of the normal one.
function RoleRoot() {
  const { user } = useAuth()
  return user?.role === 'DEVELOPER' ? <DeveloperDashboard /> : <Dashboard />
}

// "/" is the public Landing page for a logged-out visitor, and the
// normal authenticated dashboard for anyone already logged in - exactly
// what it always was for logged-in users (no ProtectedRoute redirect-to-
// login here), just with a real homepage for everyone else instead of
// being force-redirected to /login.
function RootRoute() {
  const { isAuthenticated } = useAuth()
  return isAuthenticated ? <RoleRoot /> : <Landing />
}

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/public/:tenantCode" element={<PublicTransparency />} />
      <Route path="/observe/:tenantCode" element={<PublicObserve />} />
      <Route path="/privacy" element={<PrivacyPolicy />} />
      <Route path="/terms" element={<TermsOfService />} />
      <Route path="/support" element={<Support />} />

      <Route path="/" element={<RootRoute />} />
      <Route path="/committees" element={<ProtectedRoute><Committees /></ProtectedRoute>} />
      <Route path="/inspection-log" element={<ProtectedRoute><InspectionLog /></ProtectedRoute>} />
      <Route path="/festival-setup" element={<ProtectedRoute><FestivalSetup /></ProtectedRoute>} />
      <Route path="/archives" element={<ProtectedRoute><FestivalArchives /></ProtectedRoute>} />
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
