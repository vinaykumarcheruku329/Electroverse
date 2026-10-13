import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { addressApi, orderApi } from '../api/endpoints'
import { useCart } from '../context/CartContext'
import { useUi } from '../context/UiContext'
import { Loading, ErrorMessage, EmptyState } from '../components/StateViews'
import { formatCurrency, PAYMENT_METHODS } from '../utils/format'

export default function CheckoutPage() {
  const { cart, refresh } = useCart()
  const { toast } = useUi()
  const navigate = useNavigate()

  const [addresses, setAddresses] = useState([])
  const [addressId, setAddressId] = useState('')
  const [paymentMethod, setPaymentMethod] = useState('SIMULATED_CARD')
  const [notes, setNotes] = useState('')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [busy, setBusy] = useState(false)

  useEffect(() => {
    let active = true
    addressApi.list()
      .then((list) => {
        if (!active) return
        setAddresses(list)
        const def = list.find((a) => a.defaultAddress) || list[0]
        if (def) setAddressId(String(def.id))
      })
      .catch((err) => { if (active) setError(err) })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [])

  const items = cart?.items || []

  async function placeOrder(e) {
    e.preventDefault()
    if (!addressId) { toast.error('Please select a shipping address.'); return }
    setBusy(true)
    try {
      const order = await orderApi.checkout({
        addressId: Number(addressId),
        paymentMethod,
        notes: notes || undefined
      })
      await refresh()
      toast.success(`Order ${order.orderNumber} placed!`)
      navigate(`/orders/${order.id}`)
    } catch (err) {
      toast.error(err.message)
      setError(err)
    } finally {
      setBusy(false)
    }
  }

  if (loading) return <div className="container"><Loading label="Preparing checkout…" /></div>
  if (items.length === 0) {
    return (
      <div className="container" style={{ padding: '40px 20px' }}>
        <EmptyState title="Nothing to check out" message="Your cart is empty." action={<Link to="/products" className="btn btn-primary">Shop products</Link>} />
      </div>
    )
  }

  return (
    <div className="container" style={{ paddingTop: 24, paddingBottom: 24 }}>
      <div className="page-hero" style={{ marginBottom: 24 }}>
        <h1>Checkout</h1>
        <p>Review your order and confirm shipping details.</p>
      </div>

      {addresses.length === 0 && !loading ? (
        <div className="card tool-card">
          <p>You need a shipping address before checking out.</p>
          <Link to="/addresses" className="btn btn-primary">Add an address</Link>
        </div>
      ) : (
        <form className="cart-layout" onSubmit={placeOrder}>
          <div>
            <div className="card" style={{ padding: 22, marginBottom: 18 }}>
              <h3>Shipping address</h3>
              {addresses.map((a) => (
                <label key={a.id} className="address-option" style={{ display: 'flex', gap: 10, alignItems: 'flex-start', padding: 12, border: '1px solid var(--border)', borderRadius: 10, marginBottom: 10, cursor: 'pointer' }}>
                  <input type="radio" name="address" value={a.id} checked={Number(addressId) === a.id} onChange={(e) => setAddressId(e.target.value)} style={{ marginTop: 4 }} />
                  <span>
                    <strong>{a.fullName}</strong>{a.defaultAddress ? ' · Default' : ''}<br />
                    <span className="muted small">{a.line1}{a.line2 ? `, ${a.line2}` : ''}, {a.city}, {a.state} {a.postalCode}, {a.country}<br />{a.phone}</span>
                  </span>
                </label>
              ))}
              <Link to="/addresses" className="btn btn-ghost btn-sm">Manage addresses</Link>
            </div>

            <div className="card" style={{ padding: 22, marginBottom: 18 }}>
              <h3>Payment method</h3>
              {PAYMENT_METHODS.map((m) => (
                <label key={m} style={{ display: 'flex', gap: 10, alignItems: 'center', padding: 10, cursor: 'pointer' }}>
                  <input type="radio" name="payment" value={m} checked={paymentMethod === m} onChange={(e) => setPaymentMethod(e.target.value)} />
                  <span>{m === 'CASH_ON_DELIVERY' ? 'Cash on delivery' : 'Simulated card payment'}</span>
                </label>
              ))}
            </div>

            <div className="card" style={{ padding: 22 }}>
              <h3>Order notes (optional)</h3>
              <textarea className="form-control" rows={3} maxLength={300} value={notes} onChange={(e) => setNotes(e.target.value)} placeholder="Delivery instructions, gate code…" />
            </div>
          </div>

          <aside className="summary-card">
            <h3>Your order</h3>
            {items.map((item) => (
              <div key={item.id} className="order-line">
                <span>{item.quantity} × {item.name}</span>
                <span>{formatCurrency(item.lineTotal)}</span>
              </div>
            ))}
            <div className="summary-row" style={{ marginTop: 10 }}><span>Subtotal</span><span>{formatCurrency(cart.subtotal)}</span></div>
            <p className="small muted">Final shipping and tax are computed securely on the server when you place the order.</p>
            {error ? <ErrorMessage error={error} /> : null}
            <button type="submit" className="btn btn-primary btn-block btn-lg" disabled={busy}>
              {busy ? 'Placing order…' : 'Place order'}
            </button>
          </aside>
        </form>
      )}
    </div>
  )
}