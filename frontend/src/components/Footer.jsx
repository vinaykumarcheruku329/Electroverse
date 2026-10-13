import { Link } from 'react-router-dom'

export function Footer() {
  return (
    <footer className="footer">
      <div className="container footer-grid">
        <div className="footer-brand">
          <span className="brand-mark">EV</span>
          <p className="footer-tagline">Premium electronics, thoughtfully chosen. Built for longevity, performance, and value.</p>
        </div>
        <div className="footer-col">
          <h4>Shop</h4>
          <Link to="/products">All products</Link>
          <Link to="/products?sort=price_desc">Premium picks</Link>
          <Link to="/products?featured=true">Featured</Link>
          <Link to="/products?inStock=true">In stock</Link>
        </div>
        <div className="footer-col">
          <h4>Tools</h4>
          <Link to="/finder">Smart Device Finder</Link>
          <Link to="/compare">Compare products</Link>
          <Link to="/compatibility">Compatibility Lab</Link>
          <Link to="/price-intelligence">Price Intelligence</Link>
          <Link to="/lifecycle">Tech Lifecycle Score</Link>
        </div>
        <div className="footer-col">
          <h4>Account</h4>
          <Link to="/cart">Cart</Link>
          <Link to="/wishlist">Wishlist</Link>
          <Link to="/orders">Orders</Link>
          <Link to="/account">Account</Link>
        </div>
      </div>
      <div className="container footer-bottom">
        <span>© {new Date().getFullYear()} ElectroVerse. All rights reserved.</span>
        <span>Electric an e-commerce experience that ages well.</span>
      </div>
    </footer>
  )
}