/* ============================================
   FASHION STORE — api.js
   Fetch wrapper with JWT auth injection,
   base URL config, error handling
   ============================================ */

const API_BASE = 'http://localhost:8080/api';

/**
 * Core fetch wrapper that handles:
 * - Base URL prefixing
 * - JWT Authorization header injection
 * - JSON body serialization
 * - Error response parsing
 */
async function apiFetch(path, options = {}) {
  const token = getAuthToken();

  const headers = {
    'Content-Type': 'application/json',
    ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
    ...options.headers,
  };

  const config = {
    ...options,
    headers,
  };

  if (options.body && typeof options.body === 'object') {
    config.body = JSON.stringify(options.body);
  }

  try {
    const response = await fetch(`${API_BASE}${path}`, config);

    // Handle empty response (e.g., 204 No Content)
    if (response.status === 204) return null;

    const data = await response.json().catch(() => null);

    if (!response.ok) {
      const message = data?.message || data?.error || `HTTP ${response.status}`;
      throw new ApiError(response.status, message, data);
    }

    return data;
  } catch (err) {
    if (err instanceof ApiError) throw err;
    throw new ApiError(0, 'Network error. Please check your connection.', null);
  }
}

class ApiError extends Error {
  constructor(status, message, data) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.data = data;
  }
}

/* --- Auth helpers --- */
function getAuthToken() {
  return localStorage.getItem('fs_token');
}

function setAuthToken(token) {
  if (token) localStorage.setItem('fs_token', token);
  else localStorage.removeItem('fs_token');
}

function getAuthUser() {
  try {
    const raw = localStorage.getItem('fs_user');
    return raw ? JSON.parse(raw) : null;
  } catch { return null; }
}

function setAuthUser(user) {
  if (user) localStorage.setItem('fs_user', JSON.stringify(user));
  else localStorage.removeItem('fs_user');
}

function isLoggedIn() {
  return !!getAuthToken();
}

function isAdmin() {
  const user = getAuthUser();
  return user?.role === 'ADMIN';
}

/* --- Named API methods --- */
const api = {
  // Auth
  register: (data) => apiFetch('/auth/register', { method: 'POST', body: data }),
  login:    (data) => apiFetch('/auth/login',    { method: 'POST', body: data }),
  me:       ()     => apiFetch('/auth/me'),

  // Products
  getProducts: (params = {}) => {
    const qs = new URLSearchParams(params).toString();
    return apiFetch(`/products${qs ? '?' + qs : ''}`);
  },
  getProduct:     (id)  => apiFetch(`/products/${id}`),
  createProduct:  (data) => apiFetch('/products', { method: 'POST', body: data }),
  updateProduct:  (id, data) => apiFetch(`/products/${id}`, { method: 'PUT', body: data }),
  deleteProduct:  (id)  => apiFetch(`/products/${id}`, { method: 'DELETE' }),

  // Categories
  getCategories: () => apiFetch('/categories'),

  // Cart
  getCart:       ()           => apiFetch('/cart'),
  addToCart:     (data)       => apiFetch('/cart', { method: 'POST', body: data }),
  updateCartItem:(id, data)   => apiFetch(`/cart/${id}`, { method: 'PUT', body: data }),
  removeCartItem:(id)         => apiFetch(`/cart/${id}`, { method: 'DELETE' }),

  // Orders
  checkout:      (data)   => apiFetch('/orders/checkout', { method: 'POST', body: data }),
  getOrders:     ()       => apiFetch('/orders'),
  getOrder:      (id)     => apiFetch(`/orders/${id}`),

  // Admin
  adminGetOrders: (page = 0, size = 20) => apiFetch(`/admin/orders?page=${page}&size=${size}`),
  adminUpdateOrderStatus: (id, status) =>
    apiFetch(`/admin/orders/${id}/status`, { method: 'PUT', body: { status } }),
  adminGetStats: () => apiFetch('/admin/stats'),

  // Payments
  initiatePayment: (data) => apiFetch('/payments/initiate', { method: 'POST', body: data }),
};

// Export for use across pages
window.api = api;
window.ApiError = ApiError;
window.getAuthToken = getAuthToken;
window.setAuthToken = setAuthToken;
window.getAuthUser = getAuthUser;
window.setAuthUser = setAuthUser;
window.isLoggedIn = isLoggedIn;
window.isAdmin = isAdmin;
