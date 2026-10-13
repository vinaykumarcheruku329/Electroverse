import { useEffect, useMemo, useState } from 'react'
import { catalogApi, comparisonApi } from '../api/endpoints'
import { Rating } from '../components/Rating'
import { Loading, ErrorMessage, EmptyState } from '../components/StateViews'
import { formatCurrency } from '../utils/format'

const PRIORITY_KEYS = ['price', 'rating', 'warranty', 'performance', 'battery', 'display', 'storage', 'portability']

export default function ComparePage() {
  const [products, setProducts] = useState([])
  const [loadingList, setLoadingList] = useState(true)
  const [selected, setSelected] = useState([])
  const [priorities, setPriorities] = useState(['price', 'rating'])
  const [search, setSearch] = useState('')
  const [result, setResult] = useState(null)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    catalogApi.products({ size: 60, sort: 'name' })
      .then((data) => setProducts(data.content || []))
      .catch(setError)
      .finally(() => setLoadingList(false))
  }, [])

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase()
    if (!q) return products
    return products.filter((p) => `${p.name} ${p.brand} ${p.categoryName}`.toLowerCase().includes(q))
  }, [products, search])

  function toggle(id) {
    setSelected((prev) => {
      if (prev.includes(id)) return prev.filter((x) => x !== id)
      if (prev.length >= 4) return prev
      return [...prev, id]
    })
  }

  function togglePriority(key) {
    setPriorities((prev) => (prev.includes(key) ? prev.filter((k) => k !== key) : [...prev, key]))
  }

  async function runCompare() {
    if (selected.length < 2) { setError(new Error('Select at least 2 products to compare.')); return }
    setBusy(true)
    setError(null)
    try {
      const data = await comparisonApi.compare({ productIds: selected, priorities })
      setResult(data)
    } catch (err) { setError(err) } finally { setBusy(false) }
  }

  return (
    <div className="container" style={{ paddingTop: 24, paddingBottom: 24 }}>
      <div className="page-hero" style={{ marginBottom: 24 }}>
        <h1>Product Comparison</h1>
        <p>Pick 2–4 products and choose which priorities matter most.</p>
      </div>

      <div className="card" style={{ padding: 20, marginBottom: 18 }}>
        <h3>1. Choose products ({selected.length}/4)</h3>
        <input className="form-control" style={{ margin: '10px 0' }} placeholder="Filter products…" value={search} onChange={(e) => setSearch(e.target.value)} />
        {loadingList ? <Loading label="Loading products…" /> : (
          <div className="tool-grid" style={{ maxHeight: 340, overflowY: 'auto' }}>
            {filtered.map((p) => {
              const isSel = selected.includes(p.id)
              return (
                <button
                  type="button"
                  key={p.id}
                  className="card tool-card"
                  onClick={() => toggle(p.id)}
                  style={{ textAlign: 'left', cursor: 'pointer', borderColor: isSel ? 'var(--bronze)' : 'var(--border)', background: isSel ? 'var(--bronze-soft)' : 'var(--surface)' }}
                >
                  <div style={{ display: 'flex', justifyContent: 'space-between', gap: 8 }}>
                    <strong>{p.name}</strong>
                    <span>{isSel ? '✓' : '+'}</span>
                  </div>
                  <div className="muted small">{p.brand} · {p.categoryName}</div>
                  <div style={{ marginTop: 6 }}>{formatCurrency(p.price)}</div>
                </button>
              )
            })}
          </div>
        )}
      </div>

      <div className="card" style={{ padding: 20, marginBottom: 18 }}>
        <h3>2. Priorities</h3>
        <div style={{ display: 'flex', gap: 10, flexWrap: 'wrap', marginTop: 10 }}>
          {PRIORITY_KEYS.map((k) => (
            <button key={k} type="button" className={priorities.includes(k) ? 'pill pill-bronze' : 'pill pill-muted'} style={{ border: 0, cursor: 'pointer', padding: '6px 14px', textTransform: 'capitalize' }} onClick={() => togglePriority(k)}>
              {k}
            </button>
          ))}
        </div>
        <button type="button" className="btn btn-primary btn-lg" style={{ marginTop: 16 }} onClick={runCompare} disabled={busy}>
          {busy ? 'Comparing…' : 'Compare selected'}
        </button>
      </div>

      {error ? <ErrorMessage error={error} /> : null}

      {result ? (
        <div className="card" style={{ padding: 20 }}>
          <h3>Comparison result</h3>
          {Object.keys(result.bestIndexByPriority || {}).length ? (
            <div style={{ margin: '10px 0' }}>
              {Object.entries(result.bestIndexByPriority).map(([k, idx]) => (
                <span key={k} className="pill pill-success" style={{ marginRight: 8, textTransform: 'capitalize' }}>
                  Best {k}: {result.products[idx]?.name}
                </span>
              ))}
            </div>
          ) : null}
          <div className="table-wrap">
            <table className="table compare-table">
              <thead>
                <tr>
                  <th>Attribute</th>
                  {result.products.map((p) => (
                    <th key={p.id}>
                      <div>{p.name}</div>
                      <div className="muted small">{formatCurrency(p.price)}</div>
                      <Rating value={p.ratingAverage} count={p.ratingCount} size="sm" />
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {result.rows?.map((row, i) => (
                  <tr key={i} style={{ background: row.different ? 'transparent' : '#faf8f3' }}>
                    <td><strong>{row.label}</strong>{row.group ? <div className="muted small">{row.group}</div> : null}</td>
                    {row.values.map((v, j) => (
                      <td key={j} style={{ color: row.bestIndex === j ? 'var(--success)' : 'inherit', fontWeight: row.bestIndex === j ? 700 : 400 }}>
                        {v}{row.bestIndex === j ? ' ★' : ''}
                      </td>
                    ))}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      ) : null}

      {!result && !error && selected.length === 0 && !loadingList ? (
        <EmptyState title="No products selected" message="Choose 2–4 products above to see a detailed comparison." />
      ) : null}
    </div>
  )
}