/* ============================================
   ADMIN DASHBOARD — admin.js
   Overview Stats, Product CRUD, Order Status Updates
   ============================================ */

document.addEventListener('DOMContentLoaded', async () => {
  if (!requireAdmin()) return;

  // Load active tab initially
  switchTab('stats');
});

function switchTab(tabId) {
  // Toggle tab buttons
  document.querySelectorAll('.tab-btn').forEach(btn => {
    const isActive = btn.outerHTML.includes(`'${tabId}'`);
    btn.classList.toggle('active', isActive);
  });

  // Toggle tab contents
  document.querySelectorAll('.tab-content').forEach(content => {
    const isTarget = content.id === `tab-${tabId}`;
    content.classList.toggle('active', isTarget);
  });

  // Load specific tab data
  if (tabId === 'stats') {
    loadOverviewStats();
  } else if (tabId === 'products') {
    loadAdminProducts();
  } else if (tabId === 'orders') {
    loadAdminOrders();
  }
}

async function loadOverviewStats() {
  const loader = document.getElementById('stats-loader');
  const grid = document.getElementById('stats-grid');

  try {
    const stats = await api.adminGetStats();
    
    document.getElementById('stat-revenue').textContent = formatPrice(stats.totalRevenue);
    document.getElementById('stat-orders').textContent = stats.totalOrders;
    document.getElementById('stat-users').textContent = stats.totalUsers;
    document.getElementById('stat-products').textContent = stats.totalProducts;

    loader.style.display = 'none';
    grid.style.display = 'grid';

  } catch (err) {
    loader.innerHTML = `<p class="text-error text-center">Failed to load statistics: ${escapeHtml(err.message)}</p>`;
  }
}

async function loadAdminProducts() {
  const tbody = document.getElementById('admin-products-tbody');
  tbody.innerHTML = '<tr><td colspan="7" class="text-center"><span class="spinner spinner-sm"></span></td></tr>';

  try {
    const result = await api.getProducts({ page: 0, size: 100, sortBy: 'createdAt', sortDir: 'desc' });
    const products = result.content || [];

    if (products.length === 0) {
      tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted">No products found.</td></tr>';
      return;
    }

    tbody.innerHTML = products.map(p => {
      const img = p.imageUrls?.[0] || 'https://images.unsplash.com/photo-1445205170230-053b83016050?w=600&q=80';
      return `
        <tr>
          <td><strong>#${p.id}</strong></td>
          <td><img src="${img}" alt="${escapeHtml(p.name)}"></td>
          <td>${escapeHtml(p.name)}</td>
          <td>${escapeHtml(p.category?.name || 'Uncategorized')}</td>
          <td><strong>${formatPrice(p.price)}</strong></td>
          <td>${p.stockQuantity}</td>
          <td>
            <div style="display:flex; gap: 8px;">
              <button class="btn btn-outline btn-sm" onclick="editProduct(${p.id})">Edit</button>
              <button class="btn btn-ghost btn-sm" style="color:var(--color-error);" onclick="deleteProduct(${p.id})">Delete</button>
            </div>
          </td>
        </tr>
      `;
    }).join('');

  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="7" class="text-center text-error">Failed to load products: ${escapeHtml(err.message)}</td></tr>`;
  }
}

async function loadAdminOrders() {
  const tbody = document.getElementById('admin-orders-tbody');
  tbody.innerHTML = '<tr><td colspan="7" class="text-center"><span class="spinner spinner-sm"></span></td></tr>';

  try {
    const result = await api.adminGetOrders(0, 100);
    const orders = result.content || [];

    if (orders.length === 0) {
      tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted">No orders found.</td></tr>';
      return;
    }

    tbody.innerHTML = orders.map(o => `
      <tr>
        <td><strong>#${o.id}</strong></td>
        <td>
          <div style="font-weight:600;">${escapeHtml(o.userName)}</div>
          <div class="text-muted" style="font-size:var(--text-xs);">${escapeHtml(o.shippingAddress)}</div>
        </td>
        <td>${formatDateTime(o.createdAt)}</td>
        <td><strong>${formatPrice(o.totalAmount)}</strong></td>
        <td>${orderStatusBadge(o.status)}</td>
        <td>${paymentStatusBadge(o.paymentStatus)}</td>
        <td>
          <select class="form-select form-input-sm" onchange="updateStatus(${o.id}, this.value)" style="min-width:130px;">
            <option value="">Choose Status</option>
            <option value="PENDING" ${o.status === 'PENDING' ? 'disabled' : ''}>PENDING</option>
            <option value="PAID" ${o.status === 'PAID' ? 'disabled' : ''}>PAID</option>
            <option value="SHIPPED" ${o.status === 'SHIPPED' ? 'disabled' : ''}>SHIPPED</option>
            <option value="DELIVERED" ${o.status === 'DELIVERED' ? 'disabled' : ''}>DELIVERED</option>
            <option value="CANCELLED" ${o.status === 'CANCELLED' ? 'disabled' : ''}>CANCELLED</option>
          </select>
        </td>
      </tr>
    `).join('');

  } catch (err) {
    tbody.innerHTML = `<tr><td colspan="7" class="text-center text-error">Failed to load orders: ${escapeHtml(err.message)}</td></tr>`;
  }
}

async function updateStatus(orderId, status) {
  if (!status) return;

  try {
    await api.adminUpdateOrderStatus(orderId, status);
    toastSuccess(`Order #${orderId} updated to ${status}.`);
    await loadAdminOrders();
  } catch (err) {
    toastError(err.message || 'Failed to update order status.');
  }
}

