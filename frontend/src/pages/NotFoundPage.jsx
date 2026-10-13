import { Link } from 'react-router-dom'

export default function NotFoundPage() {
  return (
    <div className="container">
      <div className="state-view">
        <div className="state-icon">404</div>
        <h2>Page not found</h2>
        <p className="muted">The page you were looking for does not exist or has moved.</p>
        <Link to="/" className="btn btn-primary">Back to home</Link>
      </div>
    </div>
  )
}