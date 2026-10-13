import { Fragment, useEffect, useState } from 'react'
import { adminApi } from '../../api/endpoints'
import { AdminLayout } from '../../components/AdminLayout'
import { Pagination } from '../../components/Pagination'
import { Loading, ErrorMessage, EmptyState } from '../../components/StateViews'
import { useUi } from '../../context/UiContext'
import { formatCurrency, formatDate, titleCase, ORDER_STATUSES } from '../../utils/format'

const SIZE = 20

export default function AdminOrders() {
  const { toast } = useUi()
  const [page, setPage] = useState(0)
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [expandedId, setExpandedId] = useState(null)
  const [draft, setDraft] = useState({ status: '', note: '' })
  const [busyId, setBusyId] = useState(null)

  function load() {
    setLoading(true)
    setError(null)
    adminApi.orders.list({ page, size: SIZE }).then(setData).catch(setError).finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [page])

  function toggleExpand(o) {
    if (expandedId === o.id) { setExpandedId(null); return }
    setExpandedId(o.id)
    setDraft({ status: o.status, note: '' })
  }

  async function saveStatus(o) {
    setBusyId(o.id)
    try {
      await adminApi.orders.updateStatus(o.id, { status: draft.status, note: draft.note || undefined })
      toast.success(`Order ${o.orderNumber} updated.`)
      setExpandedId(null)
      load()
    } catch (err) { toast.error(err.message) } finally { setBusyId(null) }
  }

  return (
    <AdminLayout title="Orders" subtitle="Review and progress customer orders." actions={<button type="button" className="btn btn-outline btn-sm" onClick={load}>Refresh</button>}>
      {loading ? <Loading label="Loading orders…" /> : null}
      {error ? <ErrorMessage error={error} onRetry={load} /> : null}
      {!loading && !error && data?.content?.length === 0 ? <EmptyState title="No orders" /> : null}

      {data?.content?.length ? (
        <section className="card tool-card">
          <div className="table-wrap">
            <table className="table">
              <thead>
                <tr><th>Order</th><th>Date</th><th>Items</th><th>Customer</th><th>Status</th><th>Total</th><th></th></tr>
              </thead>
              <tbody>
                {data.content.map((o) => (
                  <Fragment key={o.id}>
                    <tr>
                      <td><strong>{o.orderNumber}</strong></td>
                      <td className="muted small">{formatDate(o.createdAt, true)}</td>
                      <td>{o.items?.length || 0}</td>
                      <td>{o.shippingFullName}<div className="muted small">{o.shippingCity}, {o.shippingCountry}</div></td>
                      <td><span className="status-badge">{titleCase(o.status)}</span></td>
                      <td>{formatCurrency(o.total)}</td>
                      <td className="table-actions">
                        <button type="button" className="btn btn-ghost btn-sm" onClick={() => toggleExpand(o)}>{expandedId === o.id ? 'Close' : 'Manage'}</button>
                      </td>
                    </tr>
                    {expandedId === o.id ? (
                      <tr className="order-detail-row">
                        <td colSpan="7">
                          <div className="order-detail-inner">
                            <div>
                              <h4>Items</h4>
                              {o.items?.map((it) => (
                                <div key={it.id} className="order-line">
                                  <span>{it.quantity} × {it.productName}</span>
                                  <span>{formatCurrency(it.lineTotal)}</span>
                                </div>
                              ))}
                              <div className="order-line" style={{ borderTop: '1px solid var(--border)', marginTop: 8, paddingTop: 8 }}>
                                <strong>Total</strong><strong>{formatCurrency(o.total)}</strong>
                              </div>
                            </div>
                            <div>
                              <h4>Update status</h4>
                              <div className="form-group">
                                <label>Status</label>
                                <select className="form-control" value={draft.status} onChange={(e) => setDraft({ ...draft, status: e.target.value })}>
                                  {ORDER_STATUSES.map((s) => <option key={s} value={s}>{titleCase(s)}</option>)}
                                </select>
                              </div>
                              <div className="form-group">
                                <label>Note <span className="muted small">(optional)</span></label>
                                <input className="form-control" value={draft.note} onChange={(e) => setDraft({ ...draft, note: e.target.value })} />
                              </div>
                              <button type="button" className="btn btn-primary btn-sm" disabled={busyId === o.id} onClick={() => saveStatus(o)}>{busyId === o.id ? 'Saving…' : 'Save status'}</button>
                            </div>
                            <div>
                              <h4>Shipping</h4>
                              <p className="muted small">
                                {o.shippingFullName}<br />
                                {o.shippingLine1}{o.shippingLine2 ? `, ${o.shippingLine2}` : ''}<br />
                                {o.shippingCity}, {o.shippingState} {o.shippingPostalCode}<br />
                                {o.shippingCountry}<br />
                                {o.shippingPhone}
                              </p>
                              <p className="muted small">Payment: {titleCase(o.paymentMethod)}</p>
                            </div>
                          </div>
                        </td>
                      </tr>
                    ) : null}
                  </Fragment>
                ))}
              </tbody>
            </table>
          </div>
          <Pagination page={data.page} totalPages={data.totalPages} onChange={setPage} />
        </section>
      ) : null}
    </AdminLayout>
  )
}
