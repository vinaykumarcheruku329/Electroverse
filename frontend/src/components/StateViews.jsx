export function Loading({ label = 'Loading…' }) {
  return (
    <div className="state-view" role="status" aria-live="polite">
      <div className="spinner" />
      <p>{label}</p>
    </div>
  )
}

export function ErrorMessage({ error, onRetry }) {
  const message = typeof error === 'string'
    ? error
    : error?.message || 'Something went wrong.'
  return (
    <div className="state-view state-error" role="alert">
      <div className="state-icon">!</div>
      <p>{message}</p>
      {error?.fieldErrors?.length ? (
        <ul className="field-error-list">
          {error.fieldErrors.map((fe) => (
            <li key={fe.field}>{fe.field}: {fe.message}</li>
          ))}
        </ul>
      ) : null}
      {onRetry ? (
        <button type="button" className="btn btn-outline btn-sm" onClick={onRetry}>Try again</button>
      ) : null}
    </div>
  )
}

export function EmptyState({ title = 'Nothing here yet', message, action }) {
  return (
    <div className="state-view state-empty">
      <div className="state-icon">◍</div>
      <h3>{title}</h3>
      {message ? <p>{message}</p> : null}
      {action}
    </div>
  )
}

export function SkeletonGrid({ count = 8 }) {
  return (
    <div className="product-grid">
      {Array.from({ length: count }).map((_, i) => (
        <div className="card skeleton-card" key={i}>
          <div className="skeleton skeleton-img" />
          <div className="skeleton skeleton-line" />
          <div className="skeleton skeleton-line short" />
        </div>
      ))}
    </div>
  )
}
