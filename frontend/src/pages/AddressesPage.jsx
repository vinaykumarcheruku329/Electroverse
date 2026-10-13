import { useEffect, useState } from 'react'
import { addressApi } from '../api/endpoints'
import { Loading, ErrorMessage, EmptyState } from '../components/StateViews'
import { useUi } from '../context/UiContext'

const EMPTY_FORM = {
  fullName: '', phone: '', line1: '', line2: '', city: '', state: '', postalCode: '', country: '', defaultAddress: false
}

export default function AddressesPage() {
  const { toast } = useUi()
  const [addresses, setAddresses] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [form, setForm] = useState(EMPTY_FORM)
  const [editingId, setEditingId] = useState(null)
  const [busy, setBusy] = useState(false)

  async function load() {
    setLoading(true)
    setError(null)
    try {
      setAddresses(await addressApi.list())
    } catch (err) { setError(err) } finally { setLoading(false) }
  }

  useEffect(() => { load() }, [])

  function startEdit(a) {
    setEditingId(a.id)
    setForm({
      fullName: a.fullName, phone: a.phone, line1: a.line1, line2: a.line2 || '',
      city: a.city, state: a.state, postalCode: a.postalCode, country: a.country,
      defaultAddress: a.defaultAddress
    })
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setBusy(true)
    try {
      if (editingId) {
        await addressApi.update(editingId, form)
        toast.success('Address updated.')
      } else {
        await addressApi.create(form)
        toast.success('Address added.')
      }
      setForm(EMPTY_FORM)
      setEditingId(null)
      await load()
    } catch (err) { toast.error(err.message) } finally { setBusy(false) }
  }

  async function makeDefault(a) {
    try {
      await addressApi.makeDefault(a.id)
      toast.success('Default address updated.')
      await load()
    } catch (err) { toast.error(err.message) }
  }

  async function remove(a) {
    if (!window.confirm(`Delete address for ${a.fullName}?`)) return
    try {
      await addressApi.remove(a.id)
      toast.success('Address deleted.')
      await load()
    } catch (err) { toast.error(err.message) }
  }

  function field(f) {
    return (e) => setForm((prev) => ({ ...prev, [f]: e.target.value }))
  }

  return (
    <div className="container" style={{ paddingTop: 24, paddingBottom: 24 }}>
      <div className="page-hero" style={{ marginBottom: 24 }}>
        <h1>Addresses</h1>
        <p>Shipping addresses used at checkout.</p>
      </div>

      <div className="card" style={{ padding: 24, marginBottom: 24 }}>
        <h3>{editingId ? 'Edit address' : 'Add address'}</h3>
        <form onSubmit={handleSubmit}>
          <div className="form-row">
            <div className="form-group"><label>Full name</label><input className="form-control" required value={form.fullName} onChange={field('fullName')} /></div>
            <div className="form-group"><label>Phone</label><input className="form-control" required value={form.phone} onChange={field('phone')} /></div>
          </div>
          <div className="form-group"><label>Address line 1</label><input className="form-control" required value={form.line1} onChange={field('line1')} /></div>
          <div className="form-group"><label>Address line 2 (optional)</label><input className="form-control" value={form.line2} onChange={field('line2')} /></div>
          <div className="form-row">
            <div className="form-group"><label>City</label><input className="form-control" required value={form.city} onChange={field('city')} /></div>
            <div className="form-group"><label>State</label><input className="form-control" required value={form.state} onChange={field('state')} /></div>
          </div>
          <div className="form-row">
            <div className="form-group"><label>Postal code</label><input className="form-control" required value={form.postalCode} onChange={field('postalCode')} /></div>
            <div className="form-group"><label>Country</label><input className="form-control" required value={form.country} onChange={field('country')} /></div>
          </div>
          <div className="checkbox-row" style={{ marginBottom: 16 }}>
            <input type="checkbox" checked={form.defaultAddress} onChange={(e) => setForm({ ...form, defaultAddress: e.target.checked })} id="def-addr" />
            <label htmlFor="def-addr">Set as default address</label>
          </div>
          <div style={{ display: 'flex', gap: 10 }}>
            <button type="submit" className="btn btn-primary" disabled={busy}>
              {busy ? 'Saving…' : editingId ? 'Save changes' : 'Add address'}
            </button>
            {editingId ? <button type="button" className="btn btn-outline" onClick={() => { setEditingId(null); setForm(EMPTY_FORM) }}>Cancel</button> : null}
          </div>
        </form>
      </div>

      {loading ? <Loading /> : null}
      {!loading && error ? <ErrorMessage error={error} onRetry={load} /> : null}
      {!loading && !error && addresses.length === 0 ? <EmptyState title="No addresses yet" message="Add your first shipping address above." /> : null}

      <div className="tool-grid">
        {addresses.map((a) => (
          <div key={a.id} className="card tool-card">
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
              <strong>{a.fullName}</strong>
              {a.defaultAddress ? <span className="pill pill-bronze">Default</span> : null}
            </div>
            <p className="muted" style={{ marginBottom: 12 }}>{a.phone}</p>
            <p className="muted small" style={{ marginBottom: 12 }}>
              {a.line1}{a.line2 ? `, ${a.line2}` : ''}<br />{a.city}, {a.state} {a.postalCode}<br />{a.country}
            </p>
            <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
              {!a.defaultAddress ? <button className="btn btn-outline btn-sm" onClick={() => makeDefault(a)}>Set default</button> : null}
              <button className="btn btn-outline btn-sm" onClick={() => startEdit(a)}>Edit</button>
              <button className="btn btn-danger-outline btn-sm" onClick={() => remove(a)}>Delete</button>
            </div>
          </div>
        ))}
      </div>
    </div>
  )
}