import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { orderApi } from '../api/endpoints'
import { Loading, ErrorMessage, EmptyState } from '../components/StateViews'
import { Pagination } from '../components/Pagination'
import { formatCurrency, formatDate, titleCase } from '../utils/format'
import { useUi } from '../context/UiContext'

const PAGE_SIZE = 10

export default function OrdersPage() {
  const { toast } = useUi()
  const [orders, setOrders] = useState(null)
  const [page, setPage] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  async function load(p = page) {
    setLoading(true)
    setError(null)
    try {
      const data = await orderApi.list({ page: p, size: PAGE_SIZE })
      setOrders(data)
      setPage(p)
    } catch (err) { setError(err) } finally { setLoading(false) }
  }

  useEffect(() => { load(0) }, [])

  async function cancel(order) {
    if (!window.confirm(`Cancel order ${order.orderNumber}?`)) return
    try {
      await orderApi.cancel(order.id)
      toast.success('Order cancelled.')
      load(page)
    } catch (err) { toast.error(err.message) }
  }

  return (
    <div className="container" style={{ paddingTop: 24, paddingBottom: 24 }}>
      <div className="page-hero" style={{ marginBottom: 24 }}>
        <h1>Your orders</h1>
        <p>Track purchases, review details, and manage cancellations.</p>
      </div>

      {loading && !orders ? <Loading label="Loading orders…" /> : null}
      {!loading && error ? <ErrorMessage error={error} onRetry={() => load(page)} /> : null}
      {!loading && !error && orders && orders.totalElements === 0 ? (
        <EmptyState title="No orders yet" message="Once you place an order it will appear here." action={<Link to="/products" className="btn btn-primary">Start shopping</Link>} />
      ) : null}

      {!loading && orders?.content?.map((o) => (
        <div key={o.id} className="order-card">
          <div className="order-head">
            <div>
              <strong>{o.orderNumber}</strong>
              <div className="muted small">Placed {formatDate(o.createdAt, true)}</div>
            </div>
            <div style={{ textAlign: 'right' }}>
              <span className={`pill ${o.status === 'CANCELLED' ? 'pill-danger' : o.status === 'DELIVERED' ? 'pill-success' : 'pill-bronze'}`}>
                {titleCase(o.status)}
              </span>
              <div style={{ fontWeight: 800, marginTop: 4 }}>{formatCurrency(o.total)}</div>
            </div>
          </div>
          <div className="order-lines">
            {o.items?.slice(0, 4).map((it) => (
              <div key={it.id} className="order-line">
                <span>{it.quantity} × {it.productName}</span>
                <span>{formatCurrency(it.lineTotal)}</span>
              </div>
            ))}
            {o.items?.length > 4 ? <div className="order-line">+ {o.items.length - 4} more item(s)</div> : null}
          </div>
          <div style={{ display: 'flex', gap: 10, marginTop: 12, flexWrap: 'wrap' }}>
            <Link to={`/orders/${o.id}`} className="btn btn-outline btn-sm">View details</Link>
            {o.cancellable ? <button className="btn btn-danger-outline btn-sm" onClick={() => cancel(o)}>Cancel order</button> : null}
          </div>
        </div>
      ))}

      {orders && orders.totalPages > 1 ? <Pagination page={orders.page} totalPages={orders.totalPages} onChange={load} /> : null}
    </div>
  )
}