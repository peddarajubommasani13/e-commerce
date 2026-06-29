/* ============================================
   ACCOUNT PAGE — account.js
   ============================================ */

document.addEventListener('DOMContentLoaded', async () => {
  if (!requireAuth()) return;
  
  loadUserProfile();
  await loadUserOrders();

  // If orderId is present in URL (e.g. redirected from checkout), open it
  const urlOrderId = getUrlParam('orderId');
  if (urlOrderId) {
    showOrderDetails(urlOrderId);
  }
});

function loadUserProfile() {
  const user = getAuthUser();
  if (!user) return;

  document.getElementById('profile-name').textContent = user.name;
  document.getElementById('profile-email').textContent = user.email;
  document.getElementById('profile-role').textContent = user.role === 'ADMIN' ? 'Administrator' : 'Customer';
}

async function loadUserOrders() {
  const loader = document.getElementById('orders-loader');
  const emptyState = document.getElementById('orders-empty-state');
  const list = document.getElementById('orders-list');

  try {
    const orders = await api.getOrders();

    loader.style.display = 'none';

    if (!orders || orders.length === 0) {
      emptyState.style.display = 'block';
      list.style.display = 'none';
      return;
    }

    emptyState.style.display = 'none';
    list.style.display = 'flex';

    list.innerHTML = orders.map(order => `
      <div class="order-card-row" onclick="showOrderDetails(${order.id})">
        <div class="order-meta-info">
          <span class="order-meta-id">Order #${order.id}</span>
          <span class="order-meta-date">${formatDateTime(order.createdAt)}</span>
        </div>
        <div class="order-status-group">
          <span style="font-weight: 700; margin-right: 12px;">${formatPrice(order.totalAmount)}</span>
          ${orderStatusBadge(order.status)}
          ${paymentStatusBadge(order.paymentStatus)}
        </div>
      </div>
    `).join('');

  } catch (err) {
    loader.innerHTML = `
      <p class="text-error text-center">Failed to load order history: ${escapeHtml(err.message)}</p>
    `;
  }
}

async function showOrderDetails(orderId) {
  const sidebar = document.getElementById('order-details-sidebar');
  
  // Set values to skeleton/loader
  document.getElementById('detail-order-id').textContent = 'Loading...';
  document.getElementById('detail-order-date').textContent = '';
  document.getElementById('detail-order-status').innerHTML = '';
  document.getElementById('detail-payment-status').innerHTML = '';
  document.getElementById('detail-shipping-address').textContent = '';
  document.getElementById('detail-items-list').innerHTML = '<span class="spinner spinner-sm"></span>';
  document.getElementById('detail-total-amount').textContent = '';
  
  sidebar.style.display = 'block';

  try {
    const order = await api.getOrder(orderId);

    document.getElementById('detail-order-id').textContent = `#${order.id}`;
    document.getElementById('detail-order-date').textContent = formatDateTime(order.createdAt);
    document.getElementById('detail-order-status').innerHTML = orderStatusBadge(order.status);
    document.getElementById('detail-payment-status').innerHTML = paymentStatusBadge(order.paymentStatus);
    document.getElementById('detail-shipping-address').textContent = order.shippingAddress;
    document.getElementById('detail-total-amount').textContent = formatPrice(order.totalAmount);

    document.getElementById('detail-items-list').innerHTML = order.items.map(item => `
      <div class="detail-item-line">
        <div style="display:flex; flex-direction:column;">
          <span style="font-weight:600;">${escapeHtml(item.productName)}</span>
          <span class="detail-item-qty">Qty: ${item.quantity} ${item.size ? `• Size: ${item.size}` : ''} ${item.color ? `• Color: ${item.color}` : ''}</span>
        </div>
        <span>${formatPrice(item.lineTotal)}</span>
      </div>
    `).join('');

  } catch (err) {
    toastError('Failed to load order details.');
    closeOrderDetails();
  }
}

function closeOrderDetails() {
  document.getElementById('order-details-sidebar').style.display = 'none';
}

// Bind closures to window
window.showOrderDetails = showOrderDetails;
window.closeOrderDetails = closeOrderDetails;
