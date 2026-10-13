import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { orderApi } from '../api/endpoints'
import { Loading, ErrorMessage } from '../components/StateViews'
import { formatCurrency, formatDate, titleCase } from '../utils/format'
import { useUi } from '../context/UiContext'

export default function OrderDetailPage() {
  const { id } = useParams()
  const { toast } = useUi()
  const [order, setOrder] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [busy, setBusy] = useState(false)

  async function load() {
    setLoading(true)
    setError(null)
    try { setOrder(await orderApi.get(id)) } catch (err) { setError(err) } finally { setLoading(false) }
  }

  useEffect(() => { load() }, [id])

  async function cancel() {
    if (!window.confirm('Cancel this order?')) return
    setBusy(true)
    try {
      const updated = await orderApi.cancel(id)
      setOrder(updated)
      toast.success('Order cancelled.')
    } catch (err) { toast.error(err.message) } finally { setBusy(false) }
  }

  if (loading) return <div className="container"><Loading label="Loading order…" /></div>
  if (error) return <div className="container"><ErrorMessage error={error} onRetry={load} /></div>
  if (!order) return null

  return (
    <div className="container" style={{ paddingTop: 24, paddingBottom: 24 }}>
      <nav className="small muted" style={{ marginBottom: 8 }}><Link to="/orders">Orders</Link> / {order.orderNumber}</nav>
      <div className="page-hero" style={{ marginBottom: 24 }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', flexWrap: 'wrap', gap: 12 }}>
          <div>
            <h1 style={{ marginBottom: 2 }}>Order {order.orderNumber}</h1>
            <p>Placed {formatDate(order.createdAt, true)}</p>
          </div>
          <span className={`pill ${order.status === 'CANCELLED' ? 'pill-danger' : order.status === 'DELIVERED' ? 'pill-success' : 'pill-bronze'}`}>{titleCase(order.status)}</span>
        </div>
      </div>

      <div className="cart-layout">
        <div>
          <div className="card" style={{ padding: 20, marginBottom: 18 }}>
            <h3>Items</h3>
            <div className="table-wrap" style={{ border: 0 }}>
              <table className="table">
                <thead><tr><th>Product</th><th>Unit</th><th>Qty</th><th style={{ textAlign: 'right' }}>Total</th></tr></thead>
                <tbody>
                  {order.items?.map((it) => (
                    <tr key={it.id}>
                      <td><Link to={`/products/${it.productId}`}>{it.productName}</Link></td>
                      <td>{formatCurrency(it.unitPrice)}</td>
                      <td>{it.quantity}</td>
                      <td style={{ textAlign: 'right' }}>{formatCurrency(it.lineTotal)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          <div className="card" style={{ padding: 20 }}>
            <h3>Status history</h3>
            <div style={{ marginTop: 10 }}>
              {order.statusHistory?.length ? order.statusHistory.map((h, i) => (
                <div key={i} className="order-line">
                  <span><strong>{titleCase(h.status)}</strong>{h.note ? ` — ${h.note}` : ''}</span>
                  <span className="muted small">{formatDate(h.changedAt, true)}</span>
                </div>
              )) : <p className="muted small">No status changes recorded yet.</p>}
            </div>
          </div>
        </div>

        <aside className="summary-card">
          <h3>Summary</h3>
          <div className="summary-row"><span>Subtotal</span><span>{formatCurrency(order.subtotal)}</span></div>
          <div className="summary-row"><span>Shipping</span><span>{formatCurrency(order.shippingFee)}</span></div>
          <div className="summary-row"><span>Tax</span><span>{formatCurrency(order.tax)}</span></div>
          <div className="summary-row total"><span>Total</span><span>{formatCurrency(order.total)}</span></div>

          <h4 style={{ marginTop: 18 }}>Shipping to</h4>
          <p className="muted small">
            {order.shippingFullName}<br />
            {order.shippingLine1}{order.shippingLine2 ? `, ${order.shippingLine2}` : ''}<br />
            {order.shippingCity}, {order.shippingState} {order.shippingPostalCode}<br />
            {order.shippingCountry}<br />
            {order.shippingPhone}
          </p>
          <p className="small muted">Payment: {titleCase(order.paymentMethod)}</p>
          {order.cancellable ? (
            <button className="btn btn-danger-outline btn-block" onClick={cancel} disabled={busy}>
              {busy ? 'Cancelling…' : 'Cancel order'}
            </button>
          ) : null}
        </aside>
      </div>
    </div>
  )
}