import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { catalogApi, categoryApi } from '../api/endpoints'
import { ProductGrid } from '../components/ProductCard'
import { Loading, ErrorMessage, EmptyState, SkeletonGrid } from '../components/StateViews'

export default function HomePage() {
  const [categories, setCategories] = useState([])
  const [featured, setFeatured] = useState([])
  const [deals, setDeals] = useState([])
  const [newArrivals, setNewArrivals] = useState([])
  const [error, setError] = useState(null)

  useEffect(() => {
    let active = true
    Promise.all([
      categoryApi.list(),
      catalogApi.featured(),
      catalogApi.deals(),
      catalogApi.newArrivals()
    ]).then(([cats, feat, deal, news]) => {
      if (!active) return
      setCategories(cats)
      setFeatured(feat)
      setDeals(deal)
      setNewArrivals(news)
    }).catch((err) => {
      if (active) setError(err)
    })
    return () => { active = false }
  }, [])

  if (error) return <div className="container"><ErrorMessage error={error} /></div>

  return (
    <>
      <section className="hero">
        <div className="container">
          <div>
            <div className="hero-eyebrow">Premium electronics</div>
            <h1>Devices worth holding onto,<br />prices worth celebrating.</h1>
            <p className="hero-lead">
              ElectroVerse curates laptops, audio, cameras and smart tech with transparent price
              history, honest compatibility checks, and a real longevity score.
            </p>
            <div className="hero-cta">
              <Link to="/products" className="btn btn-primary btn-lg">Shop the catalog</Link>
              <Link to="/finder" className="btn btn-outline btn-lg">Find your device</Link>
            </div>
          </div>
          <div className="hero-card">
            <h3>Your smarter way to buy tech</h3>
            <div className="hero-stats">
              <div className="hero-stat"><strong>9</strong><span>curated categories</span></div>
              <div className="hero-stat"><strong>21</strong><span>hand-picked products</span></div>
              <div className="hero-stat"><strong>3 of 5</strong><span>smart tools built in</span></div>
              <div className="hero-stat"><strong>100%</strong><span>real backend data</span></div>
            </div>
            <Link to="/compare" className="btn btn-ghost btn-block">Compare side by side →</Link>
          </div>
        </div>
      </section>

      <section className="section">
        <div className="container">
          <div className="section-head">
            <h2>Browse by category</h2>
            <Link to="/products" className="muted">View all</Link>
          </div>
          <div className="tool-grid">
            {categories.map((c) => (
              <Link key={c.id} to={`/products?category=${encodeURIComponent(c.slug)}`} className="card tool-card category-chip">
                <div className="category-chip-head">
                  <span className="category-icon">{c.icon || '▸'}</span>
                  <span className="count-pill">{c.productCount}</span>
                </div>
                <strong>{c.name}</strong>
                <span className="muted small">{c.description || 'Browse the collection'}</span>
              </Link>
            ))}
          </div>
        </div>
      </section>

      <section className="section">
        <div className="container">
          <div className="section-head">
            <h2>Featured products</h2>
            <Link to="/products?featured=true" className="muted">More featured</Link>
          </div>
          {featured.length ? <ProductGrid products={featured} /> : <SkeletonGrid />}
        </div>
      </section>

      <section className="section">
        <div className="container">
          <div className="section-head">
            <h2>Deals of the moment</h2>
            <Link to="/products" className="muted">See all deals</Link>
          </div>
          {deals.length ? <ProductGrid products={deals} /> : <EmptyState title="No deals right now" />}
        </div>
      </section>

      <section className="section">
        <div className="container">
          <div className="section-head">
            <h2>New arrivals</h2>
            <Link to="/products?sort=newest" className="muted">Newest first</Link>
          </div>
          {newArrivals.length ? <ProductGrid products={newArrivals} /> : <SkeletonGrid />}
        </div>
      </section>

      <section className="section">
        <div className="container">
          <div className="tool-grid">
            <Link to="/finder" className="card tool-card"><h3>Smart Device Finder</h3><p className="muted">Answer a few questions and get a scored recommendation that fits your budget.</p></Link>
            <Link to="/compare" className="card tool-card"><h3>Product Comparison</h3><p className="muted">Put 2–4 products side-by-side and see which one wins on what matters.</p></Link>
            <Link to="/compatibility" className="card tool-card"><h3>Compatibility Lab</h3><p className="muted">Check whether components will actually work together before you buy.</p></Link>
            <Link to="/price-intelligence" className="card tool-card"><h3>Price Intelligence</h3><p className="muted">Track price history, spot trends, and set alerts so you never overpay.</p></Link>
            <Link to="/lifecycle" className="card tool-card"><h3>Tech Lifecycle Score</h3><p className="muted">See repairability, upgradeability and support ratings for a device.</p></Link>
          </div>
        </div>
      </section>
    </>
  )
}