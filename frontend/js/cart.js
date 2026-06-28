/* ============================================
   CART PAGE — cart.js
   ============================================ */

document.addEventListener('DOMContentLoaded', async () => {
  if (!requireAuth()) return;
  await loadCart();
});

async function loadCart() {
  const loader = document.getElementById('cart-loader');
  const emptyState = document.getElementById('cart-empty-state');
  const layout = document.getElementById('cart-layout');
  const list = document.getElementById('cart-items-list');

  try {
    const cart = await api.getCart();

    if (!cart || !cart.items || cart.items.length === 0) {
      loader.style.display = 'none';
      layout.style.display = 'none';
      emptyState.style.display = 'block';
      updateCartBadge(0);
      return;
    }

    // Populate lines
    list.innerHTML = cart.items.map(item => `
      <div class="cart-item-row" data-item-id="${item.id}">
        <div class="cart-item-product">
          <div class="cart-item-img-wrap">
            <img src="${item.productImage || 'https://images.unsplash.com/photo-1445205170230-053b83016050?w=600&q=80'}" alt="${escapeHtml(item.productName)}">
          </div>
          <div class="cart-item-details">
            <h3 class="cart-item-name">${escapeHtml(item.productName)}</h3>
            <div class="cart-item-variants">
              ${item.size ? `Size: ${item.size}` : ''} 
              ${item.color ? `&nbsp;•&nbsp; Color: ${item.color}` : ''}
            </div>
            <button class="cart-item-remove-btn" onclick="removeLineItem(${item.id})">Remove</button>
          </div>
        </div>

        <div>
          ${item.discountPrice && Number(item.discountPrice) < Number(item.price)
            ? `<div class="price price-discount">${formatPrice(item.discountPrice)}</div>
               <div class="price-original" style="font-size:var(--text-xs);">${formatPrice(item.price)}</div>`
            : `<div class="price">${formatPrice(item.price)}</div>`
          }
        </div>

        <div>
          <div class="qty-selector" style="height: 38px;">
            <button class="qty-btn" onclick="updateLineQty(${item.id}, ${item.quantity - 1})">—</button>
            <input type="number" value="${item.quantity}" readonly style="width:40px;">
            <button class="qty-btn" onclick="updateLineQty(${item.id}, ${item.quantity + 1})">+</button>
          </div>
        </div>

        <div class="price" style="font-weight:700;">
          ${formatPrice(item.lineTotal)}
        </div>
      </div>
    `).join('');

    // Summary
    document.getElementById('summary-subtotal').textContent = formatPrice(cart.subtotal);
    document.getElementById('summary-total').textContent = formatPrice(cart.total);

    const discountRow = document.getElementById('summary-discount-row');
    if (cart.discount && Number(cart.discount) > 0) {
      discountRow.style.display = 'flex';
      document.getElementById('summary-discount').textContent = `-${formatPrice(cart.discount)}`;
    } else {
      discountRow.style.display = 'none';
    }

    updateCartBadge(cart.itemCount);

    // Setup checkout btn
    document.getElementById('checkout-btn').onclick = () => {
      window.location.href = 'checkout.html';
    };

    // Show content
    loader.style.display = 'none';
    emptyState.style.display = 'none';
    layout.style.display = 'grid';

  } catch (err) {
    loader.innerHTML = `
      <div class="empty-state">
        <div class="empty-state-icon">⚠️</div>
        <h3>Failed to load cart</h3>
        <p>${escapeHtml(err.message)}</p>
      </div>
    `;
  }
}

async function updateLineQty(itemId, newQty) {
  if (newQty < 1) {
    await removeLineItem(itemId);
    return;
  }

  try {
    await api.updateCartItem(itemId, { quantity: newQty });
    await loadCart();
  } catch (err) {
    toastError(err.message || 'Could not update quantity.');
  }
}

async function removeLineItem(itemId) {
  try {
    await api.removeCartItem(itemId);
    toastSuccess('Item removed from cart.');
    await loadCart();
  } catch (err) {
    toastError(err.message || 'Could not remove item.');
  }
}

// Bind to window for inline onclicks
window.updateLineQty = updateLineQty;
window.removeLineItem = removeLineItem;
