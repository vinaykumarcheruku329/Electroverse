import { createContext, useContext, useEffect, useMemo, useState, useCallback } from 'react'
import { authApi } from '../api/endpoints'
import { getToken, setToken } from '../api/client'

const AuthContext = createContext(null)

export function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [initializing, setInitializing] = useState(Boolean(getToken()))

  const clear = useCallback(() => {
    setToken(null)
    setUser(null)
  }, [])

  useEffect(() => {
    let mounted = true
    async function bootstrap() {
      if (!getToken()) {
        setInitializing(false)
        return
      }
      try {
        const me = await authApi.me()
        if (mounted) setUser(me)
      } catch {
        if (mounted) clear()
      } finally {
        if (mounted) setInitializing(false)
      }
    }
    bootstrap()
    const onUnauthorized = () => setUser(null)
    window.addEventListener('ev:unauthorized', onUnauthorized)
    return () => {
      mounted = false
      window.removeEventListener('ev:unauthorized', onUnauthorized)
    }
  }, [clear])

  const applyAuth = useCallback((data) => {
    setToken(data.token)
    setUser(data.user)
    return data.user
  }, [])

  const login = useCallback(async (email, password) => {
    const data = await authApi.login({ email, password })
    return applyAuth(data)
  }, [applyAuth])

  const register = useCallback(async (payload) => {
    const data = await authApi.register(payload)
    return applyAuth(data)
  }, [applyAuth])

  const logout = useCallback(() => {
    clear()
  }, [clear])

  const updateProfile = useCallback(async (payload) => {
    const updated = await authApi.updateProfile(payload)
    setUser(updated)
    return updated
  }, [])

  const value = useMemo(() => ({
    user,
    initializing,
    isAuthenticated: Boolean(user),
    isAdmin: user?.role === 'ADMIN',
    login,
    register,
    logout,
    updateProfile
  }), [user, initializing, login, register, logout, updateProfile])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) throw new Error('useAuth must be used within AuthProvider')
  return ctx
}
