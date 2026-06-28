/* ============================================
   SHOP PAGE — shop.js
   Full filtering, sorting, pagination logic
   ============================================ */

let state = {
  page: 0,
  size: 12,
  categoryId: null,
  minPrice: null,
  maxPrice: null,
  keyword: '',
  sortBy: 'createdAt',
  sortDir: 'desc',
  totalPages: 0,
  totalElements: 0,
};

let searchDebounceTimer = null;

document.addEventListener('DOMContentLoaded', async () => {
  // Read URL params
  const params = new URLSearchParams(window.location.search);
  if (params.get('categoryId')) state.categoryId = Number(params.get('categoryId'));
  if (params.get('keyword'))    state.keyword    = params.get('keyword');
  if (params.get('page'))       state.page       = Number(params.get('page'));

  await loadCategoryFilters();
  loadProducts();

  // Event listeners
  document.getElementById('search-input').value = state.keyword;
  document.getElementById('search-input').addEventListener('input', (e) => {
    clearTimeout(searchDebounceTimer);
    searchDebounceTimer = setTimeout(() => {
      state.keyword = e.target.value.trim();
      state.page = 0;
      loadProducts();
    }, 400);
  });

  document.getElementById('sort-select').addEventListener('change', (e) => {
    const [sortBy, sortDir] = e.target.value.split(',');
    state.sortBy = sortBy;
    state.sortDir = sortDir;
    state.page = 0;
    loadProducts();
  });

  document.getElementById('apply-price-filter').addEventListener('click', () => {
    state.minPrice = document.getElementById('min-price').value || null;
    state.maxPrice = document.getElementById('max-price').value || null;
    state.page = 0;
    loadProducts();
  });

  document.getElementById('clear-filters').addEventListener('click', () => {
    state.categoryId = null;
    state.minPrice = null;
    state.maxPrice = null;
    state.keyword = '';
    state.page = 0;
    document.getElementById('search-input').value = '';
    document.getElementById('min-price').value = '';
    document.getElementById('max-price').value = '';
    document.querySelector('input[name="category"][value=""]').checked = true;
    loadProducts();
  });
});

async function loadCategoryFilters() {
  try {
    const categories = await api.getCategories();
    const container = document.getElementById('category-filters');
    const allItem = container.querySelector('label.filter-item');

    categories.forEach(cat => {
      const label = document.createElement('label');
      label.className = 'filter-item';
      label.innerHTML = `
        <input type="radio" name="category" value="${cat.id}"
          ${state.categoryId == cat.id ? 'checked' : ''}> ${escapeHtml(cat.name)}
      `;
      container.appendChild(label);
    });

    // Category change handler
    container.addEventListener('change', (e) => {
      state.categoryId = e.target.value ? Number(e.target.value) : null;
      state.page = 0;
      updatePageTitle();
      loadProducts();
    });

    updatePageTitle(categories);
  } catch (err) {
    console.error('Failed to load categories:', err);
  }
}

function updatePageTitle(categories = []) {
  const titleEl = document.getElementById('page-title');
  const breadcrumbEl = document.getElementById('breadcrumb-current');

  if (state.categoryId) {
    const cat = categories.find ? categories.find(c => c.id == state.categoryId) : null;
    const name = cat?.name || 'Category';
    if (titleEl) titleEl.textContent = name;
    if (breadcrumbEl) breadcrumbEl.textContent = name;
  } else {
    if (titleEl) titleEl.textContent = state.keyword ? `Search: "${state.keyword}"` : 'Shop All';
    if (breadcrumbEl) breadcrumbEl.textContent = 'Shop';
  }
}

async function loadProducts() {
  const grid = document.getElementById('product-grid');
  const resultsEl = document.getElementById('shop-results');

  grid.innerHTML = skeletonRow(state.size);
  resultsEl.textContent = 'Loading...';

  try {
    const params = {
      page: state.page,
      size: state.size,
      sortBy: state.sortBy,
      sortDir: state.sortDir,
    };
    if (state.categoryId) params.categoryId = state.categoryId;
    if (state.minPrice)   params.minPrice   = state.minPrice;
    if (state.maxPrice)   params.maxPrice   = state.maxPrice;
    if (state.keyword)    params.keyword    = state.keyword;

    const result = await api.getProducts(params);
    state.totalPages   = result.totalPages;
    state.totalElements = result.totalElements;

    resultsEl.textContent = `${result.totalElements} product${result.totalElements !== 1 ? 's' : ''} found`;

    if (!result.content || !result.content.length) {
      grid.innerHTML = `
        <div class="empty-state" style="grid-column:1/-1">
          <div class="empty-state-icon">🔍</div>
          <h3>No Products Found</h3>
          <p>Try adjusting your filters or search term.</p>
          <button class="btn btn-outline" onclick="document.getElementById('clear-filters').click()">Clear Filters</button>
        </div>
      `;
      document.getElementById('pagination').innerHTML = '';
      return;
    }

    grid.innerHTML = result.content.map(p => renderProductCard(p)).join('');

    // Event handlers
    grid.querySelectorAll('[data-add-to-cart]').forEach(btn => {
      btn.addEventListener('click', (e) => {
        e.preventDefault();
        e.stopPropagation();
        handleQuickAddToCart(btn.dataset.productId, btn.dataset.name);
      });
    });

    grid.querySelectorAll('.product-card').forEach(card => {
      card.addEventListener('click', (e) => {
        if (!e.target.closest('[data-add-to-cart]')) {
          window.location.href = `product.html?id=${card.dataset.productId}`;
        }
      });
    });

    renderPagination();

  } catch (err) {
    grid.innerHTML = `<div class="empty-state" style="grid-column:1/-1">
      <div class="empty-state-icon">⚠️</div>
      <h3>Failed to load products</h3>
      <p>${escapeHtml(err.message)}</p>
    </div>`;
    resultsEl.textContent = '';
  }
}

function renderPagination() {
  const container = document.getElementById('pagination');
  if (state.totalPages <= 1) { container.innerHTML = ''; return; }

  let html = '';

  // Prev
  html += `<button class="page-btn" ${state.page === 0 ? 'disabled' : ''} onclick="changePage(${state.page - 1})">‹</button>`;

  // Pages (show max 7 with ellipsis)
  const range = getPageRange(state.page, state.totalPages);
  range.forEach(p => {
    if (p === '...') {
      html += `<span style="display:flex;align-items:center;padding:0 4px;color:var(--color-text-muted)">…</span>`;
    } else {
      html += `<button class="page-btn ${p === state.page ? 'active' : ''}" onclick="changePage(${p})">${p + 1}</button>`;
    }
  });

  // Next
  html += `<button class="page-btn" ${state.page >= state.totalPages - 1 ? 'disabled' : ''} onclick="changePage(${state.page + 1})">›</button>`;

  container.innerHTML = html;
}

function getPageRange(current, total) {
  if (total <= 7) return Array.from({ length: total }, (_, i) => i);
  if (current < 4) return [0,1,2,3,4,'...',total-1];
  if (current > total - 5) return [0,'...',total-5,total-4,total-3,total-2,total-1];
  return [0,'...',current-1,current,current+1,'...',total-1];
}

function changePage(page) {
  state.page = page;
  window.scrollTo({ top: 0, behavior: 'smooth' });
  loadProducts();
}
