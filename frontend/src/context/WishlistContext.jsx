import { createContext, useContext, useEffect, useMemo, useState, useCallback } from 'react'
import { wishlistApi } from '../api/endpoints'
import { useAuth } from './AuthContext'

const WishlistContext = createContext(null)

export function WishlistProvider({ children }) {
  const { isAuthenticated } = useAuth()
  const [ids, setIds] = useState([])
  const [loading, setLoading] = useState(false)

  const refresh = useCallback(async () => {
    if (!isAuthenticated) {
      setIds([])
      return
    }
    setLoading(true)
    try {
      const items = await wishlistApi.list()
      setIds((items || []).map((p) => p.id))
    } catch {
      setIds([])
    } finally {
      setLoading(false)
    }
  }, [isAuthenticated])

  useEffect(() => {
    refresh()
  }, [refresh])

  const has = useCallback((productId) => ids.includes(productId), [ids])

  const toggle = useCallback(async (productId) => {
    if (has(productId)) {
      await wishlistApi.remove(productId)
      setIds((prev) => prev.filter((id) => id !== productId))
      return false
    }
    await wishlistApi.add(productId)
    setIds((prev) => [...prev, productId])
    return true
  }, [has])

  const value = useMemo(() => ({ ids, has, toggle, refresh, loading, count: ids.length }), [ids, has, toggle, refresh, loading])

  return <WishlistContext.Provider value={value}>{children}</WishlistContext.Provider>
}

export function useWishlist() {
  const ctx = useContext(WishlistContext)
  if (!ctx) throw new Error('useWishlist must be used within WishlistProvider')
  return ctx
}
