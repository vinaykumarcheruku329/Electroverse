import { useMemo, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { useUi } from '../context/UiContext'
import { validateRegister, hasErrors, passwordStrength } from '../utils/validation'

export default function RegisterPage() {
  const { register } = useAuth()
  const { toast } = useUi()
  const navigate = useNavigate()

  const [form, setForm] = useState({ fullName: '', email: '', password: '', confirmPassword: '', phone: '' })
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirm, setShowConfirm] = useState(false)
  const [errors, setErrors] = useState({})
  const [error, setError] = useState(null)
  const [busy, setBusy] = useState(false)

  const strength = useMemo(() => passwordStrength(form.password), [form.password])

  function field(name) {
    return (e) => {
      setForm((prev) => ({ ...prev, [name]: e.target.value }))
      setErrors((prev) => ({ ...prev, [name]: undefined }))
    }
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError(null)
    const validation = validateRegister(form)
    setErrors(validation)
    if (hasErrors(validation)) return

    setBusy(true)
    try {
      const user = await register({
        fullName: form.fullName.trim(),
        email: form.email.trim(),
        password: form.password,
        phone: form.phone.trim() || undefined
      })
      toast.success(`Welcome to ElectroVerse, ${user.fullName}!`)
      navigate('/', { replace: true })
    } catch (err) {
      if (err.fieldErrors?.length) {
        const mapped = {}
        err.fieldErrors.forEach((fe) => { mapped[fe.field] = fe.message })
        setErrors(mapped)
        setError(err.message)
      } else {
        setError(err.message || 'Unable to create your account.')
      }
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="auth-page">
      <div className="auth-card card">
        <div className="auth-head">
          <span className="brand-mark">EV</span>
          <h1>Create your account</h1>
          <p className="muted">Join ElectroVerse to shop, save and track.</p>
        </div>

        <form onSubmit={handleSubmit} noValidate>
          <div className="form-group">
            <label htmlFor="fullName">Full name</label>
            <input
              id="fullName"
              className="form-control"
              autoComplete="name"
              required
              value={form.fullName}
              onChange={field('fullName')}
              placeholder="Ananya Sharma"
              aria-invalid={Boolean(errors.fullName)}
            />
            {errors.fullName ? <p className="form-error">{errors.fullName}</p> : null}
          </div>

          <div className="form-group">
            <label htmlFor="email">Email</label>
            <input
              id="email"
              className="form-control"
              type="email"
              autoComplete="email"
              required
              value={form.email}
              onChange={field('email')}
              placeholder="you@example.com"
              aria-invalid={Boolean(errors.email)}
            />
            {errors.email ? <p className="form-error">{errors.email}</p> : null}
          </div>

          <div className="form-group">
            <label htmlFor="password">Password</label>
            <div className="input-wrap">
              <input
                id="password"
                className="form-control"
                type={showPassword ? 'text' : 'password'}
                autoComplete="new-password"
                required
                value={form.password}
                onChange={field('password')}
                placeholder="At least 8 characters"
                aria-invalid={Boolean(errors.password)}
              />
              <button
                type="button"
                className="input-toggle"
                onClick={() => setShowPassword((v) => !v)}
                aria-label={showPassword ? 'Hide password' : 'Show password'}
              >
                {showPassword ? 'Hide' : 'Show'}
              </button>
            </div>
            {form.password ? (
              <div className="password-strength" data-score={strength.score}>
                <div className="password-strength-bar">
                  <span style={{ width: `${(strength.score / 4) * 100}%` }} />
                </div>
                <span className="form-hint">{strength.label}</span>
              </div>
            ) : (
              <div className="form-hint">Use 8+ characters with a mix of letters, numbers and symbols.</div>
            )}
            {errors.password ? <p className="form-error">{errors.password}</p> : null}
          </div>

          <div className="form-group">
            <label htmlFor="confirmPassword">Confirm password</label>
            <div className="input-wrap">
              <input
                id="confirmPassword"
                className="form-control"
                type={showConfirm ? 'text' : 'password'}
                autoComplete="new-password"
                required
                value={form.confirmPassword}
                onChange={field('confirmPassword')}
                placeholder="Re-enter your password"
                aria-invalid={Boolean(errors.confirmPassword)}
              />
              <button
                type="button"
                className="input-toggle"
                onClick={() => setShowConfirm((v) => !v)}
                aria-label={showConfirm ? 'Hide password' : 'Show password'}
              >
                {showConfirm ? 'Hide' : 'Show'}
              </button>
            </div>
            {errors.confirmPassword ? <p className="form-error">{errors.confirmPassword}</p> : null}
          </div>

          <div className="form-group">
            <label htmlFor="phone">Phone <span className="muted small">(optional)</span></label>
            <input
              id="phone"
              className="form-control"
              type="tel"
              autoComplete="tel"
              inputMode="tel"
              value={form.phone}
              onChange={field('phone')}
              placeholder="98765 43210"
              aria-invalid={Boolean(errors.phone)}
            />
            {errors.phone ? <p className="form-error">{errors.phone}</p> : null}
          </div>

          {error ? <p className="form-error auth-error">{error}</p> : null}

          <button type="submit" className="btn btn-primary btn-block btn-lg" disabled={busy}>
            {busy ? 'Creating account…' : 'Create account'}
          </button>
        </form>

        <p className="muted small auth-switch">
          Already have an account? <Link to="/login">Sign in</Link>
        </p>
      </div>
    </div>
  )
}
