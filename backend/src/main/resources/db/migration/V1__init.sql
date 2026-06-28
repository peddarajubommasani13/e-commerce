-- ============================================================
-- Fashion Store - Database Schema
-- ============================================================

CREATE TABLE IF NOT EXISTS users (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(255)        NOT NULL,
    email        VARCHAR(255)        NOT NULL UNIQUE,
    password_hash VARCHAR(255)       NOT NULL,
    role         VARCHAR(20)         NOT NULL DEFAULT 'USER',
    created_at   TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS categories (
    id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    name  VARCHAR(100) NOT NULL,
    slug  VARCHAR(100) NOT NULL UNIQUE,
    INDEX idx_categories_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS products (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(255)    NOT NULL,
    description     TEXT,
    price           DECIMAL(10,2)   NOT NULL,
    discount_price  DECIMAL(10,2),
    category_id     BIGINT          NOT NULL,
    stock_quantity  INT             NOT NULL DEFAULT 0,
    image_urls      TEXT            COMMENT 'Comma-separated image URLs',
    sizes           VARCHAR(255)    COMMENT 'Comma-separated available sizes',
    colors          VARCHAR(255)    COMMENT 'Comma-separated available colors',
    rating          DOUBLE          NOT NULL DEFAULT 0.0,
    review_count    INT             NOT NULL DEFAULT 0,
    created_at      TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories(id),
    INDEX idx_products_category (category_id),
    INDEX idx_products_price (price),
    INDEX idx_products_created (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS cart_items (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT      NOT NULL,
    product_id  BIGINT      NOT NULL,
    quantity    INT         NOT NULL DEFAULT 1,
    size        VARCHAR(50),
    color       VARCHAR(50),
    CONSTRAINT fk_cart_user    FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE CASCADE,
    CONSTRAINT fk_cart_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    INDEX idx_cart_user (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS orders (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id          BIGINT          NOT NULL,
    total_amount     DECIMAL(10,2)   NOT NULL,
    status           VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    shipping_address TEXT,
    payment_status   VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    created_at       TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_orders_user (user_id),
    INDEX idx_orders_status (status),
    INDEX idx_orders_created (created_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS order_items (
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id           BIGINT          NOT NULL,
    product_id         BIGINT          NOT NULL,
    quantity           INT             NOT NULL,
    price_at_purchase  DECIMAL(10,2)   NOT NULL,
    size               VARCHAR(50),
    color              VARCHAR(50),
    CONSTRAINT fk_order_items_order   FOREIGN KEY (order_id)   REFERENCES orders(id)   ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products(id),
    INDEX idx_order_items_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS payments (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id       BIGINT          NOT NULL,
    provider       VARCHAR(100)    NOT NULL DEFAULT 'MOCK_GATEWAY',
    transaction_id VARCHAR(255),
    status         VARCHAR(50)     NOT NULL DEFAULT 'PENDING',
    amount         DECIMAL(10,2)   NOT NULL,
    created_at     TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders(id),
    INDEX idx_payments_order (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS reviews (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_id  BIGINT      NOT NULL,
    user_id     BIGINT      NOT NULL,
    rating      INT         NOT NULL CHECK (rating BETWEEN 1 AND 5),
    comment     TEXT,
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reviews_product FOREIGN KEY (product_id) REFERENCES products(id) ON DELETE CASCADE,
    CONSTRAINT fk_reviews_user    FOREIGN KEY (user_id)    REFERENCES users(id)    ON DELETE CASCADE,
    UNIQUE KEY uk_review_user_product (product_id, user_id),
    INDEX idx_reviews_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
