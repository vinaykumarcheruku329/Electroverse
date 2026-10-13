import { useUi } from '../context/UiContext'

export function Toaster() {
  const { toasts, dismiss } = useUi()
  if (!toasts.length) return null
  return (
    <div className="toaster" role="region" aria-live="polite">
      {toasts.map((t) => (
        <div key={t.id} className={`toast toast-${t.type}`}>
          <span>{t.message}</span>
          <button type="button" className="toast-close" onClick={() => dismiss(t.id)} aria-label="Dismiss">×</button>
        </div>
      ))}
    </div>
  )
}
