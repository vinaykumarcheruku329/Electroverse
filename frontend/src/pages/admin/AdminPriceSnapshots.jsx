import { useEffect, useState } from 'react'
import { adminApi, catalogApi } from '../../api/endpoints'
import { AdminLayout } from '../../components/AdminLayout'
import { Loading, ErrorMessage, EmptyState } from '../../components/StateViews'
import { useUi } from '../../context/UiContext'
import { formatCurrency, formatDate } from '../../utils/format'

export default function AdminPriceSnapshots() {
  const { toast } = useUi()
  const [points, setPoints] = useState([])
  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [form, setForm] = useState({ productId: '', price: '', source: '', recordedAt: '' })
  const [busy, setBusy] = useState(false)

  function load() {
    setLoading(true)
    setError(null)
    adminApi.priceSnapshots.recent().then(setPoints).catch(setError).finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])
  useEffect(() => { catalogApi.products({ size: 60, sort: 'name' }).then((d) => setProducts(d.content || [])).catch(() => {}) }, [])

  async function save(e) {
    e.preventDefault()
    setBusy(true)
    try {
      await adminApi.products.addSnapshot(Number(form.productId), {
        price: Number(form.price),
        source: form.source || undefined,
        recordedAt: form.recordedAt || null
      })
      toast.success('Price snapshot recorded.')
      setForm({ productId: '', price: '', source: '', recordedAt: '' })
      load()
    } catch (err) { toast.error(err.message) } finally { setBusy(false) }
  }

  return (
    <AdminLayout title="Price Snapshots" subtitle="Record and review recent market price points." actions={<button type="button" className="btn btn-outline btn-sm" onClick={load}>Refresh</button>}>
      <div className="admin-two-col">
        <section className="card tool-card">
          <h3>Recent price points</h3>
          {loading ? <Loading label="Loading snapshots…" /> : null}
          {error ? <ErrorMessage error={error} onRetry={load} /> : null}
          {!loading && !error && points.length === 0 ? <EmptyState title="No snapshots" message="Record a price snapshot to get started." /> : null}
          {points.length ? (
            <div className="table-wrap">
              <table className="table">
                <thead><tr><th>Recorded</th><th>Price</th><th>Source</th><th>Type</th></tr></thead>
                <tbody>
                  {points.map((p, i) => (
                    <tr key={i}>
                      <td>{formatDate(p.date, true)}</td>
                      <td><strong>{formatCurrency(p.price)}</strong></td>
                      <td className="muted small">{p.source || '—'}</td>
                      <td>{p.demoData ? <span className="pill pill-muted">Demo</span> : <span className="pill pill-success">Live</span>}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : null}
        </section>

        <section className="card tool-card">
          <h3>Record a snapshot</h3>
          <form onSubmit={save}>
            <div className="form-group">
              <label>Product</label>
              <select className="form-control" required value={form.productId} onChange={(e) => setForm({ ...form, productId: e.target.value })}>
                <option value="">Select product…</option>
                {products.map((p) => <option key={p.id} value={p.id}>{p.name} — {formatCurrency(p.price)}</option>)}
              </select>
            </div>
            <div className="form-group"><label>Price</label><input className="form-control" type="number" step="0.01" min="0.01" required value={form.price} onChange={(e) => setForm({ ...form, price: e.target.value })} /></div>
            <div className="form-group"><label>Source <span className="muted small">(optional)</span></label><input className="form-control" value={form.source} onChange={(e) => setForm({ ...form, source: e.target.value })} placeholder="Amazon, retailer…" /></div>
            <div className="form-group"><label>Recorded at <span className="muted small">(optional)</span></label><input className="form-control" type="datetime-local" value={form.recordedAt} onChange={(e) => setForm({ ...form, recordedAt: e.target.value })} /></div>
            <button type="submit" className="btn btn-primary" disabled={busy}>{busy ? 'Saving…' : 'Record snapshot'}</button>
          </form>
        </section>
      </div>
    </AdminLayout>
  )
}
