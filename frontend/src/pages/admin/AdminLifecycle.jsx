import { useEffect, useState } from 'react'
import { adminApi, catalogApi } from '../../api/endpoints'
import { AdminLayout } from '../../components/AdminLayout'
import { Loading, ErrorMessage, EmptyState } from '../../components/StateViews'
import { useUi } from '../../context/UiContext'
import { formatDate } from '../../utils/format'

const EMPTY = {
  productId: '', repairability: 70, upgradeability: 70, manufacturerSupport: 70, warranty: 70,
  energyEfficiency: 70, explanation: '', assessmentDate: '', methodologyVersion: 'v1'
}

export default function AdminLifecycle() {
  const { toast } = useUi()
  const [assessments, setAssessments] = useState([])
  const [products, setProducts] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [form, setForm] = useState(EMPTY)
  const [busy, setBusy] = useState(false)

  function load() {
    setLoading(true)
    setError(null)
    adminApi.lifecycle.list().then(setAssessments).catch(setError).finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])
  useEffect(() => { catalogApi.products({ size: 60, sort: 'name' }).then((d) => setProducts(d.content || [])).catch(() => {}) }, [])

  async function save(e) {
    e.preventDefault()
    setBusy(true)
    try {
      await adminApi.lifecycle.create({
        productId: Number(form.productId),
        repairability: Number(form.repairability),
        upgradeability: Number(form.upgradeability),
        manufacturerSupport: Number(form.manufacturerSupport),
        warranty: Number(form.warranty),
        energyEfficiency: Number(form.energyEfficiency),
        explanation: form.explanation || undefined,
        assessmentDate: form.assessmentDate || null,
        methodologyVersion: form.methodologyVersion || undefined
      })
      toast.success('Lifecycle assessment created.')
      setForm(EMPTY)
      load()
    } catch (err) { toast.error(err.message) } finally { setBusy(false) }
  }

  async function remove(a) {
    if (!window.confirm(`Delete assessment for "${a.productName}"?`)) return
    try {
      await adminApi.lifecycle.remove(a.id)
      toast.success('Assessment deleted.')
      load()
    } catch (err) { toast.error(err.message) }
  }

  const scores = [
    ['repairability', 'Repairability'], ['upgradeability', 'Upgradeability'],
    ['manufacturerSupport', 'Support'], ['warranty', 'Warranty'], ['energyEfficiency', 'Energy']
  ]

  return (
    <AdminLayout title="Lifecycle" subtitle="Score products for repairability, support and longevity." actions={<button type="button" className="btn btn-outline btn-sm" onClick={load}>Refresh</button>}>
      <div className="admin-two-col">
        <section className="card tool-card">
          <h3>Recent assessments</h3>
          {loading ? <Loading label="Loading assessments…" /> : null}
          {error ? <ErrorMessage error={error} onRetry={load} /> : null}
          {!loading && !error && assessments.length === 0 ? <EmptyState title="No assessments" message="Create the first lifecycle assessment." /> : null}
          {assessments.length ? (
            <div className="table-wrap">
              <table className="table">
                <thead><tr><th>Product</th><th>Overall</th><th>Repair</th><th>Upgrade</th><th>Support</th><th>Assessed</th><th></th></tr></thead>
                <tbody>
                  {assessments.map((a) => (
                    <tr key={a.id}>
                      <td><strong>{a.productName}</strong><div className="muted small">{a.methodologyVersion}</div></td>
                      <td><strong>{a.overall}</strong></td>
                      <td>{a.repairability}</td>
                      <td>{a.upgradeability}</td>
                      <td>{a.manufacturerSupport}</td>
                      <td className="muted small">{formatDate(a.assessmentDate)}</td>
                      <td className="table-actions"><button type="button" className="btn btn-ghost btn-sm danger" onClick={() => remove(a)}>Delete</button></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : null}
        </section>

        <section className="card tool-card">
          <h3>New assessment</h3>
          <form onSubmit={save}>
            <div className="form-group">
              <label>Product</label>
              <select className="form-control" required value={form.productId} onChange={(e) => setForm({ ...form, productId: e.target.value })}>
                <option value="">Select product…</option>
                {products.map((p) => <option key={p.id} value={p.id}>{p.name}</option>)}
              </select>
            </div>
            {scores.map(([key, label]) => (
              <div className="form-group" key={key}>
                <label style={{ display: 'flex', justifyContent: 'space-between' }}><span>{label}</span><span className="muted">{form[key]}</span></label>
                <input type="range" min="0" max="100" value={form[key]} onChange={(e) => setForm({ ...form, [key]: e.target.value })} style={{ width: '100%', accentColor: 'var(--bronze)' }} />
              </div>
            ))}
            <div className="form-row">
              <div className="form-group"><label>Assessment date</label><input className="form-control" type="date" value={form.assessmentDate} onChange={(e) => setForm({ ...form, assessmentDate: e.target.value })} /></div>
              <div className="form-group"><label>Methodology</label><input className="form-control" value={form.methodologyVersion} onChange={(e) => setForm({ ...form, methodologyVersion: e.target.value })} /></div>
            </div>
            <div className="form-group"><label>Explanation</label><textarea className="form-control" rows="3" value={form.explanation} onChange={(e) => setForm({ ...form, explanation: e.target.value })} /></div>
            <button type="submit" className="btn btn-primary" disabled={busy}>{busy ? 'Saving…' : 'Create assessment'}</button>
          </form>
        </section>
      </div>
    </AdminLayout>
  )
}
