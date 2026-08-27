import React, { createContext, useContext, useState, useCallback } from 'react'
import { authApi } from '../api/authApi'
import { inspectionApi } from '../api/committeeApi'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('gu_user')
    return stored ? JSON.parse(stored) : null
  })

  const login = useCallback(async (username, password) => {
    const data = await authApi.login(username, password)
    localStorage.setItem('gu_token', data.token)
    localStorage.setItem('gu_user', JSON.stringify(data))
    // a fresh login should never carry over a stale inspection backup
    localStorage.removeItem('gu_developer_token')
    localStorage.removeItem('gu_developer_user')
    setUser(data)
    return data
  }, [])

  const logout = useCallback(() => {
    localStorage.removeItem('gu_token')
    localStorage.removeItem('gu_user')
    localStorage.removeItem('gu_developer_token')
    localStorage.removeItem('gu_developer_user')
    setUser(null)
  }, [])

  // Tenant Inspection ("View as President") - swaps the active session
  // into a short-lived, committee-scoped token without touching the
  // Developer's real login. The real token/user is stashed under
  // separate keys so exitInspection() can restore it exactly, and the
  // effective role is set to PRESIDENT so every existing screen (Layout
  // nav, FestivalSetup/Loans/Sponsorships "isPresident" checks) renders
  // exactly as it would for that committee's real President. The
  // backend never trusts this "role" - only the JWT it issued matters
  // for authorization (see JwtAuthFilter / TenantContext).
  const startInspection = useCallback(async (committeeId, mode) => {
    const data = await inspectionApi.start(committeeId, mode)

    // Only stash on the FIRST inspection start, so switching committees
    // mid-session (exit then inspect another) never overwrites the
    // original Developer backup with an inspection session's own token.
    if (!localStorage.getItem('gu_developer_token')) {
      localStorage.setItem('gu_developer_token', localStorage.getItem('gu_token'))
      localStorage.setItem('gu_developer_user', localStorage.getItem('gu_user'))
    }

    const developerUser = JSON.parse(localStorage.getItem('gu_developer_user'))
    const inspectingUser = {
      ...developerUser,
      role: 'PRESIDENT',
      realRole: 'DEVELOPER',
      isInspecting: true,
      inspectionMode: data.mode,
      committeeId: data.committeeId,
      committeeName: data.committeeName,
      tenantCode: data.tenantCode,
    }

    localStorage.setItem('gu_token', data.inspectionToken)
    localStorage.setItem('gu_user', JSON.stringify(inspectingUser))
    setUser(inspectingUser)
    return inspectingUser
  }, [])

  const exitInspection = useCallback(async () => {
    try {
      // best-effort audit call, made while the inspection token is still
      // active - restoring the Developer session below happens either way
      await inspectionApi.exit()
    } catch (err) {
      // swallow - e.g. the inspection token already expired server-side;
      // we still want to get the Developer back into their own session
    }

    const developerToken = localStorage.getItem('gu_developer_token')
    const developerUser = localStorage.getItem('gu_developer_user')
    localStorage.removeItem('gu_developer_token')
    localStorage.removeItem('gu_developer_user')

    if (developerToken && developerUser) {
      localStorage.setItem('gu_token', developerToken)
      localStorage.setItem('gu_user', developerUser)
      setUser(JSON.parse(developerUser))
    } else {
      // shouldn't happen, but fail safe into a clean logout rather than
      // leaving the app in a half-inspecting, tokenless state
      logout()
    }
  }, [logout])

  const isAuthenticated = !!user

  return (
    <AuthContext.Provider value={{ user, login, logout, isAuthenticated, startInspection, exitInspection }}>
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
