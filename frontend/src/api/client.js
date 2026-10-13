const RAW_BASE = import.meta.env.VITE_API_BASE_URL || '/api/v1'
export const API_BASE = RAW_BASE.replace(/\/$/, '')

const TOKEN_KEY = 'ev_token'

export function getToken() {
  return localStorage.getItem(TOKEN_KEY)
}

export function setToken(token) {
  if (token) localStorage.setItem(TOKEN_KEY, token)
  else localStorage.removeItem(TOKEN_KEY)
}

export class ApiError extends Error {
  constructor(status, message, fieldErrors) {
    super(message || `Request failed (${status})`)
    this.name = 'ApiError'
    this.status = status
    this.fieldErrors = fieldErrors || null
  }
}

function buildQuery(params) {
  if (!params) return ''
  const usp = new URLSearchParams()
  Object.entries(params).forEach(([k, v]) => {
    if (v === undefined || v === null || v === '') return
    if (Array.isArray(v)) v.forEach((item) => usp.append(k, item))
    else usp.append(k, v)
  })
  const s = usp.toString()
  return s ? `?${s}` : ''
}

export async function request(path, options = {}) {
  const { method = 'GET', body, params, auth = false } = options
  const headers = { Accept: 'application/json' }
  if (body !== undefined) headers['Content-Type'] = 'application/json'

  const token = getToken()
  if (auth && token) headers.Authorization = `Bearer ${token}`

  let res
  try {
    res = await fetch(`${API_BASE}${path}${buildQuery(params)}`, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined
    })
  } catch (networkErr) {
    throw new ApiError(0, 'Cannot reach the server. Is the backend running?')
  }

  if (res.status === 204) return null

  const text = await res.text()
  let data = null
  if (text) {
    try {
      data = JSON.parse(text)
    } catch {
      data = text
    }
  }

  if (!res.ok) {
    if (res.status === 401) {
      setToken(null)
      window.dispatchEvent(new CustomEvent('ev:unauthorized'))
    }
    let message = `Request failed (${res.status})`
    let fieldErrors = null
    if (data && typeof data === 'object') {
      message = data.message || data.error || message
      if (Array.isArray(data.fieldErrors)) fieldErrors = data.fieldErrors
    }
    throw new ApiError(res.status, message, fieldErrors)
  }

  return data
}

export const api = {
  get: (path, opts) => request(path, { ...opts, method: 'GET' }),
  post: (path, body, opts) => request(path, { ...opts, method: 'POST', body }),
  put: (path, body, opts) => request(path, { ...opts, method: 'PUT', body }),
  patch: (path, body, opts) => request(path, { ...opts, method: 'PATCH', body }),
  del: (path, opts) => request(path, { ...opts, method: 'DELETE' })
}
