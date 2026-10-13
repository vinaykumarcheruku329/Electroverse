import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { adminApi } from '../../api/endpoints'
import { AdminLayout } from '../../components/AdminLayout'
import { Loading, ErrorMessage } from '../../components/StateViews'
import { formatCurrency, formatDate, formatNumber, titleCase } from '../../utils/format'

function Stat({ label, value, tone = '' }) {
  return (
    <div className={`stat-card ${tone}`}>
      <span className="stat-label">{label}</span>
      <strong className="stat-value">{value}</strong>
    </div>
  )
}

export default function AdminDashboard() {
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  function load() {
    setLoading(true)
    setError(null)
    adminApi.dashboard().then(setData).catch(setError).finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  const maxCat = data?.productsByCategory?.reduce((m, c) => Math.max(m, c.value), 0) || 1

  return (
    <AdminLayout title="Dashboard" subtitle="Live store metrics across the ElectroVerse catalog." actions={<button type="button" className="btn btn-outline btn-sm" onClick={load}>Refresh</button>}>
      {loading ? <Loading label="Loading dashboard…" /> : null}
      {error ? <ErrorMessage error={error} onRetry={load} /> : null}

      {data ? (
        <>
          <div className="stat-grid">
            <Stat label="Revenue" value={formatCurrency(data.totalRevenue)} tone="stat-accent" />
            <Stat label="Total orders" value={formatNumber(data.totalOrders)} />
            <Stat label="Pending orders" value={formatNumber(data.pendingOrders)} />
            <Stat label="Customers" value={formatNumber(data.totalCustomers)} />
            <Stat label="Users" value={formatNumber(data.totalUsers)} />
            <Stat label="Products" value={formatNumber(data.totalProducts)} />
            <Stat label="Active products" value={formatNumber(data.activeProducts)} />
            <Stat label="Low stock" value={formatNumber(data.lowStockProducts)} tone={data.lowStockProducts ? 'stat-warn' : ''} />
            <Stat label="Reviews" value={formatNumber(data.totalReviews)} />
            <Stat label="Pending reviews" value={formatNumber(data.pendingReviews)} tone={data.pendingReviews ? 'stat-warn' : ''} />
            <Stat label="Triggered alerts" value={formatNumber(data.triggeredAlerts)} />
            <Stat label="Finder sessions" value={formatNumber(data.finderSessions)} />
          </div>

          <div className="admin-two-col">
            <section className="card tool-card">
              <h3>Orders by status</h3>
              {data.ordersByStatus?.length ? data.ordersByStatus.map((c) => (
                <div key={c.label} className="bar-row">
                  <span className="bar-row-label">{titleCase(c.label)}</span>
                  <div className="bar-row-track"><div className="bar-row-fill" style={{ width: `${(c.value / (data.totalOrders || 1)) * 100}%` }} /></div>
                  <span className="bar-row-value">{c.value}</span>
                </div>
              )) : <p className="muted small">No orders yet.</p>}
            </section>

            <section className="card tool-card">
              <h3>Products by category</h3>
              {data.productsByCategory?.length ? data.productsByCategory.map((c) => (
                <div key={c.label} className="bar-row">
                  <span className="bar-row-label">{c.label}</span>
                  <div className="bar-row-track"><div className="bar-row-fill" style={{ width: `${(c.value / maxCat) * 100}%` }} /></div>
                  <span className="bar-row-value">{c.value}</span>
                </div>
              )) : <p className="muted small">No categories yet.</p>}
            </section>
          </div>

          <section className="card tool-card" style={{ marginTop: 18 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <h3>Recent orders</h3>
              <Link to="/admin/orders" className="btn btn-ghost btn-sm">View all</Link>
            </div>
            {data.recentOrders?.length ? (
              <div className="table-wrap">
                <table className="table">
                  <thead><tr><th>Order</th><th>Date</th><th>Status</th><th>Total</th></tr></thead>
                  <tbody>
                    {data.recentOrders.map((o) => (
                      <tr key={o.id}>
                        <td><Link to={`/orders/${o.id}`}>{o.orderNumber}</Link></td>
                        <td>{formatDate(o.createdAt, true)}</td>
                        <td><span className="status-badge">{titleCase(o.status)}</span></td>
                        <td>{formatCurrency(o.total)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : <p className="muted small">No recent orders.</p>}
          </section>
        </>
      ) : null}
    </AdminLayout>
  )
}
