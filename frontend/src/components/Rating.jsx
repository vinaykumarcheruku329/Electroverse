export function Rating({ value = 0, count, showCount = true, size = 'md' }) {
  const rounded = Math.round(Number(value) || 0)
  return (
    <span className={`rating rating-${size}`} title={`${Number(value || 0).toFixed(1)} out of 5`}>
      <span className="stars" aria-hidden="true">
        {[1, 2, 3, 4, 5].map((i) => (
          <span key={i} className={i <= rounded ? 'star filled' : 'star'}>★</span>
        ))}
      </span>
      {showCount ? (
        <span className="rating-text">
          {Number(value || 0).toFixed(1)}{count !== undefined ? ` (${count})` : ''}
        </span>
      ) : null}
    </span>
  )
}
