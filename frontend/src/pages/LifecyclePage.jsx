import { useEffect, useState } from 'react'
import { Link, useSearchParams } from 'react-router-dom'
import { catalogApi, lifecycleApi } from '../api/endpoints'
import { Loading, ErrorMessage, EmptyState } from '../components/StateViews'
import { formatDate } from '../utils/format'

function ScoreBar({ label, value }) {
  return (
    <div style={{ marginBottom: 12 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
        <span className="small"><strong>{label}</strong></span>
        <span className="small muted">{value}/100</span>
      </div>
      <div style={{ background: 'var(--border)', borderRadius: 999, height: 10, overflow: 'hidden' }}>
        <div style={{ width: `${Math.min(100, Math.max(0, value))}%`, height: '100%', background: 'var(--bronze)' }} />
      </div>
    </div>
  )
}

export default function LifecyclePage() {
  const [params, setParams] = useSearchParams()
  const [products, setProducts] = useState([])
  const [selectedId, setSelectedId] = useState(params.get('product') || '')
  const [lifecycle, setLifecycle] = useState(null)
  const [history, setHistory] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [checked, setChecked] = useState(false)

  useEffect(() => {
    catalogApi.products({ size: 60, sort: 'name' })
      .then((data) => {
        const list = data.content || []
        setProducts(list)
        setSelectedId((prev) => prev || (list.length ? String(list[0].id) : ''))
      })
      .catch(setError)
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    if (!selectedId) return
    setChecked(false)
    setError(null)
    lifecycleApi.get(selectedId)
      .then((data) => setLifecycle(data))
      .catch((err) => {
        if (err.status === 404) setLifecycle(null)
        else setError(err)
      })
      .finally(() => setChecked(true))
    lifecycleApi.history(selectedId).then((h) => setHistory(h || [])).catch(() => setHistory([]))
  }, [selectedId])

  function change(id) {
    setSelectedId(id)
    setParams(id ? { product: id } : {})
  }

  const selected = products.find((p) => String(p.id) === selectedId)

  const labels = lifecycle ? [
    ['Repairability', lifecycle.repairability],
    ['Upgradeability', lifecycle.upgradeability],
    ['Manufacturer support', lifecycle.manufacturerSupport],
    ['Warranty', lifecycle.warranty],
    ['Energy efficiency', lifecycle.energyEfficiency]
  ] : []

  return (
    <div className="container" style={{ paddingTop: 24, paddingBottom: 24 }}>
      <div className="page-hero" style={{ marginBottom: 24 }}>
        <h1>Tech Lifecycle Score</h1>
        <p>How long will this device stay useful, repairable, and supported?</p>
      </div>

      {loading ? <Loading label="Loading catalog…" /> : null}

      <div className="card" style={{ padding: 20, marginBottom: 18 }}>
        <label className="small"><strong>Select a product</strong></label>
        <select className="form-control" value={selectedId} onChange={(e) => change(e.target.value)}>
          {products.map((p) => <option key={p.id} value={p.id}>{p.name} — {p.brand}</option>)}
        </select>
      </div>

      {error ? <ErrorMessage error={error} /> : null}

      {checked && !lifecycle && !error ? (
        <EmptyState title="No lifecycle assessment yet" message="This product has not been scored yet. Check back later." action={selected ? <Link to={`/products/${selected.id}`} className="btn btn-outline">View product</Link> : null} />
      ) : null}

      {lifecycle ? (
        <div className="cart-layout" style={{ gridTemplateColumns: '1fr 1fr', alignItems: 'start' }}>
          <div className="card tool-card">
            <h3>{lifecycle.productName}</h3>
            <div className="lifecycle-score">
              <div className="score-card" style={{ background: 'var(--bronze-soft)' }}>
                <strong style={{ color: 'var(--bronze-dark)' }}>{lifecycle.overall}</strong>
                <span>Overall</span>
              </div>
              <div className="score-card"><span>Assessed</span><strong style={{ fontSize: '1rem' }}>{formatDate(lifecycle.assessmentDate)}</strong></div>
              <div className="score-card"><span>Methodology</span><strong style={{ fontSize: '1rem' }}>{lifecycle.methodologyVersion}</strong></div>
            </div>
            {labels.map(([label, val]) => <ScoreBar key={label} label={label} value={val} />)}
            {lifecycle.explanation ? <p className="muted">{lifecycle.explanation}</p> : null}
          </div>

          <div className="card tool-card">
            <h3>Assessment history</h3>
            {history.length ? (
              <div className="table-wrap">
                <table className="table">
                  <thead><tr><th>Date</th><th>Overall</th><th>Methodology</th></tr></thead>
                  <tbody>
                    {history.map((h) => (
                      <tr key={h.id}><td>{formatDate(h.assessmentDate)}</td><td><strong>{h.overall}</strong></td><td>{h.methodologyVersion}</td></tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : <p className="muted small">No prior assessments recorded.</p>}
            {selected ? <Link to={`/products/${selected.id}`} className="btn btn-outline" style={{ marginTop: 12 }}>View product</Link> : null}
          </div>
        </div>
      ) : null}
    </div>
  )
}