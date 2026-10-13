import { describe, it, expect } from 'vitest'
import {
  isValidEmail,
  isValidIndianPhone,
  passwordStrength,
  validateLogin,
  validateRegister,
  hasErrors
} from './validation'

describe('validation utils', () => {
  it('validates email format', () => {
    expect(isValidEmail('user@example.com')).toBe(true)
    expect(isValidEmail('not-an-email')).toBe(false)
    expect(isValidEmail('')).toBe(false)
  })

  it('validates Indian mobile numbers', () => {
    expect(isValidIndianPhone('9876543210')).toBe(true)
    expect(isValidIndianPhone('+91 98765 43210')).toBe(true)
    expect(isValidIndianPhone('09876543210')).toBe(true)
    expect(isValidIndianPhone('12345')).toBe(false)
    expect(isValidIndianPhone('5876543210')).toBe(false)
    expect(isValidIndianPhone('')).toBe(false)
  })

  it('scores password strength', () => {
    expect(passwordStrength('short').score).toBe(0)
    expect(passwordStrength('password').score).toBeGreaterThan(0)
    expect(passwordStrength('Str0ng!Passw0rd').score).toBe(4)
  })

  it('rejects invalid login input', () => {
    const errors = validateLogin({ email: 'bad', password: '' })
    expect(hasErrors(errors)).toBe(true)
    expect(errors.email).toBeTruthy()
    expect(errors.password).toBeTruthy()
  })

  it('accepts valid login input', () => {
    expect(hasErrors(validateLogin({ email: 'user@example.com', password: 'secret' }))).toBe(false)
  })

  it('rejects mismatched registration passwords', () => {
    const errors = validateRegister({
      fullName: 'Ananya Sharma',
      email: 'ananya@example.com',
      password: 'StrongPass1!',
      confirmPassword: 'Different1!',
      phone: ''
    })
    expect(errors.confirmPassword).toBe('Passwords do not match')
  })

  it('rejects invalid Indian phone during registration', () => {
    const errors = validateRegister({
      fullName: 'Ananya Sharma',
      email: 'ananya@example.com',
      password: 'StrongPass1!',
      confirmPassword: 'StrongPass1!',
      phone: '12345'
    })
    expect(errors.phone).toBeTruthy()
  })

  it('accepts a valid registration payload', () => {
    const errors = validateRegister({
      fullName: 'Ananya Sharma',
      email: 'ananya@example.com',
      password: 'StrongPass1!',
      confirmPassword: 'StrongPass1!',
      phone: '9876543210'
    })
    expect(hasErrors(errors)).toBe(false)
  })
})
