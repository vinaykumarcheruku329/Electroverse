import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { wishlistApi } from '../api/endpoints'
import { ProductGrid } from '../components/ProductCard'
import { Loading, ErrorMessage, EmptyState } from '../components/StateViews'
import { useWishlist } from '../context/WishlistContext'

export default function WishlistPage() {
  const { ids } = useWishlist()
  const [items, setItems] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  async function load() {
    setError(null)
    try {
      setItems(await wishlistApi.list())
    } catch (err) {
      setError(err)
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => { load() }, [])

  useEffect(() => {
    load()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [ids])

  return (
    <div className="container" style={{ paddingTop: 24, paddingBottom: 24 }}>
      <div className="page-hero" style={{ marginBottom: 24 }}>
        <h1>Wishlist</h1>
        <p>{items.length} saved product{items.length === 1 ? '' : 's'}.</p>
      </div>

      {loading ? <Loading label="Loading wishlist…" /> : null}
      {!loading && error ? <ErrorMessage error={error} onRetry={load} /> : null}
      {!loading && !error && items.length === 0 ? (
        <EmptyState
          title="Your wishlist is empty"
          message="Tap the heart on any product to save it for later."
          action={<Link to="/products" className="btn btn-primary">Browse products</Link>}
        />
      ) : null}
      {!loading && !error && items.length > 0 ? <ProductGrid products={items} /> : null}
    </div>
  )
}
