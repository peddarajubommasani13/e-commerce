-- ============================================================
-- Fashion Store - Seed Data
-- ============================================================

-- Admin user (password: Admin@123)
INSERT INTO users (name, email, password_hash, role) VALUES
('Admin User', 'admin@fashion.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh7.', 'ADMIN'),
('Jane Doe', 'jane@example.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lh7.', 'USER');

-- Categories
INSERT INTO categories (name, slug) VALUES
('Women', 'women'),
('Men', 'men'),
('Accessories', 'accessories'),
('Sale', 'sale');

-- Women's products
INSERT INTO products (name, description, price, discount_price, category_id, stock_quantity, image_urls, sizes, colors, rating, review_count) VALUES
('Silk Evening Gown',
 'Luxurious silk evening gown with a flowing silhouette and subtle sheen. Perfect for formal occasions and galas. Features a delicate back zip closure and a graceful train.',
 289.99, 199.99, 1, 15,
 'https://images.unsplash.com/photo-1566174053879-31528523f8ae?w=600&q=80,https://images.unsplash.com/photo-1572804013427-4d7ca7268217?w=600&q=80',
 'XS,S,M,L,XL', 'Champagne,Black,Burgundy', 4.8, 124),

('Linen Blazer',
 'Tailored linen blazer with a relaxed fit. Breathable fabric makes it ideal for warm weather while keeping a polished look. Single-button closure, notch lapels.',
 149.99, NULL, 1, 30,
 'https://images.unsplash.com/photo-1594938298603-c8148c4a8ba0?w=600&q=80,https://images.unsplash.com/photo-1551488831-00ddcb6c6bd3?w=600&q=80',
 'XS,S,M,L,XL,XXL', 'White,Beige,Sage Green,Navy', 4.5, 89),

('High-Waist Trousers',
 'Elegant high-waist wide-leg trousers crafted from premium crepe fabric. Flattering cut with a clean front and subtle pleat detail. Pairs with any top.',
 119.99, 89.99, 1, 45,
 'https://images.unsplash.com/photo-1594938298603-c8148c4a8ba0?w=600&q=80,https://images.unsplash.com/photo-1485518882345-15568b007407?w=600&q=80',
 'XS,S,M,L,XL', 'Black,Camel,Grey,White', 4.6, 201),

('Cashmere Turtleneck',
 'Pure cashmere turtleneck in a classic slim fit. Exceptionally soft and warm with ribbed cuffs and hem. A wardrobe essential for colder months.',
 199.99, 159.99, 1, 20,
 'https://images.unsplash.com/photo-1434389677669-e08b4cac3105?w=600&q=80,https://images.unsplash.com/photo-1576566588028-4147f3842f27?w=600&q=80',
 'XS,S,M,L,XL', 'Ivory,Camel,Black,Grey Marl', 4.9, 315),

('Floral Wrap Dress',
 'Romantic floral wrap dress with a V-neckline and adjustable belt tie. Lightweight viscose fabric with a flattering wrap silhouette suitable for any occasion.',
 89.99, NULL, 1, 60,
 'https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=600&q=80,https://images.unsplash.com/photo-1496747611176-843222e1e57c?w=600&q=80',
 'XS,S,M,L,XL', 'Blue Floral,Pink Floral,Green Floral', 4.4, 178),

-- Men''s products
('Classic Oxford Shirt',
 'Timeless Oxford shirt crafted from 100% cotton. Features a button-down collar, chest pocket, and curved hem for versatile styling. Machine washable.',
 79.99, NULL, 2, 80,
 'https://images.unsplash.com/photo-1602810318383-e386cc2a3ccf?w=600&q=80,https://images.unsplash.com/photo-1620799140408-edc6dcb6d633?w=600&q=80',
 'S,M,L,XL,XXL', 'White,Light Blue,Pink,Grey', 4.7, 432),

('Slim-Fit Chinos',
 'Modern slim-fit chinos in a premium stretch-cotton blend. Features a flat front, five-pocket design, and clean finish. Smart-casual versatility at its best.',
 99.99, 79.99, 2, 55,
 'https://images.unsplash.com/photo-1624378439575-d8705ad7ae80?w=600&q=80,https://images.unsplash.com/photo-1473966968600-fa801b869a1a?w=600&q=80',
 'W28,W30,W32,W34,W36,W38', 'Khaki,Navy,Olive,Stone', 4.5, 267),

('Merino Wool Sweater',
 'Extra-fine Merino wool crew-neck sweater with a relaxed fit. Exceptionally soft with natural temperature regulation. Ribbed neck, cuffs and hem.',
 159.99, 119.99, 2, 35,
 'https://images.unsplash.com/photo-1434389677669-e08b4cac3105?w=600&q=80,https://images.unsplash.com/photo-1591047139829-d91aecb6caea?w=600&q=80',
 'S,M,L,XL,XXL', 'Navy,Forest Green,Burgundy,Oatmeal', 4.8, 156),

('Tailored Suit',
 'Two-piece tailored suit in a structured wool-blend fabric. Slim-fit jacket with notch lapels, two-button closure. Matching flat-front trousers.',
 449.99, NULL, 2, 12,
 'https://images.unsplash.com/photo-1594938298603-c8148c4a8ba0?w=600&q=80,https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=600&q=80',
 '36R,38R,40R,42R,44R,46R', 'Charcoal,Navy,Black', 4.9, 88),

