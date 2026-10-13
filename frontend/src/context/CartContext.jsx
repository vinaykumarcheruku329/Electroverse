import { createContext, useContext, useEffect, useMemo, useState, useCallback, useRef } from 'react'
import { cartApi } from '../api/endpoints'
import { useAuth } from './AuthContext'

const CartContext = createContext(null)

const EMPTY = { cartId: null, items: [], itemCount: 0, subtotal: 0 }

export function CartProvider({ children }) {
  const { isAuthenticated } = useAuth()
  const [cart, setCart] = useState(EMPTY)
  const [loading, setLoading] = useState(false)
  const toggleRef = useRef({})

  const refresh = useCallback(async () => {
    if (!isAuthenticated) {
      setCart(EMPTY)
      return EMPTY
    }
    setLoading(true)
    try {
      const data = await cartApi.get()
      setCart(data || EMPTY)
      return data
    } finally {
      setLoading(false)
    }
  }, [isAuthenticated])

  useEffect(() => {
    refresh()
  }, [refresh])

  const addItem = useCallback(async (productId, quantity = 1) => {
    const data = await cartApi.add(productId, quantity)
    setCart(data || EMPTY)
    return data
  }, [])

  const updateItem = useCallback(async (itemId, quantity) => {
    const data = await cartApi.update(itemId, quantity)
    setCart(data || EMPTY)
    return data
  }, [])

  const removeItem = useCallback(async (itemId) => {
    const data = await cartApi.remove(itemId)
    setCart(data || EMPTY)
    return data
  }, [])

  const clearLocal = useCallback(() => setCart(EMPTY), [])

  const addItemSafe = useCallback(async (productId, quantity = 1) => {
    if (toggleRef.current[`c${productId}`]) return cart
    toggleRef.current[`c${productId}`] = true
    try {
      return await addItem(productId, quantity)
    } finally {
      toggleRef.current[`c${productId}`] = false
    }
  }, [addItem, cart])

  const value = useMemo(() => ({
    cart,
    itemCount: cart?.itemCount || 0,
    loading,
    refresh,
    addItem: addItemSafe,
    updateItem,
    removeItem,
    clearLocal
  }), [cart, loading, refresh, addItemSafe, updateItem, removeItem, clearLocal])

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>
}

export function useCart() {
  const ctx = useContext(CartContext)
  if (!ctx) throw new Error('useCart must be used within CartProvider')
  return ctx
}
