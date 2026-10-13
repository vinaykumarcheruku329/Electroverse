import { useEffect, useState } from 'react'
import { Link, NavLink, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useCart } from '../context/CartContext'
import { useWishlist } from '../context/WishlistContext'
import { categoryApi } from '../api/endpoints'

export function Navbar() {
  const { isAuthenticated, isAdmin, user, logout } = useAuth()
  const { itemCount } = useCart()
  const { count: wishCount } = useWishlist()
  const navigate = useNavigate()
  const [categories, setCategories] = useState([])
  const [search, setSearch] = useState('')
  const [open, setOpen] = useState(false)
  const [catOpen, setCatOpen] = useState(false)
  const [acctOpen, setAcctOpen] = useState(false)

  useEffect(() => {
    categoryApi.list().then(setCategories).catch(() => setCategories([]))
  }, [])

  function submitSearch(e) {
    e.preventDefault()
    const q = search.trim()
    navigate(q ? `/products?q=${encodeURIComponent(q)}` : '/products')
    setSearch('')
    setOpen(false)
  }

  function handleLogout() {
    logout()
    setOpen(false)
    setAcctOpen(false)
    navigate('/')
  }

  return (
    <header className="header">
      <div className="container header-inner">
        <Link to="/" className="brand" aria-label="ElectroVerse home">
          <span className="brand-mark">EV</span>
          <span className="brand-name">Electro<span>Verse</span></span>
        </Link>

        <nav className={`nav ${open ? 'nav-open' : ''}`} aria-label="Primary">
          <NavLink to="/products" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')} onClick={() => setOpen(false)}>Shop</NavLink>
          <NavLink to="/finder" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')} onClick={() => setOpen(false)}>Device Finder</NavLink>
          <NavLink to="/compare" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')} onClick={() => setOpen(false)}>Compare</NavLink>
          <NavLink to="/compatibility" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')} onClick={() => setOpen(false)}>Compatibility Lab</NavLink>
          <NavLink to="/price-intelligence" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')} onClick={() => setOpen(false)}>Price Intelligence</NavLink>
          <NavLink to="/lifecycle" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')} onClick={() => setOpen(false)}>Lifecycle</NavLink>
        </nav>

        <form className="header-search" onSubmit={submitSearch} role="search">
          <input
            type="search"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            placeholder="Search products…"
            aria-label="Search products"
          />
          <button type="submit" aria-label="Search">⌕</button>
        </form>

        <div className="header-actions">
          <div className="cat-drop">
            <button type="button" className="icon-btn" onClick={() => { setCatOpen((v) => !v); setAcctOpen(false) }} aria-haspopup="true" aria-expanded={catOpen} title="Browse categories">
              ▤
            </button>
            {catOpen ? (
              <div className="dropdown cat-menu" onMouseLeave={() => setCatOpen(false)}>
                {categories.map((c) => (
                  <Link key={c.id} to={`/products?category=${encodeURIComponent(c.slug)}`} onClick={() => setCatOpen(false)}>
                    <span>{c.name}</span>
                    <span className="count">{c.productCount}</span>
                  </Link>
                ))}
              </div>
            ) : null}
          </div>

          <Link to="/wishlist" className="icon-btn" title="Wishlist">
            ♥{wishCount > 0 ? <span className="icon-badge">{wishCount}</span> : null}
          </Link>
          <Link to="/cart" className="icon-btn" title="Cart">
            🛒{itemCount > 0 ? <span className="icon-badge">{itemCount}</span> : null}
          </Link>

          <div className="acct-drop">
            <button
              type="button"
              className="icon-btn"
              onClick={() => { setAcctOpen((v) => !v); setCatOpen(false) }}
              aria-haspopup="true"
              aria-expanded={acctOpen}
              title="Account"
            >
              {isAuthenticated ? '👤' : '⎆'}
            </button>
            {acctOpen ? (
              <div className="dropdown acct-menu" onMouseLeave={() => setAcctOpen(false)}>
                {isAuthenticated ? (
                  <>
                    <div className="acct-summary">
                      <strong>{user.fullName}</strong>
                      <span>{user.email}</span>
                    </div>
                    <Link to="/account" onClick={() => setAcctOpen(false)}>My Account</Link>
                    <Link to="/orders" onClick={() => setAcctOpen(false)}>Orders</Link>
                    <Link to="/wishlist" onClick={() => setAcctOpen(false)}>Wishlist</Link>
                    <Link to="/addresses" onClick={() => setAcctOpen(false)}>Addresses</Link>
                    {isAdmin ? <Link to="/admin" onClick={() => setAcctOpen(false)}>Admin Console</Link> : null}
                    <button type="button" onClick={handleLogout}>Sign out</button>
                  </>
                ) : (
                  <>
                    <Link to="/login" onClick={() => setAcctOpen(false)}>Sign in</Link>
                    <Link to="/register" onClick={() => setAcctOpen(false)}>Create account</Link>
                  </>
                )}
              </div>
            ) : null}
          </div>

          <button type="button" className="icon-btn hamburger" onClick={() => setOpen((v) => !v)} aria-label="Toggle menu">
            ☰
          </button>
        </div>
      </div>
    </header>
  )
}