('Denim Jacket',
 'Classic denim jacket with a modern slim fit. Features a chest pocket, button-front closure, and subtle distressing for a vintage look. 100% cotton denim.',
 129.99, 99.99, 2, 40,
 'https://images.unsplash.com/photo-1542272604-787c3835535d?w=600&q=80,https://images.unsplash.com/photo-1591213954196-2d0ccb3f8d4c?w=600&q=80',
 'XS,S,M,L,XL,XXL', 'Light Wash,Medium Wash,Dark Wash', 4.6, 193),

-- Accessories
('Leather Tote Bag',
 'Spacious genuine leather tote bag with a structured silhouette. Interior zip pocket, magnetic snap closure, and detachable shoulder strap. Perfect for work or weekend.',
 249.99, 189.99, 3, 25,
 'https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=600&q=80,https://images.unsplash.com/photo-1590874103328-eac38a683ce7?w=600&q=80',
 'One Size', 'Tan,Black,Cognac,Navy', 4.7, 342),

('Silk Scarf',
 'Luxurious 100% silk scarf printed with an exclusive artistic pattern. Lightweight and versatile — wear as a scarf, headband, or bag accessory.',
 69.99, NULL, 3, 100,
 'https://images.unsplash.com/photo-1601924994987-69e26d50dc26?w=600&q=80,https://images.unsplash.com/photo-1588099768531-a72d4a198538?w=600&q=80',
 'One Size', 'Floral Multicolor,Abstract Blue,Classic Paisley', 4.5, 127),

('Classic Leather Belt',
 'Full-grain leather belt with a polished silver buckle. Supple and durable construction with a clean taper. Width: 35mm.',
 59.99, NULL, 3, 70,
 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=600&q=80,https://images.unsplash.com/photo-1575738329589-9f5699178de9?w=600&q=80',
 '30,32,34,36,38,40,42', 'Black,Tan,Brown', 4.6, 89),

('Statement Earrings',
 'Bold geometric drop earrings crafted from lightweight brass with gold plating. Hypoallergenic posts. Adds a sophisticated finishing touch to any outfit.',
 34.99, 24.99, 3, 85,
 'https://images.unsplash.com/photo-1535632066927-ab7c9ab60908?w=600&q=80,https://images.unsplash.com/photo-1573408301185-9519f94816b5?w=600&q=80',
 'One Size', 'Gold,Silver,Rose Gold', 4.4, 214),

('Aviator Sunglasses',
 'Classic aviator sunglasses with UV400 polarized lenses and a lightweight metal frame. Timeless style for men and women.',
 89.99, 69.99, 3, 50,
 'https://images.unsplash.com/photo-1572635196237-14b3f281503f?w=600&q=80,https://images.unsplash.com/photo-1577803645773-f96470509666?w=600&q=80',
 'One Size', 'Gold/Brown,Silver/Grey,Black/Black', 4.7, 301),

-- Sale items
('Printed Maxi Skirt',
 'Flowing maxi skirt in a vibrant print on lightweight georgette fabric. Elasticated waist for comfort. Originally $110, now in our summer sale.',
 110.00, 49.99, 4, 30,
 'https://images.unsplash.com/photo-1583496661160-fb5886a0aaaa?w=600&q=80,https://images.unsplash.com/photo-1582639510494-c80b5de9f148?w=600&q=80',
 'XS,S,M,L,XL', 'Tropical Print,Abstract Floral', 4.3, 76),

('Lace Trim Camisole',
 'Delicate lace-trimmed camisole in a satin-touch fabric. Adjustable straps and relaxed fit. Wear alone or layer under blazers.',
 45.00, 19.99, 4, 55,
 'https://images.unsplash.com/photo-1515886657613-9f3515b0c78f?w=600&q=80,https://images.unsplash.com/photo-1544441893-675973e31985?w=600&q=80',
 'XS,S,M,L', 'Ivory,Black,Dusty Rose', 4.2, 143),

('Wool Blend Coat',
 'Oversized wool-blend coat with a single-breasted closure and notch collar. Statement piece for winter layering. Originally $299, now 50% off.',
 299.00, 149.99, 4, 10,
 'https://images.unsplash.com/photo-1539533018447-63fcce2678e3?w=600&q=80,https://images.unsplash.com/photo-1548624313-0396c75e4b1a?w=600&q=80',
 'XS,S,M,L,XL', 'Camel,Charcoal,Burgundy', 4.8, 198),

('Sneakers Classic White',
 'Clean all-white leather sneakers with a minimal design. Premium leather upper, cushioned insole. A versatile everyday essential.',
 120.00, 79.99, 4, 40,
 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600&q=80,https://images.unsplash.com/photo-1549298916-b41d501d3772?w=600&q=80',
 '36,37,38,39,40,41,42,43,44,45', 'White,White/Grey,White/Navy', 4.6, 389),

('Leather Crossbody Bag',
 'Compact genuine leather crossbody bag with an adjustable strap. Interior slip pocket and zip closure. Perfect everyday carry.',
 139.00, 89.99, 4, 22,
 'https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=600&q=80,https://images.unsplash.com/photo-1584917865442-de89df76afd3?w=600&q=80',
 'One Size', 'Black,Tan,Burgundy', 4.5, 167);
