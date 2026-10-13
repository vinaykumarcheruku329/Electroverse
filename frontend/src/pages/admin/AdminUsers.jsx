import { useEffect, useState } from 'react'
import { adminApi } from '../../api/endpoints'
import { AdminLayout } from '../../components/AdminLayout'
import { Pagination } from '../../components/Pagination'
import { Loading, ErrorMessage, EmptyState } from '../../components/StateViews'
import { useUi } from '../../context/UiContext'
import { useAuth } from '../../context/AuthContext'
import { formatDate, titleCase } from '../../utils/format'

const SIZE = 20

export default function AdminUsers() {
  const { toast } = useUi()
  const { user: current } = useAuth()
  const [page, setPage] = useState(0)
  const [data, setData] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [busyId, setBusyId] = useState(null)

  function load() {
    setLoading(true)
    setError(null)
    adminApi.users.list({ page, size: SIZE }).then(setData).catch(setError).finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [page])

  async function toggleActive(u) {
    setBusyId(u.id)
    try {
      await adminApi.users.setActive(u.id, !u.active)
      toast.success(`${u.fullName || u.email} ${u.active ? 'deactivated' : 'activated'}.`)
      load()
    } catch (err) { toast.error(err.message) } finally { setBusyId(null) }
  }

  return (
    <AdminLayout title="Users" subtitle="Manage customer and administrator accounts.">
      {loading ? <Loading label="Loading users…" /> : null}
      {error ? <ErrorMessage error={error} onRetry={load} /> : null}
      {!loading && !error && data?.content?.length === 0 ? <EmptyState title="No users" /> : null}

      {data?.content?.length ? (
        <section className="card tool-card">
          <div className="table-wrap">
            <table className="table">
              <thead>
                <tr><th>Name</th><th>Email</th><th>Role</th><th>Joined</th><th>Status</th><th>Actions</th></tr>
              </thead>
              <tbody>
                {data.content.map((u) => (
                  <tr key={u.id}>
                    <td><strong>{u.fullName || '—'}</strong>{u.phone ? <div className="muted small">{u.phone}</div> : null}</td>
                    <td>{u.email}</td>
                    <td><span className={u.role === 'ADMIN' ? 'pill pill-bronze' : 'pill pill-muted'}>{titleCase(u.role)}</span></td>
                    <td className="muted small">{formatDate(u.createdAt)}</td>
                    <td>{u.active ? <span className="pill pill-success">Active</span> : <span className="pill pill-danger">Disabled</span>}</td>
                    <td className="table-actions">
                      <button
                        type="button"
                        className="btn btn-ghost btn-sm"
                        disabled={busyId === u.id || u.id === current?.id}
                        onClick={() => toggleActive(u)}
                        title={u.id === current?.id ? 'You cannot change your own status' : undefined}
                      >
                        {u.active ? 'Deactivate' : 'Activate'}
                      </button>
                    </td>
                  </tr>
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
