import { useState } from 'react'
import { Link } from 'react-router-dom'
import { Price } from './Price'
import { ProductImage } from './ProductImage'
import { Rating } from './Rating'
import { useAuth } from '../context/AuthContext'
import { useCart } from '../context/CartContext'
import { useWishlist } from '../context/WishlistContext'
import { useUi } from '../context/UiContext'

export function ProductCard({ product }) {
  const { isAuthenticated } = useAuth()
  const { addItem } = useCart()
  const { has, toggle } = useWishlist()
  const { toast } = useUi()
  const [busy, setBusy] = useState(false)

  const outOfStock = Number(product.stock) <= 0
  const wished = has(product.id)

  async function handleAdd(e) {
    e.preventDefault()
    if (!isAuthenticated) {
      toast.info('Please sign in to add items to your cart.')
      return
    }
    setBusy(true)
    try {
      await addItem(product.id, 1)
      toast.success(`${product.name} added to cart`)
    } catch (err) {
      toast.error(err.message)
    } finally {
      setBusy(false)
    }
  }

  async function handleWish(e) {
    e.preventDefault()
    if (!isAuthenticated) {
      toast.info('Please sign in to save items to your wishlist.')
      return
    }
    try {
      const added = await toggle(product.id)
      toast.success(added ? 'Saved to wishlist' : 'Removed from wishlist')
    } catch (err) {
      toast.error(err.message)
    }
  }

  return (
    <article className="card product-card">
      <Link to={`/products/${product.id}`} className="product-media">
        <ProductImage product={product} />
        <div className="product-badges">
          {product.featured ? <span className="badge badge-bronze">Featured</span> : null}
          {product.compareAtPrice != null && Number(product.compareAtPrice) > Number(product.price)
            ? <span className="badge badge-sale">Deal</span> : null}
          {outOfStock ? <span className="badge badge-muted">Sold out</span> : null}
        </div>
        <button
          type="button"
          className={wished ? 'wish-btn active' : 'wish-btn'}
          onClick={handleWish}
          aria-label={wished ? 'Remove from wishlist' : 'Add to wishlist'}
          title={wished ? 'Remove from wishlist' : 'Add to wishlist'}
        >
          {wished ? '♥' : '♡'}
        </button>
      </Link>

      <div className="product-body">
        <span className="product-brand">{product.brand || 'ElectroVerse'}</span>
        <Link to={`/products/${product.id}`} className="product-name">{product.name}</Link>
        <div className="product-meta">
          <Rating value={product.ratingAverage} count={product.ratingCount} size="sm" />
          <span className="product-category">{product.categoryName}</span>
        </div>
        <div className="product-footer">
          <Price value={product.price} compareAt={product.compareAtPrice} currency={product.currency || 'INR'} large />
          <button
            type="button"
            className="btn btn-primary btn-sm"
            onClick={handleAdd}
            disabled={busy || outOfStock}
          >
            {busy ? 'Adding…' : outOfStock ? 'Sold out' : 'Add to cart'}
          </button>
        </div>
      </div>
    </article>
  )
}

export function ProductGrid({ products }) {
  return (
    <div className="product-grid">
      {products.map((p) => <ProductCard key={p.id} product={p} />)}
    </div>
  )
}
