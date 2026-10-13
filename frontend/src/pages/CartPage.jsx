import { Link, useNavigate } from 'react-router-dom'
import { useCart } from '../context/CartContext'
import { useUi } from '../context/UiContext'
import { ProductImage } from '../components/ProductImage'
import { Loading, EmptyState } from '../components/StateViews'
import { formatCurrency, formatINR, FREE_SHIPPING_THRESHOLD } from '../utils/format'

export default function CartPage() {
  const { cart, loading, updateItem, removeItem } = useCart()
  const { toast } = useUi()
  const navigate = useNavigate()

  const items = cart?.items || []

  async function changeQty(item, qty) {
    if (qty < 1 || qty > 99) return
    try { await updateItem(item.id, qty) } catch (err) { toast.error(err.message) }
  }

  async function remove(item) {
    try { await removeItem(item.id); toast.success('Removed from cart.') } catch (err) { toast.error(err.message) }
  }

  if (loading && !items.length) return <div className="container"><Loading label="Loading cart…" /></div>

  return (
    <div className="container" style={{ paddingTop: 24, paddingBottom: 24 }}>
      <div className="page-hero" style={{ marginBottom: 24 }}>
        <h1>Your cart</h1>
        <p>{cart?.itemCount || 0} item{cart?.itemCount === 1 ? '' : 's'} ready to go.</p>
      </div>

      {items.length === 0 ? (
        <EmptyState
          title="Your cart is empty"
          message="Browse the catalog and add something you love."
          action={<Link to="/products" className="btn btn-primary">Shop products</Link>}
        />
      ) : (
        <div className="cart-layout">
          <div className="card">
            {items.map((item) => (
              <div key={item.id} className="cart-item" style={{ borderBottom: '1px solid var(--border)' }}>
                <Link to={`/products/${item.productId}`}>
                  <ProductImage product={{ name: item.name }} url={item.imageUrl} alt={item.name} />
                </Link>
                <div className="cart-item-info">
                  <Link to={`/products/${item.productId}`} className="name">{item.name}</Link>
                  <div className="unit">{formatCurrency(item.unitPrice)} each</div>
                  {!item.available ? <span className="pill pill-danger small">Unavailable</span> : null}
                </div>
                <div className="quantity">
                  <button type="button" onClick={() => changeQty(item, item.quantity - 1)} aria-label="Decrease">−</button>
                  <input type="number" value={item.quantity} onChange={(e) => changeQty(item, Number(e.target.value))} aria-label="Quantity" />
                  <button type="button" onClick={() => changeQty(item, item.quantity + 1)} aria-label="Increase">+</button>
                </div>
                <div style={{ minWidth: 90, textAlign: 'right', fontWeight: 700 }}>{formatCurrency(item.lineTotal)}</div>
                <button type="button" className="btn btn-ghost btn-sm" onClick={() => remove(item)} aria-label={`Remove ${item.name}`}>✕</button>
              </div>
            ))}
          </div>

          <aside className="summary-card">
            <h3>Order summary</h3>
            <div className="summary-row"><span>Items</span><span>{cart.itemCount}</span></div>
            <div className="summary-row"><span>Subtotal</span><span>{formatCurrency(cart.subtotal)}</span></div>
            <p className="small muted">Shipping and tax are calculated on the server at checkout. Free shipping on orders over {formatINR(FREE_SHIPPING_THRESHOLD)}.</p>
            <button className="btn btn-primary btn-block btn-lg" onClick={() => navigate('/checkout')}>
              Proceed to checkout
            </button>
            <Link to="/products" className="btn btn-ghost btn-block" style={{ marginTop: 8 }}>Continue shopping</Link>
          </aside>
        </div>
      )}
    </div>
  )
}