import { useEffect, useState } from 'react'
import { adminApi } from '../../api/endpoints'
import { AdminLayout } from '../../components/AdminLayout'
import { Loading, ErrorMessage, EmptyState } from '../../components/StateViews'
import { useUi } from '../../context/UiContext'
import { titleCase, COMPAT_RELATIONS, COMPAT_RESULTS } from '../../utils/format'

const EMPTY = {
  ruleType: '', sourceType: '', targetType: '', sourceAttribute: '', targetAttribute: '',
  expectedValue: '', relation: 'EQUALS', result: 'COMPATIBLE', explanation: '', active: true
}

export default function AdminRules() {
  const { toast } = useUi()
  const [rules, setRules] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [form, setForm] = useState(EMPTY)
  const [editingId, setEditingId] = useState(null)
  const [busy, setBusy] = useState(false)

  function load() {
    setLoading(true)
    setError(null)
    adminApi.rules.list().then(setRules).catch(setError).finally(() => setLoading(false))
  }

  useEffect(() => { load() }, [])

  function startEdit(r) {
    setEditingId(r.id)
    setForm({
      ruleType: r.ruleType || '', sourceType: r.sourceType || '', targetType: r.targetType || '',
      sourceAttribute: r.sourceAttribute || '', targetAttribute: r.targetAttribute || '',
      expectedValue: r.expectedValue || '', relation: r.relation || 'EQUALS', result: r.result || 'COMPATIBLE',
      explanation: r.explanation || '', active: !!r.active
    })
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  function reset() { setEditingId(null); setForm(EMPTY) }

  async function save(e) {
    e.preventDefault()
    setBusy(true)
    try {
      const payload = { ...form, targetAttribute: form.targetAttribute || undefined, expectedValue: form.expectedValue || undefined }
      if (editingId) { await adminApi.rules.update(editingId, payload); toast.success('Rule updated.') }
      else { await adminApi.rules.create(payload); toast.success('Rule created.') }
      reset()
      load()
    } catch (err) { toast.error(err.message) } finally { setBusy(false) }
  }

  async function remove(r) {
    if (!window.confirm(`Delete rule "${r.ruleType}"?`)) return
    try {
      await adminApi.rules.remove(r.id)
      toast.success('Rule deleted.')
      if (editingId === r.id) reset()
      load()
    } catch (err) { toast.error(err.message) }
  }

  return (
    <AdminLayout title="Compatibility Rules" subtitle="Define how component attributes relate to one another." actions={<button type="button" className="btn btn-primary btn-sm" onClick={reset}>+ New rule</button>}>
      <div className="admin-two-col">
        <section className="card tool-card">
          <h3>Rules ({rules.length})</h3>
          {loading ? <Loading label="Loading rules…" /> : null}
          {error ? <ErrorMessage error={error} onRetry={load} /> : null}
          {!loading && !error && rules.length === 0 ? <EmptyState title="No rules" message="Add your first compatibility rule." /> : null}
          {rules.length ? (
            <div className="table-wrap">
              <table className="table">
                <thead><tr><th>Rule</th><th>Pair</th><th>Logic</th><th>Result</th><th>Active</th><th>Actions</th></tr></thead>
                <tbody>
                  {rules.map((r) => (
                    <tr key={r.id}>
                      <td><strong>{r.ruleType}</strong></td>
                      <td className="muted small">{r.sourceType} → {r.targetType}</td>
                      <td className="muted small">{r.sourceAttribute} {r.relation} {r.targetAttribute || r.expectedValue || ''}</td>
                      <td><span className={`pill ${r.result === 'COMPATIBLE' ? 'pill-success' : r.result === 'INCOMPATIBLE' ? 'pill-danger' : 'pill-warn'}`}>{titleCase(r.result)}</span></td>
                      <td>{r.active ? 'Yes' : 'No'}</td>
                      <td className="table-actions">
                        <button type="button" className="btn btn-ghost btn-sm" onClick={() => startEdit(r)}>Edit</button>
                        <button type="button" className="btn btn-ghost btn-sm danger" onClick={() => remove(r)}>Delete</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : null}
        </section>

        <section className="card tool-card">
          <h3>{editingId ? 'Edit rule' : 'Create rule'}</h3>
          <form onSubmit={save}>
            <div className="form-group"><label>Rule type</label><input className="form-control" required value={form.ruleType} onChange={(e) => setForm({ ...form, ruleType: e.target.value })} placeholder="socket-match" /></div>
            <div className="form-row">
              <div className="form-group"><label>Source type</label><input className="form-control" required value={form.sourceType} onChange={(e) => setForm({ ...form, sourceType: e.target.value })} placeholder="cpu" /></div>
              <div className="form-group"><label>Target type</label><input className="form-control" required value={form.targetType} onChange={(e) => setForm({ ...form, targetType: e.target.value })} placeholder="motherboard" /></div>
            </div>
            <div className="form-row">
              <div className="form-group"><label>Source attribute</label><input className="form-control" required value={form.sourceAttribute} onChange={(e) => setForm({ ...form, sourceAttribute: e.target.value })} placeholder="socket" /></div>
              <div className="form-group"><label>Target attribute</label><input className="form-control" value={form.targetAttribute} onChange={(e) => setForm({ ...form, targetAttribute: e.target.value })} placeholder="socket" /></div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>Relation</label>
                <select className="form-control" value={form.relation} onChange={(e) => setForm({ ...form, relation: e.target.value })}>
                  {COMPAT_RELATIONS.map((r) => <option key={r} value={r}>{r}</option>)}
                </select>
              </div>
              <div className="form-group">
                <label>Result</label>
                <select className="form-control" value={form.result} onChange={(e) => setForm({ ...form, result: e.target.value })}>
                  {COMPAT_RESULTS.map((r) => <option key={r} value={r}>{titleCase(r)}</option>)}
                </select>
              </div>
            </div>
            <div className="form-group"><label>Expected value <span className="muted small">(optional)</span></label><input className="form-control" value={form.expectedValue} onChange={(e) => setForm({ ...form, expectedValue: e.target.value })} /></div>
            <div className="form-group"><label>Explanation</label><textarea className="form-control" rows="3" required value={form.explanation} onChange={(e) => setForm({ ...form, explanation: e.target.value })} /></div>
            <label className="check"><input type="checkbox" checked={form.active} onChange={(e) => setForm({ ...form, active: e.target.checked })} /> Active</label>
            <div style={{ display: 'flex', gap: 10, marginTop: 12 }}>
              <button type="submit" className="btn btn-primary" disabled={busy}>{busy ? 'Saving…' : editingId ? 'Update rule' : 'Create rule'}</button>
              {editingId ? <button type="button" className="btn btn-outline" onClick={reset}>Cancel</button> : null}
            </div>
          </form>
        </section>
      </div>
    </AdminLayout>
  )
}
