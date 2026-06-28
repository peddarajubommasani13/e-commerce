/* ============================================
   PRODUCT DETAIL PAGE — product.js
   ============================================ */

let selectedSize = '';
let selectedColor = '';
let currentQty = 1;
let currentProduct = null;

document.addEventListener('DOMContentLoaded', async () => {
  const productId = getUrlParam('id');
  if (!productId) {
    window.location.href = 'shop.html';
    return;
  }

  await loadProductDetails(productId);
  setupQuantitySelector();
  setupAddToCart();
});

async function loadProductDetails(id) {
  const loader = document.getElementById('product-detail-loader');
  const content = document.getElementById('product-detail-content');

  try {
    const product = await api.getProduct(id);
    currentProduct = product;

    // Fill details
    document.getElementById('product-category').textContent = product.category?.name || 'Fashion';
    document.getElementById('product-name').textContent = product.name;
    document.getElementById('product-description').textContent = product.description || 'No description available.';
    document.getElementById('product-stars').innerHTML = renderStars(product.rating);
    document.getElementById('product-review-count').textContent = `(${product.reviewCount} reviews)`;

    // Price
    document.getElementById('product-price-container').innerHTML = renderPriceHtml(product.price, product.discountPrice);

    // Images
    const mainImg = document.getElementById('main-product-img');
    const thumbnails = document.getElementById('thumbnail-list');

    if (product.imageUrls && product.imageUrls.length > 0) {
      mainImg.src = product.imageUrls[0];
      mainImg.alt = product.name;

      thumbnails.innerHTML = product.imageUrls.map((url, i) => `
        <div class="thumbnail-item ${i === 0 ? 'active' : ''}" onclick="setMainImage('${url}', this)">
          <img src="${url}" alt="Thumbnail ${i}">
        </div>
      `).join('');
    } else {
      mainImg.src = 'https://images.unsplash.com/photo-1445205170230-053b83016050?w=600&q=80';
      thumbnails.innerHTML = '';
    }

    // Sizes
    const sizeOptions = document.getElementById('size-options');
    if (product.sizes && product.sizes.length > 0) {
      document.getElementById('size-selector-container').style.display = 'flex';
      sizeOptions.innerHTML = product.sizes.map(size => `
        <button class="variant-chip" onclick="selectSize('${size}', this)">${size}</button>
      `).join('');
    } else {
      document.getElementById('size-selector-container').style.display = 'none';
    }

    // Colors
    const colorOptions = document.getElementById('color-options');
    if (product.colors && product.colors.length > 0) {
      document.getElementById('color-selector-container').style.display = 'flex';
      colorOptions.innerHTML = product.colors.map(color => `
        <button class="variant-chip" onclick="selectColor('${color}', this)">${color}</button>
      `).join('');
    } else {
      document.getElementById('color-selector-container').style.display = 'none';
    }

    // Stock status
    const stockEl = document.getElementById('product-stock');
    if (product.stockQuantity > 0) {
      stockEl.textContent = `In Stock (${product.stockQuantity} available)`;
      stockEl.style.color = 'var(--color-success)';
    } else {
      stockEl.textContent = 'Out of Stock';
      stockEl.style.color = 'var(--color-error)';
      document.getElementById('add-to-cart-btn').disabled = true;
    }

    // Load related products
    loadRelatedProducts(product.category?.id, product.id);

    // Show content
    loader.style.display = 'none';
    content.style.display = 'grid';

  } catch (err) {
    loader.innerHTML = `
      <div class="empty-state">
        <div class="empty-state-icon">⚠️</div>
        <h3>Failed to Load Product</h3>
        <p>${escapeHtml(err.message)}</p>
        <a href="shop.html" class="btn btn-primary" style="margin-top:20px">Back to Shop</a>
      </div>
    `;
  }
}

function setMainImage(url, thumbEl) {
  document.getElementById('main-product-img').src = url;
  document.querySelectorAll('.thumbnail-item').forEach(el => el.classList.remove('active'));
  thumbEl.classList.add('active');
}

function selectSize(size, chipEl) {
  selectedSize = size;
  chipEl.parentNode.querySelectorAll('.variant-chip').forEach(el => el.classList.remove('selected'));
  chipEl.classList.add('selected');
}

function selectColor(color, chipEl) {
  selectedColor = color;
  chipEl.parentNode.querySelectorAll('.variant-chip').forEach(el => el.classList.remove('selected'));
  chipEl.classList.add('selected');
}

function setupQuantitySelector() {
  const decBtn = document.getElementById('qty-dec');
  const incBtn = document.getElementById('qty-inc');
  const qtyInput = document.getElementById('qty-input');

  decBtn.addEventListener('click', () => {
    if (currentQty > 1) {
      currentQty--;
      qtyInput.value = currentQty;
    }
  });

  incBtn.addEventListener('click', () => {
    if (currentProduct && currentQty < currentProduct.stockQuantity) {
      currentQty++;
      qtyInput.value = currentQty;
    } else {
      toastInfo('Cannot add more than available stock.');
    }
  });
}

function setupAddToCart() {
  const btn = document.getElementById('add-to-cart-btn');
  btn.addEventListener('click', async () => {
    if (!isLoggedIn()) {
      toastInfo('Please sign in to add items to your cart.');
      setTimeout(() => window.location.href = 'login.html', 1500);
      return;
    }

    // Validation
    if (currentProduct.sizes && currentProduct.sizes.length > 0 && !selectedSize) {
      toastError('Please select a size.');
      return;
    }
    if (currentProduct.colors && currentProduct.colors.length > 0 && !selectedColor) {
      toastError('Please select a color.');
      return;
    }

    setLoading(btn, true);

    try {
      await api.addToCart({
        productId: currentProduct.id,
        quantity: currentQty,
        size: selectedSize || null,
        color: selectedColor || null
      });

      toastSuccess(`${currentProduct.name} added to cart!`, 'Added ✓');
      refreshCartBadge();
    } catch (err) {
      toastError(err.message || 'Failed to add item to cart.');
    } finally {
      setLoading(btn, false);
    }
  });
}

async function loadRelatedProducts(categoryId, currentProductId) {
  const grid = document.getElementById('related-products-grid');
  if (!grid) return;

  grid.innerHTML = skeletonRow(4);

  try {
    const result = await api.getProducts({ categoryId, page: 0, size: 5 });
    const related = (result.content || [])
      .filter(p => p.id !== currentProductId)
      .slice(0, 4);

    if (related.length === 0) {
      grid.innerHTML = '<p class="text-muted text-center" style="grid-column:1/-1;">No related products found.</p>';
      return;
    }

    grid.innerHTML = related.map(p => renderProductCard(p)).join('');

    // Quick add and detail page events
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

  } catch (err) {
    grid.innerHTML = '<p class="text-muted text-center" style="grid-column:1/-1;">Unable to load related products.</p>';
  }
}

// Make globally accessible
window.setMainImage = setMainImage;
window.selectSize = selectSize;
window.selectColor = selectColor;
