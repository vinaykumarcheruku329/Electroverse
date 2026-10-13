import { useCallback, useEffect, useState } from 'react'
import { useSearchParams } from 'react-router-dom'
import { catalogApi, categoryApi } from '../api/endpoints'
import { ProductGrid } from '../components/ProductCard'
import { Loading, ErrorMessage, EmptyState } from '../components/StateViews'
import { Pagination } from '../components/Pagination'

const SORT_OPTIONS = [
  { value: '', label: 'Popularity' },
  { value: 'price_asc', label: 'Price: Low to High' },
  { value: 'price_desc', label: 'Price: High to Low' },
  { value: 'newest', label: 'Newest' },
  { value: 'rating', label: 'Top rated' },
  { value: 'name', label: 'Name (A–Z)' }
]

const SIZE = 12

export default function ProductsPage() {
  const [params, setParams] = useSearchParams()

  const [q, setQ] = useState(params.get('q') || '')
  const [category, setCategory] = useState(params.get('category') || '')
  const [brand, setBrand] = useState(params.get('brand') || '')
  const [minPrice, setMinPrice] = useState(params.get('minPrice') || '')
  const [maxPrice, setMaxPrice] = useState(params.get('maxPrice') || '')
  const [minRating, setMinRating] = useState(params.get('minRating') || '')
  const [inStock, setInStock] = useState(params.get('inStock') === 'true')
  const [featured, setFeatured] = useState(params.get('featured') === 'true')
  const [sort, setSort] = useState(params.get('sort') || '')
  const [page, setPage] = useState(Number(params.get('page')) || 0)

  const [categories, setCategories] = useState([])
  const [brands, setBrands] = useState([])
  const [result, setResult] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [submitted, setSubmitted] = useState(q || brand || minPrice || maxPrice || minRating)

  useEffect(() => {
    categoryApi.list().then(setCategories).catch(() => setCategories([]))
    catalogApi.brands().then(setBrands).catch(() => setBrands([]))
  }, [])

  const syncUrl = useCallback((next) => {
    const copy = {}
    if (next.q) copy.q = next.q
    if (next.category && next.category !== 'all') copy.category = next.category
    if (next.brand) copy.brand = next.brand
    if (next.minPrice) copy.minPrice = next.minPrice
    if (next.maxPrice) copy.maxPrice = next.maxPrice
    if (next.minRating) copy.minRating = next.minRating
    if (next.inStock) copy.inStock = 'true'
    if (next.featured) copy.featured = 'true'
    if (next.sort) copy.sort = next.sort
    if (next.page) copy.page = String(next.page)
    setParams(copy, { replace: true })
  }, [setParams])

  useEffect(() => {
    let active = true
    setLoading(true)
    setError(null)
    const filter = {
      q: q || undefined,
      category: category === 'all' ? undefined : (category || undefined),
      brand: brand || undefined,
      minPrice: minPrice || undefined,
      maxPrice: maxPrice || undefined,
      minRating: minRating || undefined,
      inStock: inStock ? 'true' : undefined,
      featured: featured ? 'true' : undefined,
      sort: sort || undefined,
      page,
      size: SIZE
    }
    catalogApi.products(filter)
      .then((data) => { if (active) setResult(data) })
      .catch((err) => { if (active) setError(err) })
      .finally(() => { if (active) setLoading(false) })
    return () => { active = false }
  }, [q, category, brand, minPrice, maxPrice, minRating, inStock, featured, sort, page])

  function nextParams() {
    return {
      q, category, brand, minPrice, maxPrice, minRating,
      inStock, featured, sort, page
    }
  }

  function apply(partial) {
    const next = { ...nextParams(), ...partial, page: 0 }
    setPage(0)
    if (partial.q !== undefined) setQ(partial.q)
    if (partial.category !== undefined) setCategory(partial.category)
    if (partial.brand !== undefined) setBrand(partial.brand)
    if (partial.minPrice !== undefined) setMinPrice(partial.minPrice)
    if (partial.maxPrice !== undefined) setMaxPrice(partial.maxPrice)
    if (partial.minRating !== undefined) setMinRating(partial.minRating)
    if (partial.inStock !== undefined) setInStock(partial.inStock)
    if (partial.featured !== undefined) setFeatured(partial.featured)
    if (partial.sort !== undefined) setSort(partial.sort)
    setSubmitted(next)
    syncUrl(next)
  }

  function submitSearch(e) {
    e.preventDefault()
    apply({ q, page: 0 })
  }

  function changePage(p) {
    setPage(p)
    syncUrl({ ...nextParams(), page: p })
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  function clearAll() {
    apply({ q: '', category: 'all', brand: '', minPrice: '', maxPrice: '', minRating: '', inStock: false, featured: false, sort: '' })
  }

  const hasActiveFilters = Boolean(submitted.q || (submitted.category && submitted.category !== 'all') || submitted.brand ||
    submitted.minPrice || submitted.maxPrice || submitted.minRating || submitted.inStock || submitted.featured)

  return (
    <>
      <section className="page-hero">
        <div className="container">
          <h1>Shop all products</h1>
          <p>Filter, search and sort the catalog. Every result is served live from the ElectroVerse backend.</p>
        </div>
      </section>

      <div className="container">
        <form className="filters" onSubmit={submitSearch}>
          <div className="filter-group">
            <label>Search</label>
            <input className="form-control" value={q} onChange={(e) => setQ(e.target.value)} placeholder="Name, brand, or keyword…" />
          </div>
          <div className="filter-group">
            <label>Category</label>
            <select className="form-control" value={category || 'all'} onChange={(e) => apply({ category: e.target.value })}>
              <option value="all">All categories</option>
              {categories.map((c) => <option key={c.id} value={c.slug}>{c.name}</option>)}
            </select>
          </div>
          <div className="filter-group">
            <label>Brand</label>
            <select className="form-control" value={brand} onChange={(e) => apply({ brand: e.target.value })}>
              <option value="">All brands</option>
              {brands.map((b) => <option key={b} value={b}>{b}</option>)}
            </select>
          </div>
          <div className="filter-group">
            <label>Min price</label>
            <input className="form-control" type="number" min="0" value={minPrice} onChange={(e) => setMinPrice(e.target.value)} onBlur={() => apply({ minPrice })} placeholder="0" />
          </div>
          <div className="filter-group">
            <label>Max price</label>
            <input className="form-control" type="number" min="0" value={maxPrice} onChange={(e) => setMaxPrice(e.target.value)} onBlur={() => apply({ maxPrice })} placeholder="5000" />
          </div>
          <div className="filter-group">
            <label>Min rating</label>
            <select className="form-control" value={minRating} onChange={(e) => apply({ minRating: e.target.value })}>
              <option value="">Any rating</option>
              <option value="4">4★ & up</option>
              <option value="3">3★ & up</option>
            </select>
          </div>
          <div className="filter-group">
            <label>Availability</label>
            <select className="form-control" value={inStock ? 'true' : ''} onChange={(e) => apply({ inStock: e.target.value === 'true' })}>
              <option value="">Any stock</option>
              <option value="true">In stock only</option>
            </select>
          </div>
          <div className="filter-group">
            <label>Featured</label>
            <select className="form-control" value={featured ? 'true' : ''} onChange={(e) => apply({ featured: e.target.value === 'true' })}>
              <option value="">Any</option>
              <option value="true">Featured only</option>
            </select>
          </div>
          <div className="filter-group">
            <label>Sort</label>
            <select className="form-control" value={sort} onChange={(e) => apply({ sort: e.target.value })}>
              {SORT_OPTIONS.map((o) => <option key={o.value} value={o.value}>{o.label}</option>)}
            </select>
          </div>
          <div className="filter-group">
            <button type="submit" className="btn btn-primary">Apply</button>
          </div>
          {hasActiveFilters ? (
            <div className="filter-group">
              <button type="button" className="btn btn-ghost" onClick={clearAll}>Clear filters</button>
            </div>
          ) : null}
        </form>

        {loading ? <Loading label="Loading products…" /> : null}
        {!loading && error ? <ErrorMessage error={error} onRetry={() => setPage((p) => p)} /> : null}
        {!loading && !error && result && result.totalElements === 0 ? (
          <EmptyState title="No products match" message="Try adjusting your search or filters." />
        ) : null}

        {!loading && !error && result && result.content.length ? (
          <>
            <p className="muted small" style={{ marginBottom: 16 }}>
              {result.totalElements} product{result.totalElements === 1 ? '' : 's'} found
            </p>
            <ProductGrid products={result.content} />
            <Pagination page={result.page} totalPages={result.totalPages} onChange={changePage} />
          </>
        ) : null}
      </div>
    </>
  )
}