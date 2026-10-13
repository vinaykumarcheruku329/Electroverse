import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { catalogApi, priceApi } from '../api/endpoints'
import { Loading, ErrorMessage, EmptyState } from '../components/StateViews'
import { useAuth } from '../context/AuthContext'
import { useUi } from '../context/UiContext'
import { formatCurrency, formatDate } from '../utils/format'

export default function PriceIntelligencePage() {
  const { isAuthenticated } = useAuth()
  const { toast } = useUi()
  const [products, setProducts] = useState([])
  const [selectedId, setSelectedId] = useState('')
  const [history, setHistory] = useState(null)
  const [alerts, setAlerts] = useState([])
  const [targetPrice, setTargetPrice] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    catalogApi.products({ size: 60, sort: 'name' })
      .then((data) => {
        const list = data.content || []
        setProducts(list)
        if (list.length) setSelectedId(String(list[0].id))
      })
      .catch(setError)
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    if (isAuthenticated) {
      priceApi.alerts().then(setAlerts).catch(() => setAlerts([]))
    } else {
      setAlerts([])
    }
  }, [isAuthenticated])

  useEffect(() => {
    if (!selectedId) return
    setHistory(null)
    priceApi.history(selectedId).then(setHistory).catch(() => setHistory(null))
  }, [selectedId])

  const selected = useMemo(() => products.find((p) => String(p.id) === selectedId), [products, selectedId])

  async function createAlert(e) {
    e.preventDefault()
    if (!selected) return
    setBusy(true)
    try {
      await priceApi.createAlert({ productId: selected.id, targetPrice: Number(targetPrice) })
      setTargetPrice('')
      setAlerts(await priceApi.alerts())
      toast.success('Price alert created.')
    } catch (err) { toast.error(err.message) } finally { setBusy(false) }
  }

  async function removeAlert(id) {
    try {
      await priceApi.removeAlert(id)
      setAlerts((prev) => prev.filter((a) => a.id !== id))
      toast.success('Alert removed.')
    } catch (err) { toast.error(err.message) }
  }

  const points = history?.points || []
  const min = points.length ? Math.min(...points.map((p) => Number(p.price))) : 0
  const max = points.length ? Math.max(...points.map((p) => Number(p.price))) : 0

  return (
    <div className="container" style={{ paddingTop: 24, paddingBottom: 24 }}>
      <div className="page-hero" style={{ marginBottom: 24 }}>
        <h1>Price Intelligence</h1>
        <p>Track price movement over time and set alerts so you buy at the right moment.</p>
      </div>

      {loading ? <Loading label="Loading catalog…" /> : null}
      {error ? <ErrorMessage error={error} /> : null}

      <div className="card" style={{ padding: 20, marginBottom: 18 }}>
        <div className="form-group" style={{ marginBottom: 0 }}>
          <label>Select a product</label>
          <select className="form-control" value={selectedId} onChange={(e) => setSelectedId(e.target.value)}>
            {products.map((p) => <option key={p.id} value={p.id}>{p.name} — {formatCurrency(p.price)}</option>)}
          </select>
        </div>
      </div>

      {history ? (
        <div className="cart-layout" style={{ gridTemplateColumns: '1.6fr 1fr', alignItems: 'start' }}>
          <div className="card tool-card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h3>{selected?.name}</h3>
              <Link to={`/products/${selectedId}`} className="btn btn-ghost btn-sm">View product</Link>
            </div>
            <div className="kv">
              <div className="kv-item"><span>Latest</span><strong>{formatCurrency(history.latestPrice)}</strong></div>
              <div className="kv-item"><span>Lowest</span><strong>{formatCurrency(history.lowestPrice)}</strong></div>
              <div className="kv-item"><span>Updated</span><strong>{formatDate(history.latestDate)}</strong></div>
              <div className="kv-item"><span>Source</span><strong>{history.source || 'Market'}</strong></div>
            </div>
            {points.length > 1 ? (
              <div className="chart-bars">
                {points.map((p, i) => {
                  const h = max === min ? 55 : ((Number(p.price) - min) / (max - min)) * 100
                  return (
                    <div key={i} className="chart-bar" title={`${formatDate(p.date)} · ${formatCurrency(p.price)}`}>
                      <div className="bar" style={{ height: `${Math.max(6, h)}%` }} />
                      <div className="bar-label">{new Date(p.date).toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}</div>
                    </div>
                  )
                })}
              </div>
            ) : <p className="muted small">Not enough data points to chart yet.</p>}

            {points.length ? (
              <div className="table-wrap" style={{ marginTop: 10 }}>
                <table className="table">
                  <thead><tr><th>Date</th><th>Price</th><th>Source</th></tr></thead>
                  <tbody>
                    {points.slice().reverse().map((p, i) => (
                      <tr key={i}><td>{formatDate(p.date, true)}</td><td>{formatCurrency(p.price)}</td><td>{p.source || '—'}</td></tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : null}
          </div>

          <aside className="summary-card">
            <h3>Set a price alert</h3>
            {isAuthenticated ? (
              <form onSubmit={createAlert}>
                <div className="form-group">
                  <label>Notify me when price ≤</label>
                  <input className="form-control" type="number" step="0.01" min="0" required value={targetPrice} onChange={(e) => setTargetPrice(e.target.value)} placeholder="Target price" />
                </div>
                <button type="submit" className="btn btn-primary btn-block" disabled={busy}>{busy ? 'Saving…' : 'Create alert'}</button>
              </form>
            ) : (
              <p className="muted">Please <Link to="/login">sign in</Link> to create price alerts.</p>
            )}

            {isAuthenticated ? (
              <div style={{ marginTop: 20 }}>
                <h4>My alerts ({alerts.length})</h4>
                {alerts.length === 0 ? <p className="muted small">No alerts yet.</p> : alerts.map((a) => (
                  <div key={a.id} className="order-line" style={{ alignItems: 'center' }}>
                    <span>
                      <strong>{a.productName}</strong><br />
                      <span className="muted small">Target {formatCurrency(a.targetPrice)} · last {formatCurrency(a.lastObservedPrice)}</span>
                    </span>
                    <button type="button" className="btn btn-ghost btn-sm" onClick={() => removeAlert(a.id)} aria-label="Delete alert">✕</button>
                  </div>
                ))}
              </div>
            ) : null}
          </aside>
        </div>
      ) : (!loading && selectedId) ? <EmptyState title="No price history" message="No historical price data is available for this product." /> : null}
    </div>
  )
}