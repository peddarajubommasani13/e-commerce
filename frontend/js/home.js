/* ============================================
   HOME PAGE — home.js
   ============================================ */

const CATEGORY_IMAGES = {
  'women':       'https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=600&q=80',
  'men':         'https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=600&q=80',
  'accessories': 'https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=600&q=80',
  'sale':        'https://images.unsplash.com/photo-1483985988355-763728e1935b?w=600&q=80',
};

document.addEventListener('DOMContentLoaded', async () => {
  loadCategories();
  loadFeaturedProducts();
});

async function loadCategories() {
  const grid = document.getElementById('category-grid');
  if (!grid) return;

  try {
    const categories = await api.getCategories();
    grid.innerHTML = categories.map(cat => `
      <a href="pages/shop.html?categoryId=${cat.id}" class="category-tile">
        <img
          class="category-tile-img"
          src="${CATEGORY_IMAGES[cat.slug] || 'https://images.unsplash.com/photo-1445205170230-053b83016050?w=600&q=80'}"
          alt="${escapeHtml(cat.name)}"
          loading="lazy"
        />
        <div class="category-tile-overlay">
          <div class="category-tile-name">${escapeHtml(cat.name)}</div>
          <span class="category-tile-link">Shop Now</span>
        </div>
      </a>
    `).join('');
  } catch (err) {
    grid.innerHTML = '<p class="text-muted text-center" style="grid-column:1/-1;padding:40px">Unable to load categories.</p>';
  }
}

async function loadFeaturedProducts() {
  const grid = document.getElementById('featured-grid');
  if (!grid) return;

  // Show skeletons
  grid.innerHTML = skeletonRow(4);

  try {
    const result = await api.getProducts({ page: 0, size: 8, sortBy: 'createdAt', sortDir: 'desc' });
    const products = result.content || [];

    if (!products.length) {
      grid.innerHTML = '<div class="empty-state" style="grid-column:1/-1"><div class="empty-state-icon">🛍</div><h3>Coming Soon</h3><p>Products are on their way!</p></div>';
      return;
    }

    grid.innerHTML = products.map(p => renderProductCard(p)).join('');

    // Attach add-to-cart handlers
    grid.querySelectorAll('[data-add-to-cart]').forEach(btn => {
      btn.addEventListener('click', (e) => {
        e.preventDefault();
        e.stopPropagation();
        handleQuickAddToCart(btn.dataset.productId, btn.dataset.name);
      });
    });

    // Card click → product detail
    grid.querySelectorAll('.product-card').forEach(card => {
      card.addEventListener('click', (e) => {
        if (!e.target.closest('[data-add-to-cart]')) {
          window.location.href = `pages/product.html?id=${card.dataset.productId}`;
        }
      });
    });
  } catch (err) {
    grid.innerHTML = '<p class="text-muted text-center" style="grid-column:1/-1;padding:40px">Unable to load products. Make sure the backend is running.</p>';
  }
}


// Note: renderProductCard and handleQuickAddToCart are defined in utils.js

