-- ============================================================
-- Fix Broken Unsplash Product Image URLs
-- ============================================================

UPDATE products 
SET image_urls = 'https://images.unsplash.com/photo-1591047139829-d91aecb6caea?w=600&q=80,https://images.unsplash.com/photo-1551488831-00ddcb6c6bd3?w=600&q=80' 
WHERE name = 'Linen Blazer';

UPDATE products 
SET image_urls = 'https://images.unsplash.com/photo-1594633312681-425c7b97ccd1?w=600&q=80,https://images.unsplash.com/photo-1485518882345-15568b007407?w=600&q=80' 
WHERE name = 'High-Waist Trousers';

UPDATE products 
SET image_urls = 'https://images.unsplash.com/photo-1593030761757-71fae45fa0e7?w=600&q=80,https://images.unsplash.com/photo-1507679799987-c73779587ccf?w=600&q=80' 
WHERE name = 'Tailored Suit';
