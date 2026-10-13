import { useEffect, useMemo, useState } from 'react'
import { adminApi } from '../../api/endpoints'
import { AdminLayout } from '../../components/AdminLayout'
import { Rating } from '../../components/Rating'
import { Loading, ErrorMessage, EmptyState } from '../../components/StateViews'
import { useUi } from '../../context/UiContext'
import { formatDate, titleCase, REVIEW_STATUSES } from '../../utils/format'

export default function AdminReviews() {
  const { toast } = useUi()
  const [reviews, setReviews] = useState([])
  const [filter, setFilter] = useState('ALL')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [busyId, setBusyId] = useState(null)

  function load() {
    setLoading(true)
    setError(null)
    adminApi.reviews.list().then(setReviews).catch(setError).finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  async function moderate(r, status) {
    setBusyId(r.id)
    try {
      await adminApi.reviews.setStatus(r.id, status)
      toast.success(`Review marked ${titleCase(status)}.`)
      load()
    } catch (err) { toast.error(err.message) } finally { setBusyId(null) }
  }

  async function remove(r) {
    if (!window.confirm('Delete this review permanently?')) return
    setBusyId(r.id)
    try {
      await adminApi.reviews.remove(r.id)
      toast.success('Review deleted.')
      load()
    } catch (err) { toast.error(err.message) } finally { setBusyId(null) }
  }

  const filtered = useMemo(() => (filter === 'ALL' ? reviews : reviews.filter((r) => r.status === filter)), [reviews, filter])

  const counts = useMemo(() => reviews.reduce((acc, r) => ({ ...acc, [r.status]: (acc[r.status] || 0) + 1 }), {}), [reviews])

  return (
    <AdminLayout title="Reviews" subtitle="Moderate customer feedback across products." actions={<button type="button" className="btn btn-outline btn-sm" onClick={load}>Refresh</button>}>
      <div className="filter-tabs" style={{ marginBottom: 16 }}>
        {['ALL', ...REVIEW_STATUSES].map((s) => (
          <button key={s} type="button" className={filter === s ? 'filter-tab active' : 'filter-tab'} onClick={() => setFilter(s)}>
            {titleCase(s)}{s !== 'ALL' ? ` (${counts[s] || 0})` : ` (${reviews.length})`}
          </button>
        ))}
      </div>

      {loading ? <Loading label="Loading reviews…" /> : null}
      {error ? <ErrorMessage error={error} onRetry={load} /> : null}
      {!loading && !error && filtered.length === 0 ? <EmptyState title="No reviews" message="Nothing matches this filter." /> : null}

      {filtered.length ? (
        <div className="review-admin-list">
          {filtered.map((r) => (
            <article key={r.id} className="card tool-card review-admin-item">
              <div style={{ display: 'flex', justifyContent: 'space-between', gap: 12, flexWrap: 'wrap' }}>
                <div>
                  <strong>{r.title || 'Untitled review'}</strong>
                  <div className="muted small">{r.productName} · by {r.userName} · {formatDate(r.createdAt, true)}</div>
                </div>
                <div style={{ textAlign: 'right' }}>
                  <Rating value={r.rating} showCount={false} size="sm" />
                  <div style={{ marginTop: 4 }}>
                    <span className={`pill ${r.status === 'APPROVED' ? 'pill-success' : r.status === 'REJECTED' ? 'pill-danger' : 'pill-warn'}`}>{titleCase(r.status)}</span>
                  </div>
                </div>
              </div>
              {r.comment ? <p style={{ marginTop: 10 }}>{r.comment}</p> : null}
              <div className="table-actions" style={{ marginTop: 12 }}>
                {r.status !== 'APPROVED' ? <button type="button" className="btn btn-primary btn-sm" disabled={busyId === r.id} onClick={() => moderate(r, 'APPROVED')}>Approve</button> : null}
                {r.status !== 'REJECTED' ? <button type="button" className="btn btn-outline btn-sm" disabled={busyId === r.id} onClick={() => moderate(r, 'REJECTED')}>Reject</button> : null}
                <button type="button" className="btn btn-ghost btn-sm danger" disabled={busyId === r.id} onClick={() => remove(r)}>Delete</button>
              </div>
            </article>
          ))}
        </div>
      ) : null}
    </AdminLayout>
  )
}
