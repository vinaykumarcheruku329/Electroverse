import { NavLink } from 'react-router-dom'

const LINKS = [
  { to: '/admin', label: 'Dashboard', end: true },
  { to: '/admin/products', label: 'Products' },
  { to: '/admin/categories', label: 'Categories' },
  { to: '/admin/orders', label: 'Orders' },
  { to: '/admin/reviews', label: 'Reviews' },
  { to: '/admin/users', label: 'Users' },
  { to: '/admin/rules', label: 'Compatibility' },
  { to: '/admin/lifecycle', label: 'Lifecycle' },
  { to: '/admin/price-snapshots', label: 'Price Snapshots' }
]

export function AdminLayout({ title, subtitle, actions, children }) {
  return (
    <div className="container admin-layout" style={{ paddingTop: 24, paddingBottom: 40 }}>
      <div className="page-hero admin-hero">
        <div>
          <span className="pill pill-bronze">Admin console</span>
          <h1 style={{ marginTop: 8 }}>{title}</h1>
          {subtitle ? <p>{subtitle}</p> : null}
        </div>
        {actions ? <div className="admin-hero-actions">{actions}</div> : null}
      </div>

      <nav className="admin-nav" aria-label="Admin sections">
        {LINKS.map((l) => (
          <NavLink key={l.to} to={l.to} end={l.end} className={({ isActive }) => (isActive ? 'admin-nav-link active' : 'admin-nav-link')}>
            {l.label}
          </NavLink>
        ))}
      </nav>

      <div className="admin-content">{children}</div>
    </div>
  )
}
