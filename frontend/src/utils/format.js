export const DEFAULT_CURRENCY = 'INR'

export function formatINR(amount) {
  if (amount === null || amount === undefined || amount === '') return '—'
  const num = Number(amount)
  if (Number.isNaN(num)) return '—'
  try {
    return new Intl.NumberFormat('en-IN', {
      style: 'currency',
      currency: 'INR',
      minimumFractionDigits: 0,
      maximumFractionDigits: 2,
    }).format(num)
  } catch {
    return `₹${num.toLocaleString('en-IN')}`
  }
}

export function formatCurrency(value, currency = DEFAULT_CURRENCY) {
  if (currency && currency.toUpperCase() !== 'INR') {
    if (value === null || value === undefined || value === '') return '—'
    const num = Number(value)
    if (Number.isNaN(num)) return '—'
    try {
      return new Intl.NumberFormat('en-IN', { style: 'currency', currency }).format(num)
    } catch {
      return formatINR(value)
    }
  }
  return formatINR(value)
}

export function formatNumber(value) {
  const num = Number(value)
  return Number.isNaN(num) ? '0' : num.toLocaleString('en-IN')
}

export function formatDate(value, withTime = false) {
  if (!value) return '—'
  const d = new Date(value)
  if (Number.isNaN(d.getTime())) return String(value)
  return d.toLocaleDateString('en-IN', withTime
    ? { year: 'numeric', month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit' }
    : { year: 'numeric', month: 'short', day: 'numeric' })
}

export function titleCase(value) {
  if (!value) return ''
  return String(value)
    .toLowerCase()
    .split(/[\s_]+/)
    .map((w) => (w ? w[0].toUpperCase() + w.slice(1) : w))
    .join(' ')
}

export function slugify(value) {
  if (!value) return ''
  return String(value)
    .toLowerCase()
    .trim()
    .replace(/[^a-z0-9]+/g, '-')
    .replace(/^-+|-+$/g, '')
}

export function truncate(value, max = 120) {
  if (!value) return ''
  return value.length > max ? `${value.slice(0, max - 1)}…` : value
}

export const FREE_SHIPPING_THRESHOLD = 5000
export const SHIPPING_FEE = 99

export const ORDER_STATUSES = ['PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED']
export const PAYMENT_METHODS = ['CASH_ON_DELIVERY', 'SIMULATED_CARD']
export const REVIEW_STATUSES = ['PENDING', 'APPROVED', 'REJECTED']
export const COMPAT_RELATIONS = ['EQUALS', 'NOT_EQUALS', 'IN', 'GREATER_OR_EQUAL', 'LESS_OR_EQUAL', 'CONTAINS']
export const COMPAT_RESULTS = ['COMPATIBLE', 'INCOMPATIBLE', 'CONDITIONAL', 'UNKNOWN']
