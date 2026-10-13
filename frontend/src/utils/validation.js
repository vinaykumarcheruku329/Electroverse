const EMAIL_RE = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/

export function isValidEmail(value) {
  return EMAIL_RE.test(String(value || '').trim())
}

export function normalizePhone(value) {
  return String(value || '').replace(/[\s\-()]/g, '')
}

export function isValidIndianPhone(value) {
  if (!value) return false
  const normalized = normalizePhone(value)
  return /^(?:\+?91|0)?[6-9]\d{9}$/.test(normalized)
}

export function passwordStrength(value) {
  const pwd = String(value || '')
  let score = 0
  if (pwd.length >= 8) score += 1
  if (pwd.length >= 12) score += 1
  if (/[a-z]/.test(pwd) && /[A-Z]/.test(pwd)) score += 1
  if (/\d/.test(pwd)) score += 1
  if (/[^A-Za-z0-9]/.test(pwd)) score += 1
  const capped = Math.min(score, 4)
  const labels = ['Very weak', 'Weak', 'Fair', 'Good', 'Strong']
  return { score: capped, label: labels[capped] }
}

export function validateLogin({ email, password }) {
  const errors = {}
  if (!email || !String(email).trim()) errors.email = 'Email is required'
  else if (!isValidEmail(email)) errors.email = 'Enter a valid email address'
  if (!password) errors.password = 'Password is required'
  return errors
}

export function validateRegister({ fullName, email, password, confirmPassword, phone }) {
  const errors = {}
  if (!fullName || fullName.trim().length < 2) errors.fullName = 'Please enter your full name'
  if (!email || !String(email).trim()) errors.email = 'Email is required'
  else if (!isValidEmail(email)) errors.email = 'Enter a valid email address'
  if (!password) errors.password = 'Password is required'
  else if (password.length < 8) errors.password = 'Password must be at least 8 characters'
  if (!confirmPassword) errors.confirmPassword = 'Please confirm your password'
  else if (confirmPassword !== password) errors.confirmPassword = 'Passwords do not match'
  if (phone && String(phone).trim() && !isValidIndianPhone(phone)) {
    errors.phone = 'Enter a valid Indian mobile number (10 digits, starting 6-9)'
  }
  return errors
}

export function hasErrors(errors) {
  return Object.keys(errors).length > 0
}
