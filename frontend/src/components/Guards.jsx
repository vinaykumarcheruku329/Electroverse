import { Navigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { Loading } from './StateViews'

export function ProtectedRoute({ children }) {
  const { isAuthenticated, initializing } = useAuth()
  const location = useLocation()
  if (initializing) return <Loading label="Checking your session…" />
  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location, notice: 'Please sign in to continue.' }} replace />
  }
  return children
}

export function AdminRoute({ children }) {
  const { isAuthenticated, isAdmin, initializing } = useAuth()
  const location = useLocation()
  if (initializing) return <Loading label="Checking your session…" />
  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location, notice: 'Administrator sign-in required.' }} replace />
  }
  if (!isAdmin) return <Navigate to="/" replace />
  return children
}