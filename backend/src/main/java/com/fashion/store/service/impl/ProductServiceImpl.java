package com.fashion.store.service.impl;

import com.fashion.store.dto.CategoryDTO;
import com.fashion.store.dto.ProductDTO;
import com.fashion.store.entity.Category;
import com.fashion.store.entity.Product;
import com.fashion.store.exception.ResourceNotFoundException;
import com.fashion.store.repository.CategoryRepository;
import com.fashion.store.repository.ProductRepository;
import com.fashion.store.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public ProductDTO.PagedProductResponse getProducts(Long categoryId, BigDecimal minPrice,
            BigDecimal maxPrice, String keyword, Pageable pageable) {

        String kw = StringUtils.hasText(keyword) ? keyword.trim() : null;
        Page<Product> page = productRepository.findWithFilters(categoryId, minPrice, maxPrice, kw, pageable);

        List<ProductDTO.ProductResponse> content = page.getContent().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());

        return ProductDTO.PagedProductResponse.builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductDTO.ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        return toResponse(product);
    }

    @Override
    @Transactional
    public ProductDTO.ProductResponse createProduct(ProductDTO.ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .discountPrice(request.getDiscountPrice())
                .category(category)
                .stockQuantity(request.getStockQuantity())
                .imageUrls(listToString(request.getImageUrls()))
                .sizes(listToString(request.getSizes()))
                .colors(listToString(request.getColors()))
                .rating(0.0)
                .reviewCount(0)
                .build();

        return toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public ProductDTO.ProductResponse updateProduct(Long id, ProductDTO.ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", request.getCategoryId()));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setDiscountPrice(request.getDiscountPrice());
        product.setCategory(category);
        product.setStockQuantity(request.getStockQuantity());
        product.setImageUrls(listToString(request.getImageUrls()));
        product.setSizes(listToString(request.getSizes()));
        product.setColors(listToString(request.getColors()));

        return toResponse(productRepository.save(product));
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", id);
        }
        productRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO.CategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(c -> CategoryDTO.CategoryResponse.builder()
                        .id(c.getId())
                        .name(c.getName())
                        .slug(c.getSlug())
                        .build())
                .collect(Collectors.toList());
    }

    public ProductDTO.ProductResponse toResponse(Product p) {
        return ProductDTO.ProductResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .discountPrice(p.getDiscountPrice())
                .category(CategoryDTO.CategoryResponse.builder()
                        .id(p.getCategory().getId())
                        .name(p.getCategory().getName())
                        .slug(p.getCategory().getSlug())
                        .build())
                .stockQuantity(p.getStockQuantity())
                .imageUrls(stringToList(p.getImageUrls()))
                .sizes(stringToList(p.getSizes()))
                .colors(stringToList(p.getColors()))
                .rating(p.getRating())
                .reviewCount(p.getReviewCount())
                .createdAt(p.getCreatedAt() != null ? p.getCreatedAt().toString() : null)
                .build();
    }

    private String listToString(List<String> list) {
        if (list == null || list.isEmpty()) return null;
        return String.join(",", list);
    }

    private List<String> stringToList(String str) {
        if (!StringUtils.hasText(str)) return Collections.emptyList();
        return Arrays.stream(str.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .collect(Collectors.toList());
    }
}
