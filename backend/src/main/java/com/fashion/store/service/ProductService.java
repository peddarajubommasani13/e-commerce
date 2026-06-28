package com.fashion.store.service;

import com.fashion.store.dto.CategoryDTO;
import com.fashion.store.dto.ProductDTO;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface ProductService {
    ProductDTO.PagedProductResponse getProducts(Long categoryId, BigDecimal minPrice,
            BigDecimal maxPrice, String keyword, Pageable pageable);
    ProductDTO.ProductResponse getProductById(Long id);
    ProductDTO.ProductResponse createProduct(ProductDTO.ProductRequest request);
    ProductDTO.ProductResponse updateProduct(Long id, ProductDTO.ProductRequest request);
    void deleteProduct(Long id);
    java.util.List<CategoryDTO.CategoryResponse> getAllCategories();
}
