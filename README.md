# MAISON — Fashion E-Commerce Application

A full-stack, portfolio-grade fashion e-commerce web application built with a **Java Spring Boot** REST API backend and a **Vanilla HTML/CSS/JS** frontend.

---

## Table of Contents
1. [Project Structure](#project-structure)
2. [Tech Stack](#tech-stack)
3. [Prerequisites](#prerequisites)
4. [Quick Start](#quick-start)
5. [Backend Setup](#backend-setup)
6. [Frontend Setup](#frontend-setup)
7. [API Reference](#api-reference)
8. [Default Credentials](#default-credentials)
9. [Running Tests](#running-tests)
10. [Architecture Decisions](#architecture-decisions)

---

## Project Structure

```
Ecommerce website/
├── backend/                          # Spring Boot REST API
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/com/fashion/store/
│       │   │   ├── FashionStoreApplication.java
│       │   │   ├── config/           # SecurityConfig, CorsConfig
│       │   │   ├── controller/       # REST controllers (Auth, Product, Cart, Order, Payment, Admin)
│       │   │   ├── dto/              # Request/Response DTOs
│       │   │   ├── entity/           # JPA entities (User, Product, Order, CartItem, ...)
│       │   │   ├── exception/        # GlobalExceptionHandler, custom exceptions
│       │   │   ├── repository/       # Spring Data JPA repositories
│       │   │   ├── security/         # JwtUtil, JwtAuthFilter, UserDetailsServiceImpl
│       │   │   └── service/          # Business logic (interfaces + implementations)
│       │   └── resources/
│       │       ├── application.yml   # Main + test profile config
│       │       └── db/migration/     # Flyway SQL migrations
│       └── test/                     # Unit + integration tests
│
└── frontend/                         # Vanilla HTML/CSS/JS SPA
    ├── index.html                    # Home page
    ├── css/
    │   ├── design-system.css         # Global tokens, typography, buttons, forms
    │   ├── global.css                # Navbar, footer, shared components
    │   ├── home.css / shop.css / product.css
    │   ├── cart.css / checkout.css
    │   ├── auth.css / account.css / admin.css
    ├── js/
    │   ├── api.js                    # Fetch wrapper with JWT injection
    │   ├── utils.js                  # Toast, formatting, nav guards, helpers
    │   ├── nav.js                    # Shared navbar/footer behaviour
    │   ├── home.js / shop.js / product.js
    │   ├── cart.js / checkout.js
    │   ├── account.js / admin.js
    └── pages/
        ├── shop.html / product.html
        ├── cart.html / checkout.html
        ├── login.html / register.html
        ├── account.html / admin.html
        └── 404.html
```

---

## Tech Stack

| Layer       | Technology                          |
|-------------|-------------------------------------|
| Frontend    | HTML5, CSS3, Vanilla JavaScript     |
| Backend     | Java 17, Spring Boot 3.2.5          |
| ORM         | Spring Data JPA + Hibernate         |
| Database    | MySQL 8.x (dev), H2 (tests)         |
| Auth        | JWT (JJWT 0.11.5), BCrypt           |
| Migrations  | Flyway                              |
| Build       | Maven 3.9+                         |
| Tests       | JUnit 5, Mockito, MockMvc, H2       |

---

## Prerequisites

- **Java 17+** — `java -version`
- **Maven 3.9+** — `mvn -version`
- **MySQL 8.x** — running locally with root access
- **A modern browser** — Chrome / Edge / Firefox
- **Live Server** (VS Code extension) or `python -m http.server` for the frontend

---

## Quick Start

### 1. Set up the MySQL database

```sql
-- In MySQL shell or Workbench
CREATE DATABASE fashion_store CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Flyway will create all tables and seed data automatically on first start.

### 2. Configure backend (optional — defaults work out of the box)

Edit `backend/src/main/resources/application.yml` if your MySQL credentials differ from the defaults:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/fashion_store?...
    username: root     # ← change if needed
    password: root     # ← change if needed
```

Or pass as environment variables (recommended for production):
```bash
DB_USERNAME=root DB_PASSWORD=yourpassword mvn spring-boot:run
```

### 3. Start the backend

```bash
cd backend
mvn spring-boot:run
```

The API starts on **http://localhost:8080**.  
You should see Flyway apply the migrations, then `Started FashionStoreApplication`.

### 4. Serve the frontend

**Option A — VS Code Live Server** (recommended):
1. Open the `frontend/` folder in VS Code.
2. Right-click `index.html` → **Open with Live Server**.
3. It opens at `http://127.0.0.1:5500/index.html`.

**Option B — Python HTTP server**:
```bash
cd frontend
python -m http.server 5500
# Visit http://localhost:5500
```

**Option C — Direct file open**:
Just open `frontend/index.html` directly in your browser.  
⚠️ Note: Some browsers block `fetch()` on `file://` URLs. Use Option A or B instead.

---

## Backend Setup

### Environment Variables

| Variable     | Default    | Description                      |
|--------------|------------|----------------------------------|
| `DB_HOST`    | `localhost`| MySQL host                       |
| `DB_PORT`    | `3306`     | MySQL port                       |
| `DB_NAME`    | `fashion_store` | Database name               |
| `DB_USERNAME`| `root`     | MySQL username                   |
| `DB_PASSWORD`| `root`     | MySQL password                   |
| `JWT_SECRET` | built-in   | HS256 signing secret (64 chars)  |

### Database Migrations (Flyway)

- `V1__init.sql` — Creates all tables: `users`, `categories`, `products`, `cart_items`, `orders`, `order_items`, `payments`, `reviews`
- `V2__seed.sql` — Seeds 4 categories and 12+ products, plus an admin user

Flyway runs automatically on startup. To re-run migrations from scratch:
```sql
DROP DATABASE fashion_store;
CREATE DATABASE fashion_store;
```

---

## Frontend Setup

The frontend is pure static files — no build step required.

All pages share these scripts (loaded in order):
1. `js/api.js` — centralised `fetch()` wrapper with JWT auto-injection
2. `js/utils.js` — toast notifications, formatters, nav guards
3. `js/nav.js` — navbar state, hamburger, user menu, logout

The API base URL is configured at the top of `js/api.js`:
```js
const API_BASE = 'http://localhost:8080/api';
```
Change this if you deploy the backend to a different host.

---

## API Reference

All API endpoints are prefixed `/api`. Protected endpoints require `Authorization: Bearer <token>`.

### Auth
| Method | Endpoint              | Auth | Description              |
|--------|-----------------------|------|--------------------------|
| POST   | `/api/auth/register`  | No   | Create new user account  |
| POST   | `/api/auth/login`     | No   | Login, returns JWT token |
| GET    | `/api/auth/me`        | Yes  | Get current user profile |

### Products
| Method | Endpoint              | Auth  | Description                      |
|--------|-----------------------|-------|----------------------------------|
| GET    | `/api/products`       | No    | List with filters/pagination     |
| GET    | `/api/products/{id}`  | No    | Get product by ID                |
| GET    | `/api/categories`     | No    | List all categories              |
| POST   | `/api/products`       | Admin | Create product                   |
| PUT    | `/api/products/{id}`  | Admin | Update product                   |
| DELETE | `/api/products/{id}`  | Admin | Delete product                   |

**Product query params**: `categoryId`, `minPrice`, `maxPrice`, `keyword`, `page`, `size`, `sortBy`, `sortDir`

### Cart
| Method | Endpoint          | Auth | Description              |
|--------|-------------------|------|--------------------------|
| GET    | `/api/cart`       | Yes  | Get current user's cart  |
| POST   | `/api/cart`       | Yes  | Add item to cart         |
| PUT    | `/api/cart/{id}`  | Yes  | Update item quantity      |
| DELETE | `/api/cart/{id}`  | Yes  | Remove item from cart    |

### Orders
| Method | Endpoint                  | Auth | Description              |
|--------|---------------------------|------|--------------------------|
| POST   | `/api/orders/checkout`    | Yes  | Place order from cart    |
| GET    | `/api/orders`             | Yes  | Get user's order history |
| GET    | `/api/orders/{id}`        | Yes  | Get single order         |

### Admin
| Method | Endpoint                           | Auth  | Description            |
|--------|------------------------------------|-------|------------------------|
| GET    | `/api/admin/orders`                | Admin | Paginated all orders   |
| PUT    | `/api/admin/orders/{id}/status`    | Admin | Update order status    |
| GET    | `/api/admin/stats`                 | Admin | Aggregate dashboard stats |

---

## Default Credentials

After running `V2__seed.sql`, these accounts are ready to use:

| Role  | Email                  | Password     |
|-------|------------------------|--------------|
| Admin | `admin@maison.com`     | `Admin123!`  |
| User  | `jane@example.com`     | `User1234!`  |

> ⚠️ The seeded admin password is BCrypt-hashed in the SQL. To change it, hash a new password with BCrypt strength 12 and update the `password_hash` value in `V2__seed.sql`.

---

## Running Tests

### Unit tests only (no DB required)

```bash
cd backend
mvn test -Dtest="AuthServiceTest,ProductServiceTest,CartServiceTest,OrderServiceTest"
```

### All tests (includes integration test with H2)

```bash
cd backend
mvn test
```

The integration test (`AuthControllerIntegrationTest`) uses the `test` Spring profile which activates the H2 in-memory database and disables Flyway — no MySQL needed.

### Test coverage overview

| Test Class                       | Type        | Coverage                                      |
|----------------------------------|-------------|-----------------------------------------------|
| `AuthServiceTest`                | Unit        | Register, login, duplicate email, get profile |
| `ProductServiceTest`             | Unit        | CRUD, pagination, not-found                   |
| `CartServiceTest`                | Unit        | Add, remove, stock check                      |
| `OrderServiceTest`               | Unit        | Checkout, stock deduction, status update      |
| `AuthControllerIntegrationTest`  | Integration | Full HTTP request/response with MockMvc + H2  |

---

## Architecture Decisions

### 1. Stateless JWT Authentication
- Tokens are issued at login/register and sent via `Authorization: Bearer` on every subsequent request.
- The `JwtAuthFilter` validates tokens on each request — no session state is stored server-side.
- Tokens expire after 24 hours (configurable via `app.jwt.expiration-ms`).

### 2. Mock Payment Service
- `PaymentService` is an interface; `MockPaymentServiceImpl` always returns `SUCCESS`.
- To integrate a real gateway (e.g. Stripe), implement `PaymentService` and swap the `@Primary` bean annotation — no controller changes needed.

### 3. Flyway Database Migrations
- All schema changes are versioned SQL files under `db/migration/`.
- Enables reproducible local setup and safe production rollouts.

### 4. Cascaded Stock Management
- `CartServiceImpl` validates stock on `addToCart`.
- `OrderServiceImpl` validates stock again at checkout and atomically decrements it — prevents double-sells under concurrent load.

### 5. Frontend: No Framework
- Pure HTML/CSS/JS with a shared design system (`design-system.css`) using CSS custom properties.
- `js/api.js` wraps all `fetch()` calls with JWT injection and error normalisation.
- `js/utils.js` provides toast notifications, formatters, and navigation guards.
- No bundler or build step required — open `index.html` and go.

### 6. CORS Configuration
- `CorsConfig.java` allows the frontend origins `http://localhost:5500` and `http://127.0.0.1:5500`.
- Add additional origins to `app.cors.allowed-origins` in `application.yml` as needed.

---

## Known Limitations & Future Improvements

- **Email sending**: No email confirmation on register (would require an SMTP service like SendGrid).
- **Image upload**: Products use image URLs. A real implementation would use S3 / Cloudinary.
- **Payment**: Mock only — swap with Stripe by implementing the `PaymentService` interface.
- **Review system**: The `Review` entity and repository are scaffolded but the endpoint is not exposed (left as an exercise).
- **Pagination on frontend**: The shop page supports full pagination; the admin orders table loads up to 100 orders — pagination UI for admin is a future improvement.
