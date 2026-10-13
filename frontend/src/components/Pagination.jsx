export function Pagination({ page, totalPages, onChange }) {
  if (!totalPages || totalPages <= 1) return null
  const pages = []
  const add = (p) => { if (!pages.includes(p)) pages.push(p) }
  add(0)
  for (let p = page - 1; p <= page + 1; p += 1) {
    if (p > 0 && p < totalPages - 1) add(p)
  }
  if (totalPages > 1) add(totalPages - 1)
  pages.sort((a, b) => a - b)

  return (
    <nav className="pagination" aria-label="Pagination">
      <button
        type="button"
        className="btn btn-outline btn-sm"
        disabled={page <= 0}
        onClick={() => onChange(page - 1)}
      >
        Prev
      </button>
      {pages.map((p, idx) => {
        const prev = pages[idx - 1]
        const gap = prev !== undefined && p - prev > 1
        return (
          <span key={p} className="pagination-item">
            {gap ? <span className="pagination-gap">…</span> : null}
            <button
              type="button"
              className={p === page ? 'page-btn active' : 'page-btn'}
              onClick={() => onChange(p)}
              aria-current={p === page ? 'page' : undefined}
            >
              {p + 1}
            </button>
          </span>
        )
      })}
      <button
        type="button"
        className="btn btn-outline btn-sm"
        disabled={page >= totalPages - 1}
        onClick={() => onChange(page + 1)}
      >
        Next
      </button>
    </nav>
  )
}
