import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { categoryApi, finderApi } from '../api/endpoints'
import { ProductCard } from '../components/ProductCard'
import { Loading, ErrorMessage, EmptyState } from '../components/StateViews'
import { useAuth } from '../context/AuthContext'
import { formatCurrency } from '../utils/format'

const DEFAULT_CRITERIA = ['performance', 'battery', 'portability', 'display', 'storage', 'energyEfficiency', 'repairability', 'value']
const PURPOSES = ['', 'gaming', 'programming', 'study', 'office', 'photography', 'everyday']

export default function FinderPage() {
  const { isAuthenticated } = useAuth()
  const [categories, setCategories] = useState([])
  const [criteria, setCriteria] = useState(DEFAULT_CRITERIA)
  const [form, setForm] = useState({
    categorySlug: '',
    budget: 100000,
    purpose: '',
    maxResults: 6,
    priorities: {}
  })
  const [result, setResult] = useState(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)

  useEffect(() => {
    categoryApi.list().then((cats) => {
      setCategories(cats)
      if (cats.length) setForm((f) => ({ ...f, categorySlug: f.categorySlug || cats[0].slug }))
    }).catch(() => {})
    if (isAuthenticated) {
      finderApi.criteria().then((c) => setCriteria(c || DEFAULT_CRITERIA)).catch(() => {})
    }
  }, [isAuthenticated])

  function setPriority(key, value) {
    setForm((f) => ({ ...f, priorities: { ...f.priorities, [key]: Number(value) } }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setLoading(true)
    setError(null)
    try {
      const payload = {
        categorySlug: form.categorySlug,
        budget: Number(form.budget),
        purpose: form.purpose || undefined,
        maxResults: Number(form.maxResults),
        priorities: Object.keys(form.priorities).length ? form.priorities : undefined
      }
      setResult(await finderApi.recommend(payload))
    } catch (err) { setError(err) } finally { setLoading(false) }
  }

  return (
    <div className="container" style={{ paddingTop: 24, paddingBottom: 24 }}>
      <div className="page-hero" style={{ marginBottom: 24 }}>
        <h1>Smart Device Finder</h1>
        <p>Tell us what matters and we will score the catalog to find your best match.</p>
      </div>

      <form className="tool-card" onSubmit={handleSubmit}>
        <div className="form-row">
          <div className="form-group">
            <label>Category</label>
            <select className="form-control" required value={form.categorySlug} onChange={(e) => setForm({ ...form, categorySlug: e.target.value })}>
              <option value="">Select a category…</option>
              {categories.map((c) => <option key={c.id} value={c.slug}>{c.name}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label>Budget (max price, ₹)</label>
            <input className="form-control" type="number" min="1" required value={form.budget} onChange={(e) => setForm({ ...form, budget: e.target.value })} />
          </div>
        </div>
        <div className="form-row">
          <div className="form-group">
            <label>Primary purpose</label>
            <select className="form-control" value={form.purpose} onChange={(e) => setForm({ ...form, purpose: e.target.value })}>
              {PURPOSES.map((p) => <option key={p} value={p}>{p ? p[0].toUpperCase() + p.slice(1) : 'Any / general'}</option>)}
            </select>
          </div>
          <div className="form-group">
            <label>Max results ({form.maxResults})</label>
            <input className="form-control" type="range" min="1" max="12" value={form.maxResults} onChange={(e) => setForm({ ...form, maxResults: e.target.value })} />
          </div>
        </div>

        <fieldset style={{ border: '1px solid var(--border)', borderRadius: 12, padding: 16 }}>
          <legend className="small muted" style={{ padding: '0 6px' }}>Priorities (1 = low, 5 = essential)</legend>
          <div className="tool-grid">
            {criteria.map((c) => (
              <div key={c} className="form-group" style={{ marginBottom: 0 }}>
                <label style={{ display: 'flex', justifyContent: 'space-between' }}>
                  <span>{c.replace(/([A-Z])/g, ' $1')}</span>
                  <span className="muted">{form.priorities[c] || 0}</span>
                </label>
                <input type="range" min="0" max="5" value={form.priorities[c] || 0} onChange={(e) => setPriority(c, e.target.value)} style={{ width: '100%', accentColor: 'var(--bronze)' }} />
              </div>
            ))}
          </div>
        </fieldset>

        <button type="submit" className="btn btn-primary btn-lg" style={{ marginTop: 16 }} disabled={loading}>
          {loading ? 'Scoring products…' : 'Find my device'}
        </button>
      </form>

      {loading ? <Loading label="Analysing products…" /> : null}
      {error ? <ErrorMessage error={error} /> : null}

      {result ? (
        <section className="section" style={{ paddingBottom: 0 }}>
          <div className="section-head">
            <h2>Top recommendations</h2>
            <span className="muted small">Session #{result.sessionId} · budget {formatCurrency(result.budget)}</span>
          </div>
          {result.results?.length ? (
            <div className="tool-grid">
              {result.results.map((r) => (
                <div key={r.product.id} className="card tool-card">
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 8 }}>
                    <span className="pill pill-bronze">#{r.rank} · score {r.score}</span>
                    {r.withinBudget ? <span className="pill pill-success">Within budget</span> : <span className="pill pill-muted">Over budget</span>}
                  </div>
                  <ProductCard product={r.product} />
                  {r.explanation ? <p className="muted small" style={{ marginTop: 10 }}>{r.explanation}</p> : null}
                  {r.specMatches ? <p className="small"><strong>Matches:</strong> {r.specMatches}</p> : null}
                </div>
              ))}
            </div>
          ) : <EmptyState title="No matches found" message="Try increasing your budget or changing category." />}
        </section>
      ) : null}
    </div>
  )
}