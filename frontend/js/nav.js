/* ============================================
   FASHION STORE — nav.js
   Shared navbar/footer logic:
   - Active link highlighting
   - Scroll shadow
   - Hamburger toggle
   - User menu dropdown
   - Cart badge sync
   ============================================ */

document.addEventListener('DOMContentLoaded', function () {
  initNavbar();
  initFooter();
});

function initNavbar() {
  // Scroll shadow
  const navbar = document.querySelector('.navbar');
  if (navbar) {
    window.addEventListener('scroll', () => {
      navbar.classList.toggle('scrolled', window.scrollY > 20);
    }, { passive: true });
  }

  // Active link highlighting
  highlightActiveNavLink();

  // Hamburger / mobile drawer
  initMobileNav();

  // User menu
  initUserMenu();

  // Cart badge
  refreshCartBadge();
}

function highlightActiveNavLink() {
  const path = window.location.pathname;
  document.querySelectorAll('.nav-link, .nav-mobile-link').forEach(link => {
    const href = link.getAttribute('href') || '';
    // Match exact or partial path
    const isActive = href && (
      path === href ||
      path.endsWith(href) ||
      (href !== '/index.html' && href !== '/' && path.includes(href.replace('.html', '')))
    );
    link.classList.toggle('active', isActive);
  });
}

function initMobileNav() {
  const hamburger   = document.getElementById('nav-hamburger');
  const drawer      = document.getElementById('nav-mobile-drawer');
  const overlay     = document.getElementById('nav-mobile-overlay');
  const closeBtn    = document.getElementById('nav-drawer-close');

  if (!hamburger) return;

  function openDrawer() {
    hamburger.classList.add('open');
    drawer?.classList.add('open');
    overlay?.classList.add('open');
    document.body.style.overflow = 'hidden';
  }

  function closeDrawer() {
    hamburger.classList.remove('open');
    drawer?.classList.remove('open');
    overlay?.classList.remove('open');
    document.body.style.overflow = '';
  }

  hamburger.addEventListener('click', openDrawer);
  closeBtn?.addEventListener('click', closeDrawer);
  overlay?.addEventListener('click', closeDrawer);
}

function initUserMenu() {
  const userBtn  = document.getElementById('nav-user-btn');
  const dropdown = document.getElementById('nav-user-dropdown');
  const logoutBtn = document.getElementById('nav-logout-btn');
  const loginLink = document.getElementById('nav-login-link');
  const registerLink = document.getElementById('nav-register-link');
  const adminLink = document.getElementById('nav-admin-link');
  const userNameEl = document.getElementById('nav-user-name');
  const avatarEl   = document.getElementById('nav-avatar');

  const user = getAuthUser();

  if (user) {
    // Show user menu, hide login/register
    if (userBtn) userBtn.style.display = 'flex';
    if (loginLink) loginLink.style.display = 'none';
    if (registerLink) registerLink.style.display = 'none';
    if (userNameEl) userNameEl.textContent = user.name?.split(' ')[0] || 'Account';
    if (avatarEl)   avatarEl.textContent   = (user.name || 'U')[0].toUpperCase();

    // Admin link visibility
    if (adminLink) {
      adminLink.style.display = user.role === 'ADMIN' ? 'flex' : 'none';
    }
  } else {
    // Show login/register, hide user menu
    if (userBtn)     userBtn.style.display = 'none';
    if (adminLink)   adminLink.style.display = 'none';
    if (loginLink)   loginLink.style.display = '';
    if (registerLink) registerLink.style.display = '';
  }

  // Dropdown toggle
  if (userBtn && dropdown) {
    userBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      dropdown.classList.toggle('open');
    });
    document.addEventListener('click', () => dropdown.classList.remove('open'));
  }

  // Logout
  if (logoutBtn) {
    logoutBtn.addEventListener('click', () => {
      setAuthToken(null);
      setAuthUser(null);
      toastSuccess('You have been logged out.');
      setTimeout(() => window.location.href = resolveRoot('index.html'), 600);
    });
  }
}

function initFooter() {
  const footer = document.querySelector('.footer');
  if (!footer) return;

  const currentYear = new Date().getFullYear();

  footer.innerHTML = `
    <div class="footer-grid">
      <div class="footer-brand">
        <div class="footer-brand-name">MAISON</div>
        <p class="footer-brand-desc">Luxury fashion for the modern individual. We believe great style is about finding what makes you feel uniquely yourself.</p>
        <div class="footer-social">
          <a href="#" class="footer-social-btn">f</a>
          <a href="#" class="footer-social-btn">in</a>
          <a href="#" class="footer-social-btn">tw</a>
          <a href="#" class="footer-social-btn">ig</a>
        </div>
      </div>
      <div>
        <div class="footer-heading">Shop</div>
        <div class="footer-links">
          <a href="${resolveRoot('pages/shop.html?categoryId=1')}" class="footer-link">Women</a>
          <a href="${resolveRoot('pages/shop.html?categoryId=2')}" class="footer-link">Men</a>
          <a href="${resolveRoot('pages/shop.html?categoryId=3')}" class="footer-link">Accessories</a>
          <a href="${resolveRoot('pages/shop.html?categoryId=4')}" class="footer-link">Sale</a>
          <a href="${resolveRoot('pages/shop.html')}" class="footer-link">New Arrivals</a>
        </div>
      </div>
      <div>
        <div class="footer-heading">Account</div>
        <div class="footer-links">
          <a href="${resolveRoot('pages/login.html')}" class="footer-link">Sign In</a>
          <a href="${resolveRoot('pages/register.html')}" class="footer-link">Create Account</a>
          <a href="${resolveRoot('pages/account.html')}" class="footer-link">My Orders</a>
          <a href="${resolveRoot('pages/cart.html')}" class="footer-link">Cart</a>
        </div>
      </div>
      <div>
        <div class="footer-heading">Help</div>
        <div class="footer-links">
          <a href="${resolveRoot('pages/help.html#sizing')}" class="footer-link">Sizing Guide</a>
          <a href="${resolveRoot('pages/help.html#shipping')}" class="footer-link">Shipping Info</a>
          <a href="${resolveRoot('pages/help.html#returns')}" class="footer-link">Returns Policy</a>
          <a href="${resolveRoot('pages/help.html#contact')}" class="footer-link">Contact Us</a>
        </div>
      </div>
    </div>
    <div class="footer-bottom">
      <span>© ${currentYear} MAISON. All rights reserved.</span>
      <div class="footer-payment-icons">
        <span class="payment-icon">VISA</span>
        <span class="payment-icon">MC</span>
        <span class="payment-icon">AMEX</span>
        <span class="payment-icon">PayPal</span>
      </div>
    </div>
  `;
}
