/* ============================================
   FASHION STORE — utils.js
   Toast notifications, formatting helpers,
   loading states, navigation guards
   ============================================ */

// Redirect to trailing slash version of /frontend context to fix relative path resolution
if (window.location.pathname.toLowerCase() === '/frontend') {
  window.location.replace(window.location.pathname + '/');
}

/* --- Toast System --- */
(function initToasts() {
  if (document.getElementById('toast-container')) return;
  const el = document.createElement('div');
  el.id = 'toast-container';
  el.className = 'toast-container';
  document.body.appendChild(el);
})();

const TOAST_ICONS = {
  success: '✓',
  error:   '✕',
  info:    'ℹ',
  default: '★',
};

function showToast(message, type = 'default', title = '', duration = 4000) {
  const container = document.getElementById('toast-container');
  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;

  const icon = TOAST_ICONS[type] || TOAST_ICONS.default;
  const titleHtml = title ? `<div class="toast-title">${escapeHtml(title)}</div>` : '';

  toast.innerHTML = `
    <span class="toast-icon">${icon}</span>
    <div class="toast-body">
      ${titleHtml}
      <div class="toast-message">${escapeHtml(message)}</div>
    </div>
    <button class="toast-close" aria-label="Close">×</button>
  `;

  toast.querySelector('.toast-close').addEventListener('click', () => dismissToast(toast));
  container.appendChild(toast);

  const timer = setTimeout(() => dismissToast(toast), duration);
  toast._timer = timer;
  return toast;
}

function dismissToast(toast) {
  clearTimeout(toast._timer);
  toast.classList.add('toast-out');
  toast.addEventListener('animationend', () => toast.remove(), { once: true });
}

function toastSuccess(msg, title) { showToast(msg, 'success', title); }
function toastError(msg, title)   { showToast(msg, 'error',   title || 'Error'); }
function toastInfo(msg, title)    { showToast(msg, 'info',    title); }

/* --- Price Formatting --- */
function formatPrice(amount) {
  if (amount == null) return '$0.00';
  return '$' + Number(amount).toFixed(2);
}

function renderPriceHtml(price, discountPrice) {
  if (discountPrice && Number(discountPrice) < Number(price)) {
    return `
      <span class="price price-discount">${formatPrice(discountPrice)}</span>
      <span class="price-original">${formatPrice(price)}</span>
    `;
  }
  return `<span class="price">${formatPrice(price)}</span>`;
}

/* --- Date Formatting --- */
function formatDate(isoString) {
  if (!isoString) return '';
  const d = new Date(isoString);
  return d.toLocaleDateString('en-US', { year: 'numeric', month: 'short', day: 'numeric' });
}

function formatDateTime(isoString) {
  if (!isoString) return '';
  const d = new Date(isoString);
  return d.toLocaleString('en-US', {
    year: 'numeric', month: 'short', day: 'numeric',
    hour: '2-digit', minute: '2-digit'
  });
}

/* --- Star Rating --- */
function renderStars(rating, maxStars = 5) {
  let html = '<div class="stars">';
  for (let i = 1; i <= maxStars; i++) {
    html += `<span class="star${i <= Math.round(rating) ? '' : ' empty'}">★</span>`;
  }
  html += '</div>';
  return html;
}

/* --- HTML Sanitization --- */
function escapeHtml(str) {
  if (!str) return '';
  const div = document.createElement('div');
  div.textContent = String(str);
  return div.innerHTML;
}

/* --- URL Params --- */
function getUrlParam(key) {
  return new URLSearchParams(window.location.search).get(key);
}

function setUrlParam(key, value) {
  const url = new URL(window.location.href);
  if (value !== null && value !== undefined && value !== '') {
    url.searchParams.set(key, value);
  } else {
    url.searchParams.delete(key);
  }
  window.history.replaceState({}, '', url.toString());
}

/* --- Loading State --- */
function setLoading(element, loading) {
  if (!element) return;
  if (loading) {
    element.disabled = true;
    element._origText = element.innerHTML;
    element.innerHTML = `<span class="spinner spinner-sm"></span>`;
  } else {
    element.disabled = false;
    if (element._origText) element.innerHTML = element._origText;
  }
}

/* --- Navigation Guards --- */
function requireAuth(redirectTo = 'pages/login.html') {
  if (!isLoggedIn()) {
    const resolvedRedirect = resolveRoot(redirectTo);
    const current = encodeURIComponent(window.location.pathname + window.location.search);
    window.location.href = `${resolvedRedirect}?redirect=${current}`;
    return false;
  }
  return true;
}

function requireAdmin() {
  if (!isLoggedIn() || !isAdmin()) {
    toastError('Admin access required.');
    window.location.href = resolveRoot('index.html');
    return false;
  }
  return true;
}

function redirectIfLoggedIn(to = 'index.html') {
  if (isLoggedIn()) {
    window.location.href = resolveRoot(to);
    return true;
  }
  return false;
}

/* --- Cart Count Badge Update --- */
function updateCartBadge(count) {
  const badge = document.getElementById('cart-count-badge');
  if (!badge) return;
  if (count && count > 0) {
    badge.textContent = count > 99 ? '99+' : count;
    badge.style.display = 'flex';
  } else {
    badge.style.display = 'none';
  }
}

