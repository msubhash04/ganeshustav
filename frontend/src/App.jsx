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

export default function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/public" element={<PublicTransparency />} />

      <Route path="/" element={<ProtectedRoute><Dashboard /></ProtectedRoute>} />
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
