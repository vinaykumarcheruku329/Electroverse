import { useEffect, useMemo, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { catalogApi, priceApi, lifecycleApi } from '../api/endpoints'
import { Price } from '../components/Price'
import { ProductImage } from '../components/ProductImage'
import { Rating } from '../components/Rating'
import { Loading, ErrorMessage, EmptyState } from '../components/StateViews'
import { Pagination } from '../components/Pagination'
import { ProductGrid } from '../components/ProductCard'
import { useAuth } from '../context/AuthContext'
import { useCart } from '../context/CartContext'
import { useWishlist } from '../context/WishlistContext'
import { useUi } from '../context/UiContext'
import { formatCurrency, formatDate, titleCase } from '../utils/format'

export default function ProductDetailPage() {
  const { id } = useParams()
  const { isAuthenticated } = useAuth()
  const { addItem } = useCart()
  const { has, toggle } = useWishlist()
  const { toast } = useUi()

  const [product, setProduct] = useState(null)
  const [related, setRelated] = useState([])
  const [reviews, setReviews] = useState(null)
  const [priceHistory, setPriceHistory] = useState(null)
  const [lifecycle, setLifecycle] = useState(null)
  const [error, setError] = useState(null)
  const [loading, setLoading] = useState(true)

  const [quantity, setQuantity] = useState(1)
  const [tab, setTab] = useState('specs')
  const [activeImage, setActiveImage] = useState(0)
  const [reviewPage, setReviewPage] = useState(0)

  const [alertPrice, setAlertPrice] = useState('')
  const [reviewForm, setReviewForm] = useState({ rating: 5, title: '', comment: '' })
  const [busy, setBusy] = useState(false)

  const images = useMemo(() => {
    if (!product) return []
    const list = []
    if (product.primaryImageUrl) list.push({ url: product.primaryImageUrl, alt: product.name })
    ;(product.images || []).forEach((img) => {
      if (img.url !== product.primaryImageUrl) list.push({ url: img.url, alt: img.altText || product.name })
    })
    return list
  }, [product])

  const wished = product ? has(product.id) : false

  useEffect(() => {
    let active = true
    setLoading(true)
    setError(null)
    setTab('specs')
    setActiveImage(0)
    setReviewPage(0)
    Promise.all([
      catalogApi.product(id),
      catalogApi.related(id),
      catalogApi.reviews(id, { page: 0, size: 5 }),
      priceApi.history(id).catch(() => null)
    ]).then(([prod, rel, rev, ph]) => {
      if (!active) return
      setProduct(prod)
      setRelated(rel || [])
      setReviews(rev)
      setPriceHistory(ph)
    }).catch((err) => {
      if (active) setError(err)
    }).finally(() => {
      if (active) setLoading(false)
    })

    lifecycleApi.get(id)
      .then((l) => { if (active && l) setLifecycle(l) })
      .catch(() => {})
    return () => { active = false }
  }, [id])

  async function loadReviews(page) {
    const data = await catalogApi.reviews(id, { page, size: 5 })
    setReviews(data)
    setReviewPage(page)
  }

  async function handleAddToCart() {
    if (!isAuthenticated) { toast.info('Please sign in to add items to your cart.'); return }
    setBusy(true)
    try {
      await addItem(product.id, quantity)
      toast.success(`${product.name} added to cart`)
    } catch (err) { toast.error(err.message) } finally { setBusy(false) }
  }

  async function handleWish() {
    if (!isAuthenticated) { toast.info('Please sign in to save items to your wishlist.'); return }
    try {
      const added = await toggle(product.id)
      toast.success(added ? 'Saved to wishlist' : 'Removed from wishlist')
    } catch (err) { toast.error(err.message) }
  }

  async function handleAlert(e) {
    e.preventDefault()
    if (!isAuthenticated) { toast.info('Sign in to create a price alert.'); return }
    setBusy(true)
    try {
      await priceApi.createAlert({ productId: product.id, targetPrice: Number(alertPrice) })
      toast.success('Price alert created. We will notify you when the price drops.')
      setAlertPrice('')
    } catch (err) { toast.error(err.message) } finally { setBusy(false) }
  }

  async function handleReview(e) {
    e.preventDefault()
    setBusy(true)
    try {
      await catalogApi.addReview(product.id, { ...reviewForm, rating: Number(reviewForm.rating) })
      toast.success('Thank you for your review!')
      setReviewForm({ rating: 5, title: '', comment: '' })
      loadReviews(reviewPage)
    } catch (err) { toast.error(err.message) } finally { setBusy(false) }
  }

  if (loading) return <div className="container"><Loading label="Loading product…" /></div>
  if (error) return <div className="container"><ErrorMessage error={error} onRetry={() => window.location.reload()} /></div>
  if (!product) return <div className="container"><EmptyState title="Product not found" /></div>

  const outOfStock = Number(product.stock) <= 0
  const specsByGroup = (product.specs || []).reduce((acc, s) => {
    const g = s.group || 'General'
    if (!acc[g]) acc[g] = []
    acc[g].push(s)
    return acc
  }, {})

  return (
    <>
      <section className="page-hero">
        <div className="container">
          <nav className="small muted" style={{ marginBottom: 8 }}>
            <Link to="/products">Shop</Link> / <Link to={`/products?category=${product.categorySlug}`}>{product.categoryName}</Link> / {product.brand}
          </nav>
          <h1>{product.name}</h1>
          <Rating value={product.ratingAverage} count={product.ratingCount} />
        </div>
      </section>

      <div className="container">
        <div className="detail-grid">
          <div>
            <div className="detail-gallery">
              <ProductImage
                product={product}
                url={images[activeImage]?.url}
                alt={images[activeImage]?.alt || product.name}
                loading="eager"
              />
            </div>
            {images.length > 1 ? (
              <div className="detail-thumbs">
                {images.map((img, i) => (
                  <button key={i} className={i === activeImage ? 'active' : ''} onClick={() => setActiveImage(i)}>
                    <ProductImage product={product} url={img.url} alt={img.alt || product.name} />
                  </button>
                ))}
              </div>
            ) : null}
          </div>

          <div>
            <div className="detail-price-row">
              <Price value={product.price} compareAt={product.compareAtPrice} currency={product.currency} large />
            </div>
            <div className="detail-meta">
              <span>● {outOfStock ? 'Out of stock' : `${product.stock} in stock`}</span>
              {product.warrantyMonths ? <span>🛡 {product.warrantyMonths} month warranty</span> : null}
              {product.ratingCount > 0 ? <span>★ {Number(product.ratingAverage).toFixed(1)} from {product.ratingCount} reviews</span> : <span>No reviews yet</span>}
            </div>
            <p className="detail-desc">{product.description || 'No description available yet.'}</p>

            <div className="detail-actions">
              <div className="quantity">
                <button type="button" onClick={() => setQuantity((v) => Math.max(1, v - 1))} aria-label="Decrease quantity">−</button>
                <input type="number" min="1" max={Math.max(1, product.stock || 1)} value={quantity}
                  onChange={(e) => setQuantity(Math.max(1, Math.min(Number(e.target.value) || 1, product.stock || 99)))} aria-label="Quantity" />
                <button type="button" onClick={() => setQuantity((v) => Math.min(product.stock || 99, v + 1))} aria-label="Increase quantity">+</button>
              </div>
              <button type="button" className="btn btn-primary btn-lg" onClick={handleAddToCart} disabled={busy || outOfStock}>
                {busy ? 'Adding…' : outOfStock ? 'Sold out' : 'Add to cart'}
              </button>
              <button type="button" className={`btn btn-outline btn-lg ${wished ? 'active' : ''}`} onClick={handleWish}>
                {wished ? '♥ In wishlist' : '♡ Save'}
              </button>
            </div>

            <form onSubmit={handleAlert} className="tool-card" style={{ display: 'flex', gap: 10, alignItems: 'center', flexWrap: 'wrap' }}>
              <strong>Price alert</strong>
              <input className="form-control" type="number" step="0.01" min="0" style={{ maxWidth: 150 }}
                placeholder="Target price" value={alertPrice} onChange={(e) => setAlertPrice(e.target.value)} required />
              <button type="submit" className="btn btn-outline" disabled={busy}>Notify me</button>
            </form>

            {product.specs?.length ? (
              <div className="tool-card">
                <h3>Quick specs</h3>
                <table className="spec-table">
                  <tbody>
                    {product.specs.slice(0, 8).map((s, i) => (
                      <tr key={i}><td>{titleCase(s.key)}</td><td>{s.value}</td></tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ) : null}
          </div>
        </div>

        <div className="tabs">
          {['specs', 'reviews', 'price', 'lifecycle'].map((t) => (
            <button key={t} className={tab === t ? 'tab active' : 'tab'} onClick={() => setTab(t)}>
              {t === 'specs' ? 'Specifications' : t === 'reviews' ? `Reviews (${reviews?.totalElements || 0})` : t === 'price' ? 'Price history' : 'Lifecycle score'}
            </button>
          ))}
        </div>

        {tab === 'specs' ? (
          <div className="tool-card">
            {Object.entries(specsByGroup).map(([group, list]) => (
              <div key={group} style={{ marginBottom: 16 }}>
                <h4 style={{ color: 'var(--bronze-dark)', textTransform: 'uppercase', fontSize: '0.85rem', letterSpacing: 1 }}>{group}</h4>
                <table className="spec-table">
                  <tbody>
                    {list.map((s, i) => (
                      <tr key={i}><td>{titleCase(s.key)}</td><td>{s.value}</td></tr>
                    ))}
                  </tbody>
                </table>
              </div>
            ))}
          </div>
        ) : null}

        {tab === 'reviews' ? (
          <div className="tool-card">
            {isAuthenticated ? (
              <form className="tool-card" style={{ background: 'var(--ivory)' }} onSubmit={handleReview}>
                <h3>Write a review</h3>
                <div className="form-group">
                  <label>Rating</label>
                  <select className="form-control" value={reviewForm.rating} onChange={(e) => setReviewForm({ ...reviewForm, rating: e.target.value })}>
                    {[5, 4, 3, 2, 1].map((r) => <option key={r} value={r}>{'★'.repeat(r)}{'☆'.repeat(5 - r)}</option>)}
                  </select>
                </div>
                <div className="form-group">
                  <label>Title</label>
                  <input className="form-control" value={reviewForm.title} onChange={(e) => setReviewForm({ ...reviewForm, title: e.target.value })} placeholder="Short summary" />
                </div>
                <div className="form-group">
                  <label>Comment</label>
                  <textarea className="form-control" rows={4} value={reviewForm.comment} onChange={(e) => setReviewForm({ ...reviewForm, comment: e.target.value })} placeholder="What do you think?" />
                </div>
                <button type="submit" className="btn btn-primary" disabled={busy}>Submit review</button>
              </form>
            ) : (
              <p className="muted">Please <Link to="/login">sign in</Link> to write a review.</p>
            )}

            <div style={{ marginTop: 20 }}>
              {reviews && reviews.totalElements === 0 ? <EmptyState title="No reviews yet" message="Be the first to review this product." /> : null}
              {reviews?.content.map((r) => (
                <div key={r.id} className="review-card">
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                    <strong>{r.userName}</strong>
                    <span className="muted small">{formatDate(r.createdAt)}</span>
                  </div>
                  <Rating value={r.rating} showCount={false} />
                  {r.title ? <h4 style={{ marginTop: 6 }}>{r.title}</h4> : null}
                  {r.comment ? <p className="muted">{r.comment}</p> : null}
                </div>
              ))}
              {reviews && reviews.totalPages > 1 ? (
                <Pagination page={reviewPage} totalPages={reviews.totalPages} onChange={loadReviews} />
              ) : null}
            </div>
          </div>
        ) : null}

        {tab === 'price' ? (
          <PriceTab priceHistory={priceHistory} />
        ) : null}

        {tab === 'lifecycle' ? (
          <LifecycleTab lifecycle={lifecycle} productId={product.id} productName={product.name} />
        ) : null}

        {related.length ? (
          <section className="section">
            <div className="section-head"><h2>You may also like</h2></div>
            <ProductGrid products={related} />
          </section>
        ) : null}
      </div>
    </>
  )
}

function PriceTab({ priceHistory }) {
  if (!priceHistory) return <EmptyState title="No price history yet" message="Price tracking data will appear here as collection continues." />
  const points = priceHistory.points || []
  const min = Math.min(...points.map((p) => Number(p.price)))
  const max = Math.max(...points.map((p) => Number(p.price)))
  return (
    <div className="tool-card">
      <h3>Price history</h3>
      {points.length > 1 ? (
        <div className="chart-bars">
          {points.map((p, i) => {
            const height = max === min ? 50 : ((Number(p.price) - min) / (max - min)) * 100
            return (
              <div key={i} className="chart-bar" title={formatCurrency(p.price)}>
                <div className="bar" style={{ height: `${Math.max(6, height)}%` }} />
                <div className="bar-label">{new Date(p.date).toLocaleDateString('en-US', { month: 'short' })}</div>
              </div>
            )
          })}
        </div>
      ) : null}
      <div className="kv">
        <div className="kv-item"><span>Lowest price</span><strong>{formatCurrency(priceHistory.lowestPrice)}</strong></div>
        <div className="kv-item"><span>Latest price</span><strong>{formatCurrency(priceHistory.latestPrice)}</strong></div>
        <div className="kv-item"><span>Updated</span><strong>{formatDate(priceHistory.latestDate)}</strong></div>
        <div className="kv-item"><span>Source</span><strong>{priceHistory.source || 'Market'}</strong></div>
      </div>
      {points.length ? (
        <table className="table">
          <thead><tr><th>Date</th><th>Price</th><th>Source</th></tr></thead>
          <tbody>
            {points.slice().reverse().map((p, i) => (
              <tr key={i}><td>{formatDate(p.date, true)}</td><td>{formatCurrency(p.price)}</td><td>{p.source || '—'}</td></tr>
            ))}
          </tbody>
        </table>
      ) : null}
    </div>
  )
}

function LifecycleTab({ lifecycle, productId, productName }) {
  if (!lifecycle) return <EmptyState title="No lifecycle assessment yet" message="This product has not been scored yet." />
  const labels = [
    ['Repairability', lifecycle.repairability],
    ['Upgradeability', lifecycle.upgradeability],
    ['Support', lifecycle.manufacturerSupport],
    ['Warranty', lifecycle.warranty],
    ['Energy', lifecycle.energyEfficiency]
  ]
  return (
    <div className="tool-card">
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h3>Tech Lifecycle Score</h3>
        <Link to={`/lifecycle?product=${productId}`} className="btn btn-outline btn-sm">Full page</Link>
      </div>
      <div className="lifecycle-score">
        {labels.map(([label, val]) => (
          <div key={label} className="score-card"><strong>{val}</strong><span>{label}</span></div>
        ))}
        <div className="score-card" style={{ background: 'var(--bronze-soft)' }}><strong style={{ color: 'var(--bronze-dark)' }}>{lifecycle.overall}</strong><span>Overall</span></div>
      </div>
      {lifecycle.explanation ? <p className="muted">{lifecycle.explanation}</p> : null}
      <span className="small muted">Assessed {formatDate(lifecycle.assessmentDate)} · Methodology {lifecycle.methodologyVersion}</span>
    </div>
  )
}