import { api } from './client'

/* ---------------- Auth ---------------- */
export const authApi = {
  register: (payload) => api.post('/auth/register', payload),
  login: (payload) => api.post('/auth/login', payload),
  me: () => api.get('/users/me', { auth: true }),
  updateProfile: (payload) => api.put('/users/me', payload, { auth: true })
}

/* ---------------- Catalog ---------------- */
export const catalogApi = {
  products: (params) => api.get('/products', { params }),
  product: (id) => api.get(`/products/${id}`),
  specifications: (id) => api.get(`/products/${id}/specifications`),
  related: (id) => api.get(`/products/${id}/related`),
  featured: () => api.get('/products/featured'),
  newArrivals: () => api.get('/products/new-arrivals'),
  deals: () => api.get('/products/deals'),
  brands: () => api.get('/products/brands'),
  reviews: (productId, params) => api.get(`/products/${productId}/reviews`, { params }),
  addReview: (productId, payload) => api.post(`/products/${productId}/reviews`, payload, { auth: true })
}

export const categoryApi = {
  list: () => api.get('/categories')
}

/* ---------------- Cart ---------------- */
export const cartApi = {
  get: () => api.get('/cart', { auth: true }),
  add: (productId, quantity = 1) => api.post('/cart/items', { productId, quantity }, { auth: true }),
  update: (itemId, quantity) => api.patch(`/cart/items/${itemId}`, { quantity }, { auth: true }),
  remove: (itemId) => api.del(`/cart/items/${itemId}`, { auth: true })
}

/* ---------------- Wishlist ---------------- */
export const wishlistApi = {
  list: () => api.get('/wishlist', { auth: true }),
  add: (productId) => api.post(`/wishlist/${productId}`, undefined, { auth: true }),
  remove: (productId) => api.del(`/wishlist/${productId}`, { auth: true })
}

/* ---------------- Addresses ---------------- */
export const addressApi = {
  list: () => api.get('/addresses', { auth: true }),
  create: (payload) => api.post('/addresses', payload, { auth: true }),
  update: (id, payload) => api.put(`/addresses/${id}`, payload, { auth: true }),
  makeDefault: (id) => api.patch(`/addresses/${id}/default`, undefined, { auth: true }),
  remove: (id) => api.del(`/addresses/${id}`, { auth: true })
}

/* ---------------- Orders / Checkout ---------------- */
export const orderApi = {
  checkout: (payload) => api.post('/checkout', payload, { auth: true }),
  list: (params) => api.get('/orders', { params, auth: true }),
  get: (id) => api.get(`/orders/${id}`, { auth: true }),
  cancel: (id) => api.post(`/orders/${id}/cancel`, undefined, { auth: true })
}

/* ---------------- Feature: Finder ---------------- */
export const finderApi = {
  recommend: (payload) => api.post('/finder/recommendations', payload),
  session: (id) => api.get(`/finder/sessions/${id}`, { auth: true }),
  sessions: () => api.get('/finder/sessions', { auth: true }),
  criteria: () => api.get('/finder/criteria', { auth: true })
}

/* ---------------- Feature: Comparison ---------------- */
export const comparisonApi = {
  compare: (payload) => api.post('/comparisons', payload)
}

/* ---------------- Feature: Compatibility ---------------- */
export const compatibilityApi = {
  check: (payload) => api.post('/compatibility/check', payload),
  types: () => api.get('/compatibility/types', { auth: true })
}

/* ---------------- Feature: Price Intelligence ---------------- */
export const priceApi = {
  history: (productId) => api.get(`/products/${productId}/price-history`),
  alerts: () => api.get('/price-alerts', { auth: true }),
  createAlert: (payload) => api.post('/price-alerts', payload, { auth: true }),
  removeAlert: (id) => api.del(`/price-alerts/${id}`, { auth: true })
}

/* ---------------- Feature: Lifecycle ---------------- */
export const lifecycleApi = {
  get: (productId) => api.get(`/lifecycle/${productId}`),
  history: (productId) => api.get(`/lifecycle/${productId}/history`)
}

/* ---------------- Admin ---------------- */
export const adminApi = {
  dashboard: () => api.get('/admin/dashboard', { auth: true }),

  inventory: (lowStockOnly) => api.get('/admin/inventory', { params: { lowStockOnly }, auth: true }),

  categories: {
    create: (payload) => api.post('/admin/categories', payload, { auth: true }),
    update: (id, payload) => api.put(`/admin/categories/${id}`, payload, { auth: true }),
    remove: (id) => api.del(`/admin/categories/${id}`, { auth: true })
  },

  products: {
    list: (params) => api.get('/admin/products', { params, auth: true }),
    get: (id) => api.get(`/admin/products/${id}`, { auth: true }),
    create: (payload) => api.post('/admin/products', payload, { auth: true }),
    update: (id, payload) => api.put(`/admin/products/${id}`, payload, { auth: true }),
    updateStock: (id, stock) => api.patch(`/admin/products/${id}/stock`, { stock }, { auth: true }),
    setActive: (id, active) => api.patch(`/admin/products/${id}/active`, undefined, { params: { active }, auth: true }),
    deactivate: (id) => api.del(`/admin/products/${id}`, { auth: true }),
    addSnapshot: (id, payload) => api.post(`/admin/products/${id}/price-snapshots`, payload, { auth: true })
  },

  orders: {
    list: (params) => api.get('/admin/orders', { params, auth: true }),
    get: (id) => api.get(`/admin/orders/${id}`, { auth: true }),
    updateStatus: (id, payload) => api.patch(`/admin/orders/${id}/status`, payload, { auth: true })
  },

  reviews: {
    list: () => api.get('/admin/reviews', { auth: true }),
    setStatus: (id, status) => api.patch(`/admin/reviews/${id}/status`, undefined, { params: { status }, auth: true }),
    remove: (id) => api.del(`/admin/reviews/${id}`, { auth: true })
  },

  users: {
    list: (params) => api.get('/admin/users', { params, auth: true }),
    setActive: (id, active) => api.patch(`/admin/users/${id}/status`, { active }, { auth: true })
  },

  rules: {
    list: () => api.get('/admin/compatibility-rules', { auth: true }),
    create: (payload) => api.post('/admin/compatibility-rules', payload, { auth: true }),
    update: (id, payload) => api.put(`/admin/compatibility-rules/${id}`, payload, { auth: true }),
    remove: (id) => api.del(`/admin/compatibility-rules/${id}`, { auth: true })
  },

  lifecycle: {
    list: () => api.get('/admin/lifecycle', { auth: true }),
    create: (payload) => api.post('/admin/lifecycle', payload, { auth: true }),
    remove: (id) => api.del(`/admin/lifecycle/${id}`, { auth: true })
  },

  priceSnapshots: {
    recent: () => api.get('/admin/price-snapshots/recent', { auth: true })
  }
}
