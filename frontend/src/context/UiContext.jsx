import { createContext, useContext, useMemo, useState, useCallback } from 'react'

const UiContext = createContext(null)

let seq = 0

export function UiProvider({ children }) {
  const [toasts, setToasts] = useState([])

  const dismiss = useCallback((id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id))
  }, [])

  const push = useCallback((type, message) => {
    const id = ++seq
    setToasts((prev) => [...prev, { id, type, message }])
    setTimeout(() => dismiss(id), 4200)
  }, [dismiss])

  const toast = useMemo(() => ({
    success: (m) => push('success', m),
    error: (m) => push('error', m),
    info: (m) => push('info', m)
  }), [push])

  const value = useMemo(() => ({ toasts, dismiss, toast }), [toasts, dismiss, toast])

  return <UiContext.Provider value={value}>{children}</UiContext.Provider>
}

export function useUi() {
  const ctx = useContext(UiContext)
  if (!ctx) throw new Error('useUi must be used within UiProvider')
  return ctx
}
