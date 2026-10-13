import { useState } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useUi } from '../context/UiContext'
import { formatDate } from '../utils/format'

export default function AccountPage() {
  const { user, updateProfile, isAdmin } = useAuth()
  const { toast } = useUi()
  const [form, setForm] = useState({ fullName: user?.fullName || '', phone: user?.phone || '' })
  const [busy, setBusy] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setBusy(true)
    try {
      await updateProfile(form)
      toast.success('Profile updated.')
    } catch (err) { toast.error(err.message) } finally { setBusy(false) }
  }

  return (
    <div className="container" style={{ paddingTop: 24, paddingBottom: 24 }}>
      <div className="page-hero" style={{ marginBottom: 24 }}>
        <h1>My Account</h1>
        <p>Manage your profile and personal details.</p>
      </div>
      <div className="cart-layout" style={{ gridTemplateColumns: '1fr 1fr' }}>
        <div className="card" style={{ padding: 24 }}>
          <h3>Profile</h3>
          <div className="kv" style={{ margin: '16px 0' }}>
            <div className="kv-item"><span>Email</span><strong>{user.email}</strong></div>
            <div className="kv-item"><span>Role</span><strong>{user.role}</strong></div>
            <div className="kv-item"><span>Member since</span><strong>{formatDate(user.createdAt)}</strong></div>
            <div className="kv-item"><span>Status</span><strong>{user.active ? 'Active' : 'Inactive'}</strong></div>
          </div>
          {isAdmin ? <Link to="/admin" className="btn btn-outline">Open Admin Console →</Link> : null}
        </div>

        <div className="card" style={{ padding: 24 }}>
          <h3>Edit profile</h3>
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label htmlFor="pf-name">Full name</label>
              <input id="pf-name" className="form-control" required value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} />
            </div>
            <div className="form-group">
              <label htmlFor="pf-phone">Phone</label>
              <input id="pf-phone" className="form-control" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} />
            </div>
            <button type="submit" className="btn btn-primary" disabled={busy}>{busy ? 'Saving…' : 'Save changes'}</button>
          </form>
        </div>
      </div>
    </div>
  )
}