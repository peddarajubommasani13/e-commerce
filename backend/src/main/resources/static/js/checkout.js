/* ============================================
   CHECKOUT PAGE — checkout.js
   ============================================ */

document.addEventListener('DOMContentLoaded', async () => {
  if (!requireAuth()) return;
  await loadOrderSummary();
});

async function loadOrderSummary() {
  const loader = document.getElementById('checkout-loader');
  const form = document.getElementById('checkout-form');
  const itemsContainer = document.getElementById('checkout-line-items');

  try {
    const cart = await api.getCart();

    if (!cart || !cart.items || cart.items.length === 0) {
      toastError('Your cart is empty. Redirecting back to cart.');
      setTimeout(() => window.location.href = 'cart.html', 1500);
      return;
    }

    // Populate checkout item previews
    itemsContainer.innerHTML = cart.items.map(item => `
      <div class="checkout-line-item">
        <div class="checkout-line-item-desc">
          <span class="checkout-line-item-name">${escapeHtml(item.productName)}</span>
          <span class="checkout-line-item-qty">
            Qty: ${item.quantity} ${item.size ? `• Size: ${item.size}` : ''} ${item.color ? `• Color: ${item.color}` : ''}
          </span>
        </div>
        <span style="font-weight: 600;">${formatPrice(item.lineTotal)}</span>
      </div>
    `).join('');

    // Summary totals
    document.getElementById('checkout-subtotal').textContent = formatPrice(cart.subtotal);
    document.getElementById('checkout-total').textContent = formatPrice(cart.total);

    const discountRow = document.getElementById('checkout-discount-row');
    if (cart.discount && Number(cart.discount) > 0) {
      discountRow.style.display = 'flex';
      document.getElementById('checkout-discount').textContent = `-${formatPrice(cart.discount)}`;
    } else {
      discountRow.style.display = 'none';
    }

    // Show content
    loader.style.display = 'none';
    form.style.display = 'grid';

  } catch (err) {
    loader.innerHTML = `
      <div class="empty-state">
        <div class="empty-state-icon">⚠️</div>
        <h3>Failed to prepare checkout</h3>
        <p>${escapeHtml(err.message)}</p>
      </div>
    `;
  }
}

async function handlePlaceOrder(event) {
  event.preventDefault();
  const btn = document.getElementById('place-order-btn');
  setLoading(btn, true);

  const shippingAddress = [
    document.getElementById('ship-name').value.trim(),
    document.getElementById('ship-address').value.trim(),
    document.getElementById('ship-city').value.trim(),
    document.getElementById('ship-state').value.trim(),
    document.getElementById('ship-zip').value.trim(),
    document.getElementById('ship-country').value.trim()
  ].join(', ');

  const cardName = document.getElementById('card-name').value.trim();
  const cardNumber = document.getElementById('card-number').value.replace(/\s+/g, '');
  const cardNumberLast4 = cardNumber.slice(-4);

  const payload = {
    shippingAddress,
    paymentMethod: 'MOCK_CREDIT_CARD',
    cardName,
    cardNumberLast4
  };

  try {
    const order = await api.checkout(payload);
    
    toastSuccess('Order placed successfully!', 'Completed ✓');
    updateCartBadge(0);
    
    // Redirect to orders screen
    setTimeout(() => {
      window.location.href = `account.html?orderId=${order.id}`;
    }, 1500);

  } catch (err) {
    toastError(err.message || 'Checkout failed. Please check details.');
    setLoading(btn, false);
  }
}

// Global hook
window.handlePlaceOrder = handlePlaceOrder;