/* Modal and Product CRUD Actions */
function openProductModal() {
  document.getElementById('modal-title').textContent = 'Add New Product';
  document.getElementById('form-product-id').value = '';
  document.getElementById('product-form').reset();
  document.getElementById('product-modal').classList.add('open');
}

function closeProductModal() {
  document.getElementById('product-modal').classList.remove('open');
}

async function editProduct(id) {
  openProductModal();
  document.getElementById('modal-title').textContent = `Edit Product #${id}`;
  document.getElementById('form-product-id').value = id;

  try {
    const p = await api.getProduct(id);
    document.getElementById('form-name').value = p.name;
    document.getElementById('form-desc').value = p.description || '';
    document.getElementById('form-price').value = p.price;
    document.getElementById('form-discount').value = p.discountPrice || '';
    document.getElementById('form-category').value = p.category?.id || '1';
    document.getElementById('form-stock').value = p.stockQuantity;
    document.getElementById('form-images').value = p.imageUrls ? p.imageUrls.join(', ') : '';
    document.getElementById('form-sizes').value = p.sizes ? p.sizes.join(', ') : '';
    document.getElementById('form-colors').value = p.colors ? p.colors.join(', ') : '';

  } catch (err) {
    toastError('Failed to fetch product details.');
    closeProductModal();
  }
}

async function handleSaveProduct(event) {
  event.preventDefault();
  const saveBtn = document.getElementById('form-save-btn');
  setLoading(saveBtn, true);

  const id = document.getElementById('form-product-id').value;
  const payload = {
    name: document.getElementById('form-name').value.trim(),
    description: document.getElementById('form-desc').value.trim(),
    price: Number(document.getElementById('form-price').value),
    discountPrice: document.getElementById('form-discount').value ? Number(document.getElementById('form-discount').value) : null,
    categoryId: Number(document.getElementById('form-category').value),
    stockQuantity: Number(document.getElementById('form-stock').value),
    imageUrls: document.getElementById('form-images').value.split(',').map(s => s.trim()).filter(Boolean),
    sizes: document.getElementById('form-sizes').value.split(',').map(s => s.trim()).filter(Boolean),
    colors: document.getElementById('form-colors').value.split(',').map(s => s.trim()).filter(Boolean)
  };

  try {
    if (id) {
      await api.updateProduct(id, payload);
      toastSuccess('Product updated successfully!');
    } else {
      await api.createProduct(payload);
      toastSuccess('Product added successfully!');
    }
    
    closeProductModal();
    await loadAdminProducts();

  } catch (err) {
    toastError(err.message || 'Failed to save product.');
  } finally {
    setLoading(saveBtn, false);
  }
}

async function deleteProduct(id) {
  if (!confirm(`Are you sure you want to delete Product #${id}?`)) return;

  try {
    await api.deleteProduct(id);
    toastSuccess(`Product #${id} deleted.`);
    await loadAdminProducts();
  } catch (err) {
    toastError(err.message || 'Could not delete product.');
  }
}

// Bind closures to window
window.switchTab = switchTab;
window.updateStatus = updateStatus;
window.editProduct = editProduct;
window.deleteProduct = deleteProduct;
window.openProductModal = openProductModal;
window.closeProductModal = closeProductModal;
window.handleSaveProduct = handleSaveProduct;
