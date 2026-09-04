import React, { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export default function Login() {
  const { login } = useAuth()
  const navigate = useNavigate()
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await login(username, password)
      navigate('/')
    } catch (err) {
      setError(err?.response?.data || 'Invalid username or password')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="min-h-screen flex flex-col bg-gradient-to-br from-saffron-100 via-cream to-maroon-100">
      {/* Header - just the logo, left-aligned (icon + text) on every
          screen size, linking back to the public landing page */}
      <header className="px-4 md:px-8 py-4">
        <Link to="/" className="inline-flex items-center gap-2 text-maroon-800 font-display font-bold text-lg hover:opacity-80 transition">
          <span className="text-2xl leading-none">🐘</span> Ganesh Utsav
        </Link>
      </header>

      <main className="flex-1 flex items-center justify-center px-4 py-8">
        <div className="card w-full max-w-sm">
          {/* Clicking the icon/title also returns to the landing page */}
          <Link to="/" className="flex flex-col items-center mb-6 group">
            <div className="w-16 h-16 rounded-full bg-saffron-400 flex items-center justify-center text-3xl mb-3 transition group-hover:scale-105">🐘</div>
            <h1 className="text-xl font-bold text-maroon-800 transition group-hover:text-saffron-600">Ganesh Utsav</h1>
            <p className="text-sm text-maroon-500">Expense Tracker · Committee Login</p>
          </Link>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="label-text">Username</label>
              <input
                className="input-field"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                placeholder="e.g. admin"
                required
              />
            </div>
            <div>
              <label className="label-text">Password</label>
              <input
                type="password"
                className="input-field"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                required
              />
            </div>

            {error && <p className="text-sm text-maroon-600 bg-maroon-50 rounded-lg px-3 py-2">{error}</p>}

            <button type="submit" className="btn-primary w-full" disabled={loading}>
              {loading ? 'Signing in…' : 'Sign In'}
            </button>
          </form>

          <p className="text-xs text-maroon-400 text-center mt-5">
            Only registered committee members (President, Treasurer, Secretary, Volunteer) can access this dashboard.
          </p>
        </div>
      </main>

      {/* Simple footer - deliberately minimal here, unlike the full
          Footer used on public pages (no link columns, no contact card) */}
      <footer className="text-center py-6 px-4 text-xs text-maroon-400">
        <p>© {new Date().getFullYear()} All rights reserved.</p>
        <p className="mt-0.5">Developed and Managed by Lenvy Technologies</p>
      </footer>
    </div>
  )
}