async function refreshCartBadge() {
  if (!isLoggedIn()) { updateCartBadge(0); return; }
  try {
    const cart = await api.getCart();
    updateCartBadge(cart?.itemCount || 0);
  } catch { /* silent */ }
}

/* --- Skeleton Helpers --- */
function skeletonCard() {
  return `
    <div class="product-card">
      <div class="skeleton" style="height:280px;margin-bottom:12px;border-radius:8px"></div>
      <div class="skeleton" style="height:16px;width:70%;margin-bottom:8px"></div>
      <div class="skeleton" style="height:14px;width:40%;margin-bottom:8px"></div>
      <div class="skeleton" style="height:20px;width:50%"></div>
    </div>
  `;
}

function skeletonRow(cols = 4) {
  return Array(cols).fill(skeletonCard()).join('');
}

/* --- Order Status Badge --- */
function orderStatusBadge(status) {
  const map = {
    PENDING:   'badge-warning',
    PAID:      'badge-success',
    SHIPPED:   'badge-info',
    DELIVERED: 'badge-success',
    CANCELLED: 'badge-error',
  };
  const cls = map[status] || 'badge-primary';
  return `<span class="badge ${cls}">${status}</span>`;
}

/* --- Payment Status Badge --- */
function paymentStatusBadge(status) {
  const cls = status === 'PAID' ? 'badge-success'
            : status === 'FAILED' ? 'badge-error' : 'badge-warning';
  return `<span class="badge ${cls}">${status}</span>`;
}

/* --- Resolve relative path --- */
function resolveRoot(path) {
  const pathname = window.location.pathname;
  const pathnameLower = pathname.toLowerCase();
  const isFrontendCtx = pathnameLower.startsWith('/frontend') || pathnameLower.includes('/frontend/');
  if (isFrontendCtx) {
    const match = pathname.match(/\/frontend\/?/i);
    const prefix = match ? match[0] : '/frontend/';
    const root = prefix.endsWith('/') ? prefix : prefix + '/';
    return root + path;
  }
  return '/' + path;
}


/* --- Product Card (shared across home, shop, product pages) --- */
function renderProductCard(p) {
  const imageUrl = p.imageUrls?.[0] || 'https://images.unsplash.com/photo-1445205170230-053b83016050?w=600&q=80';
  const hasDiscount = p.discountPrice && Number(p.discountPrice) < Number(p.price);

  return `
    <article class="product-card" data-product-id="${p.id}">
      <div class="product-card-image-wrap">
        <img class="product-card-img" src="${escapeHtml(imageUrl)}" alt="${escapeHtml(p.name)}" loading="lazy"/>
        ${hasDiscount ? `<div class="product-card-badge"><span class="badge badge-accent">SALE</span></div>` : ''}
        <div class="product-card-quick-add">Quick Add</div>
      </div>
      <div class="product-card-body">
        <div class="product-card-category">${escapeHtml(p.category?.name || '')}</div>
        <h3 class="product-card-name">${escapeHtml(p.name)}</h3>
        <div class="product-card-rating">
          ${renderStars(p.rating || 0)}
          <span>(${p.reviewCount || 0})</span>
        </div>
      </div>
      <div class="product-card-footer">
        <div class="price-wrap">
          ${hasDiscount
            ? `<span class="price price-discount">${formatPrice(p.discountPrice)}</span>
               <span class="price-original">${formatPrice(p.price)}</span>`
            : `<span class="price">${formatPrice(p.price)}</span>`
          }
        </div>
        <button class="product-card-add-btn" data-add-to-cart data-product-id="${p.id}" data-name="${escapeHtml(p.name)}" title="Add to cart">
          +
        </button>
      </div>
    </article>
  `;
}

async function handleQuickAddToCart(productId, productName) {
  if (!isLoggedIn()) {
    toastInfo('Please sign in to add items to your cart.');
    setTimeout(() => {
      window.location.href = resolveRoot('pages/login.html');
    }, 1500);
    return;
  }

  try {
    await api.addToCart({ productId: Number(productId), quantity: 1 });
    toastSuccess(`${productName} added to cart!`, 'Added ✓');
    refreshCartBadge();
  } catch (err) {
    toastError(err.message || 'Could not add to cart.');
  }
}

// Export globals
window.showToast = showToast;
window.toastSuccess = toastSuccess;
window.toastError = toastError;
window.toastInfo = toastInfo;
window.formatPrice = formatPrice;
window.renderPriceHtml = renderPriceHtml;
window.formatDate = formatDate;
window.formatDateTime = formatDateTime;
window.renderStars = renderStars;
window.escapeHtml = escapeHtml;
window.getUrlParam = getUrlParam;
window.setUrlParam = setUrlParam;
window.setLoading = setLoading;
window.requireAuth = requireAuth;
window.requireAdmin = requireAdmin;
window.redirectIfLoggedIn = redirectIfLoggedIn;
window.updateCartBadge = updateCartBadge;
window.refreshCartBadge = refreshCartBadge;
window.skeletonCard = skeletonCard;
window.skeletonRow = skeletonRow;
window.orderStatusBadge = orderStatusBadge;
window.paymentStatusBadge = paymentStatusBadge;
window.resolveRoot = resolveRoot;
window.renderProductCard = renderProductCard;
window.handleQuickAddToCart = handleQuickAddToCart;
