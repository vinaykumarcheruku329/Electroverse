import { describe, it, expect } from 'vitest'
import { formatCurrency, formatINR, formatNumber, formatDate, titleCase, slugify, truncate, ORDER_STATUSES } from './format'

describe('format utils', () => {
  it('formats currency in Indian Rupees with Indian grouping', () => {
    expect(formatINR(999)).toBe('₹999')
    expect(formatINR(12499)).toBe('₹12,499')
    expect(formatINR(125999)).toBe('₹1,25,999')
    expect(formatINR('4398.00')).toBe('₹4,398')
    expect(formatINR(124417.5)).toBe('₹1,24,417.5')
    expect(formatINR(null)).toBe('—')
    expect(formatINR('not-a-number')).toBe('—')
  })

  it('defaults formatCurrency to INR', () => {
    expect(formatCurrency(2199)).toBe('₹2,199')
    expect(formatCurrency('4398.00')).toBe('₹4,398')
    expect(formatCurrency(null)).toBe('—')
  })

  it('still supports an explicit non-INR currency', () => {
    expect(formatCurrency(2199, 'USD')).toBe('$2,199.00')
  })

  it('formats numbers', () => {
    expect(formatNumber(12345)).toBe('12,345')
    expect(formatNumber('not-a-number')).toBe('0')
  })

  it('formats dates', () => {
    expect(formatDate(null)).toBe('—')
    expect(formatDate('2026-10-12T09:00:00')).toContain('2026')
  })

  it('title-cases and slugs', () => {
    expect(titleCase('in_stock')).toBe('In Stock')
    expect(slugify('Nova Gaming 17 RTX Laptop')).toBe('nova-gaming-17-rtx-laptop')
  })

  it('truncates long strings', () => {
    expect(truncate('abcdef', 4)).toBe('abc…')
    expect(truncate('ab', 4)).toBe('ab')
  })

  it('exposes order statuses', () => {
    expect(ORDER_STATUSES).toContain('DELIVERED')
    expect(ORDER_STATUSES).toHaveLength(6)
  })
})
