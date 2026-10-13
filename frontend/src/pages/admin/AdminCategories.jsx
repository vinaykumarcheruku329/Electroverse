import { useEffect, useState } from 'react'
import { adminApi, categoryApi } from '../../api/endpoints'
import { AdminLayout } from '../../components/AdminLayout'
import { Loading, ErrorMessage, EmptyState } from '../../components/StateViews'
import { useUi } from '../../context/UiContext'
import { slugify } from '../../utils/format'

const EMPTY = { name: '', slug: '', description: '', icon: '' }

export default function AdminCategories() {
  const { toast } = useUi()
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [form, setForm] = useState(EMPTY)
  const [editingId, setEditingId] = useState(null)
  const [busy, setBusy] = useState(false)

  function load() {
    setLoading(true)
    setError(null)
    categoryApi.list().then(setCategories).catch(setError).finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  function startCreate() { setEditingId(null); setForm(EMPTY) }
  function startEdit(c) { setEditingId(c.id); setForm({ name: c.name, slug: c.slug, description: c.description || '', icon: c.icon || '' }) }

  async function save(e) {
    e.preventDefault()
    setBusy(true)
    try {
      const payload = { ...form, slug: form.slug || slugify(form.name) }
      if (editingId) {
        await adminApi.categories.update(editingId, payload)
        toast.success('Category updated.')
      } else {
        await adminApi.categories.create(payload)
        toast.success('Category created.')
      }
      setForm(EMPTY)
      setEditingId(null)
      load()
    } catch (err) { toast.error(err.message) } finally { setBusy(false) }
  }

  async function remove(c) {
    if (!window.confirm(`Delete category "${c.name}"?`)) return
    try {
      await adminApi.categories.remove(c.id)
      toast.success('Category deleted.')
      if (editingId === c.id) { setEditingId(null); setForm(EMPTY) }
      load()
    } catch (err) { toast.error(err.message) }
  }

  return (
    <AdminLayout
      title="Categories"
      subtitle="Organise the catalog taxonomy."
      actions={<button type="button" className="btn btn-primary btn-sm" onClick={startCreate}>+ New category</button>}
    >
      <div className="admin-two-col">
        <section className="card tool-card">
          <h3>All categories</h3>
          {loading ? <Loading label="Loading categories…" /> : null}
          {error ? <ErrorMessage error={error} onRetry={load} /> : null}
          {!loading && !error && categories.length === 0 ? <EmptyState title="No categories" message="Create your first category." /> : null}
          {categories.length ? (
            <div className="table-wrap">
              <table className="table">
                <thead><tr><th>Name</th><th>Slug</th><th>Products</th><th>Actions</th></tr></thead>
                <tbody>
                  {categories.map((c) => (
                    <tr key={c.id}>
                      <td><strong>{c.name}</strong>{c.description ? <div className="muted small">{c.description}</div> : null}</td>
                      <td className="muted">{c.slug}</td>
                      <td>{c.productCount}</td>
                      <td className="table-actions">
                        <button type="button" className="btn btn-ghost btn-sm" onClick={() => startEdit(c)}>Edit</button>
                        <button type="button" className="btn btn-ghost btn-sm danger" onClick={() => remove(c)}>Delete</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : null}
        </section>

        <section className="card tool-card">
          <h3>{editingId ? 'Edit category' : 'Create category'}</h3>
          <form onSubmit={save}>
            <div className="form-group">
              <label>Name</label>
              <input className="form-control" required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
            </div>
            <div className="form-group">
              <label>Slug <span className="muted small">(optional)</span></label>
              <input className="form-control" value={form.slug} onChange={(e) => setForm({ ...form, slug: e.target.value })} placeholder="auto-generated" />
            </div>
            <div className="form-group">
              <label>Icon</label>
              <input className="form-control" value={form.icon} onChange={(e) => setForm({ ...form, icon: e.target.value })} placeholder="laptop, phone…" />
            </div>
            <div className="form-group">
              <label>Description</label>
              <textarea className="form-control" rows="3" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} />
            </div>
            <div style={{ display: 'flex', gap: 10 }}>
              <button type="submit" className="btn btn-primary" disabled={busy}>{busy ? 'Saving…' : editingId ? 'Update' : 'Create'}</button>
              {editingId ? <button type="button" className="btn btn-outline" onClick={startCreate}>Cancel</button> : null}
            </div>
          </form>
        </section>
      </div>
    </AdminLayout>
  )
}
