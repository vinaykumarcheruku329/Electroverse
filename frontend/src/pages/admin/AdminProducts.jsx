import { useEffect, useState } from 'react'
import { adminApi, categoryApi } from '../../api/endpoints'
import { AdminLayout } from '../../components/AdminLayout'
import { Pagination } from '../../components/Pagination'
import { Loading, ErrorMessage, EmptyState } from '../../components/StateViews'
import { useUi } from '../../context/UiContext'
import { formatCurrency } from '../../utils/format'

const SIZE = 12

const EMPTY_FORM = {
  name: '', brand: '', categoryId: '', price: '', compareAtPrice: '', currency: 'INR',
  stock: '0', warrantyMonths: '', primaryImageUrl: '', releaseDate: '', description: '',
  featured: false, active: true, specs: [], images: []
}

export default function AdminProducts() {
  const { toast } = useUi()
  const [page, setPage] = useState(0)
  const [query, setQuery] = useState('')
  const [search, setSearch] = useState('')
  const [data, setData] = useState(null)
  const [categories, setCategories] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [form, setForm] = useState(EMPTY_FORM)
  const [editingId, setEditingId] = useState(null)
  const [showForm, setShowForm] = useState(false)
  const [busy, setBusy] = useState(false)
  const [stockDraft, setStockDraft] = useState({})

  function load() {
    setLoading(true)
    setError(null)
    adminApi.products.list({ page, size: SIZE, q: search || undefined, sort: 'name' })
      .then(setData).catch(setError).finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [page, search])
  useEffect(() => { categoryApi.list().then(setCategories).catch(() => {}) }, [])

  function startCreate() {
    setForm(EMPTY_FORM)
    setEditingId(null)
    setShowForm(true)
  }

  async function startEdit(id) {
    try {
      const p = await adminApi.products.get(id)
      setForm({
        name: p.name || '', brand: p.brand || '', categoryId: String(p.categoryId || ''),
        price: String(p.price ?? ''), compareAtPrice: p.compareAtPrice != null ? String(p.compareAtPrice) : '',
        currency: p.currency || 'INR', stock: String(p.stock ?? 0),
        warrantyMonths: p.warrantyMonths != null ? String(p.warrantyMonths) : '',
        primaryImageUrl: p.primaryImageUrl || '', releaseDate: p.releaseDate || '',
        description: p.description || '', featured: !!p.featured, active: !!p.active,
        specs: (p.specs || []).map((s) => ({ specGroup: s.group || '', specKey: s.key || '', specValue: s.value || '', numericValue: s.numericValue != null ? String(s.numericValue) : '' })),
        images: (p.images || []).map((i) => i.url)
      })
      setEditingId(id)
      setShowForm(true)
      window.scrollTo({ top: 0, behavior: 'smooth' })
    } catch (err) { toast.error(err.message) }
  }

  function submitSearch(e) {
    e.preventDefault()
    setPage(0)
    setSearch(query.trim())
  }

  function updateSpec(idx, field, value) {
    setForm((f) => ({ ...f, specs: f.specs.map((s, i) => (i === idx ? { ...s, [field]: value } : s)) }))
  }

  async function save(e) {
    e.preventDefault()
    setBusy(true)
    try {
      const payload = {
        name: form.name,
        brand: form.brand || undefined,
        categoryId: Number(form.categoryId),
        description: form.description || undefined,
        price: Number(form.price),
        compareAtPrice: form.compareAtPrice === '' ? null : Number(form.compareAtPrice),
        currency: form.currency || 'INR',
        stock: Number(form.stock || 0),
        warrantyMonths: form.warrantyMonths === '' ? null : Number(form.warrantyMonths),
        primaryImageUrl: form.primaryImageUrl || undefined,
        releaseDate: form.releaseDate || null,
        featured: form.featured,
        active: form.active,
        specs: form.specs.filter((s) => s.specKey && s.specValue).map((s) => ({
          specGroup: s.specGroup || undefined,
          specKey: s.specKey,
          specValue: s.specValue,
          numericValue: s.numericValue === '' ? null : Number(s.numericValue)
        })),
        images: form.images.filter(Boolean)
      }
      if (editingId) {
        await adminApi.products.update(editingId, payload)
        toast.success('Product updated.')
      } else {
        await adminApi.products.create(payload)
        toast.success('Product created.')
      }
      setShowForm(false)
      setForm(EMPTY_FORM)
      setEditingId(null)
      load()
    } catch (err) { toast.error(err.message) } finally { setBusy(false) }
  }

  async function saveStock(id) {
    try {
      await adminApi.products.updateStock(id, Number(stockDraft[id]))
      toast.success('Stock updated.')
      load()
    } catch (err) { toast.error(err.message) }
  }

  async function toggleActive(id, active) {
    try {
      await adminApi.products.setActive(id, active)
      toast.success(active ? 'Product activated.' : 'Product deactivated.')
      load()
    } catch (err) { toast.error(err.message) }
  }

  return (
    <AdminLayout
      title="Products"
      subtitle="Create, edit market products and manage inventory."
      actions={<button type="button" className="btn btn-primary btn-sm" onClick={startCreate}>+ New product</button>}
    >
      {showForm ? (
        <section className="card tool-card" style={{ marginBottom: 18 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <h3>{editingId ? 'Edit product' : 'Create product'}</h3>
            <button type="button" className="btn btn-ghost btn-sm" onClick={() => { setShowForm(false); setForm(EMPTY_FORM); setEditingId(null) }}>Close</button>
          </div>
          <form onSubmit={save}>
            <div className="form-row">
              <div className="form-group"><label>Name</label><input className="form-control" required value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} /></div>
              <div className="form-group"><label>Brand</label><input className="form-control" value={form.brand} onChange={(e) => setForm({ ...form, brand: e.target.value })} /></div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>Category</label>
                <select className="form-control" required value={form.categoryId} onChange={(e) => setForm({ ...form, categoryId: e.target.value })}>
                  <option value="">Select…</option>
                  {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
                </select>
              </div>
              <div className="form-group"><label>Release date</label><input className="form-control" type="date" value={form.releaseDate} onChange={(e) => setForm({ ...form, releaseDate: e.target.value })} /></div>
            </div>
            <div className="form-row">
              <div className="form-group"><label>Price</label><input className="form-control" type="number" step="0.01" min="0.01" required value={form.price} onChange={(e) => setForm({ ...form, price: e.target.value })} /></div>
              <div className="form-group"><label>Compare-at price</label><input className="form-control" type="number" step="0.01" min="0" value={form.compareAtPrice} onChange={(e) => setForm({ ...form, compareAtPrice: e.target.value })} /></div>
              <div className="form-group"><label>Currency</label><input className="form-control" maxLength="3" value={form.currency} onChange={(e) => setForm({ ...form, currency: e.target.value.toUpperCase() })} /></div>
            </div>
            <div className="form-row">
              <div className="form-group"><label>Stock</label><input className="form-control" type="number" min="0" value={form.stock} onChange={(e) => setForm({ ...form, stock: e.target.value })} /></div>
              <div className="form-group"><label>Warranty (months)</label><input className="form-control" type="number" min="0" value={form.warrantyMonths} onChange={(e) => setForm({ ...form, warrantyMonths: e.target.value })} /></div>
              <div className="form-group"><label>Primary image URL</label><input className="form-control" value={form.primaryImageUrl} onChange={(e) => setForm({ ...form, primaryImageUrl: e.target.value })} /></div>
            </div>
            <div className="form-group"><label>Description</label><textarea className="form-control" rows="3" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} /></div>
            <div style={{ display: 'flex', gap: 20, margin: '8px 0' }}>
              <label className="check"><input type="checkbox" checked={form.featured} onChange={(e) => setForm({ ...form, featured: e.target.checked })} /> Featured</label>
              <label className="check"><input type="checkbox" checked={form.active} onChange={(e) => setForm({ ...form, active: e.target.checked })} /> Active</label>
            </div>

            <h4>Images</h4>
            {form.images.map((url, i) => (
              <div key={i} className="form-row" style={{ gridTemplateColumns: '1fr auto', marginBottom: 8 }}>
                <input className="form-control" placeholder="Image URL" value={url} onChange={(e) => setForm({ ...form, images: form.images.map((u, j) => (j === i ? e.target.value : u)) })} />
                <button type="button" className="btn btn-ghost btn-sm" onClick={() => setForm({ ...form, images: form.images.filter((_, j) => j !== i) })}>✕</button>
              </div>
            ))}
            <button type="button" className="btn btn-ghost btn-sm" onClick={() => setForm({ ...form, images: [...form.images, ''] })}>+ Add image</button>

            <h4 style={{ marginTop: 16 }}>Specifications</h4>
            {form.specs.map((s, i) => (
              <div key={i} className="form-row" style={{ gridTemplateColumns: '1fr 1fr 1.4fr 0.8fr auto', marginBottom: 8 }}>
                <input className="form-control" placeholder="Group" value={s.specGroup} onChange={(e) => updateSpec(i, 'specGroup', e.target.value)} />
                <input className="form-control" placeholder="Key" value={s.specKey} onChange={(e) => updateSpec(i, 'specKey', e.target.value)} />
                <input className="form-control" placeholder="Value" value={s.specValue} onChange={(e) => updateSpec(i, 'specValue', e.target.value)} />
                <input className="form-control" placeholder="Numeric" type="number" step="any" value={s.numericValue} onChange={(e) => updateSpec(i, 'numericValue', e.target.value)} />
                <button type="button" className="btn btn-ghost btn-sm" onClick={() => setForm({ ...form, specs: form.specs.filter((_, j) => j !== i) })}>✕</button>
              </div>
            ))}
            <button type="button" className="btn btn-ghost btn-sm" onClick={() => setForm({ ...form, specs: [...form.specs, { specGroup: '', specKey: '', specValue: '', numericValue: '' }] })}>+ Add specification</button>

            <div style={{ marginTop: 16, display: 'flex', gap: 10 }}>
              <button type="submit" className="btn btn-primary" disabled={busy}>{busy ? 'Saving…' : editingId ? 'Update product' : 'Create product'}</button>
              <button type="button" className="btn btn-outline" onClick={() => { setShowForm(false); setForm(EMPTY_FORM); setEditingId(null) }}>Cancel</button>
            </div>
          </form>
        </section>
      ) : null}

      <section className="card tool-card">
        <form onSubmit={submitSearch} style={{ display: 'flex', gap: 10, marginBottom: 14 }}>
          <input className="form-control" placeholder="Search products…" value={query} onChange={(e) => setQuery(e.target.value)} />
          <button type="submit" className="btn btn-outline">Search</button>
        </form>

        {loading ? <Loading label="Loading products…" /> : null}
        {error ? <ErrorMessage error={error} onRetry={load} /> : null}
        {!loading && !error && data?.content?.length === 0 ? <EmptyState title="No products" message="Try a different search or create a product." /> : null}

        {data?.content?.length ? (
          <>
            <div className="table-wrap">
              <table className="table">
                <thead>
                  <tr><th>Name</th><th>Category</th><th>Price</th><th>Stock</th><th>Featured</th><th>Quick stock</th><th>Actions</th></tr>
                </thead>
                <tbody>
                  {data.content.map((p) => (
                    <tr key={p.id}>
                      <td>
                        <strong>{p.name}</strong>
                        <div className="muted small">{p.brand}</div>
                      </td>
                      <td className="muted small">{p.categoryName}</td>
                      <td>{formatCurrency(p.price)}</td>
                      <td className={p.stock <= 5 ? 'stock-low' : ''}>{p.stock}</td>
                      <td>{p.featured ? <span className="pill pill-bronze">Featured</span> : <span className="muted small">—</span>}</td>
                      <td>
                        <div style={{ display: 'flex', gap: 6 }}>
                          <input className="form-control form-control-sm" style={{ width: 72 }} type="number" min="0" value={stockDraft[p.id] ?? p.stock} onChange={(e) => setStockDraft({ ...stockDraft, [p.id]: e.target.value })} />
                          <button type="button" className="btn btn-ghost btn-sm" disabled={stockDraft[p.id] === undefined || Number(stockDraft[p.id]) === p.stock} onClick={() => saveStock(p.id)}>Save</button>
                        </div>
                      </td>
                      <td className="table-actions">
                        <button type="button" className="btn btn-ghost btn-sm" onClick={() => startEdit(p.id)}>Edit</button>
                        <button type="button" className="btn btn-ghost btn-sm danger" onClick={() => window.confirm(`Deactivate "${p.name}"?`) ? toggleActive(p.id, false) : null}>Deactivate</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <Pagination page={data.page} totalPages={data.totalPages} onChange={setPage} />
          </>
        ) : null}
      </section>
    </AdminLayout>
  )
}
