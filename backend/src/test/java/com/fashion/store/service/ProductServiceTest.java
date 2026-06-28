package com.fashion.store.service;

import com.fashion.store.dto.ProductDTO;
import com.fashion.store.entity.*;
import com.fashion.store.exception.ResourceNotFoundException;
import com.fashion.store.repository.*;
import com.fashion.store.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;

    @InjectMocks private ProductServiceImpl productService;

    private Category testCategory;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder().id(1L).name("Women").slug("women").build();
        testProduct = Product.builder()
                .id(1L)
                .name("Test Dress")
                .description("Beautiful dress")
                .price(new BigDecimal("99.99"))
                .discountPrice(new BigDecimal("79.99"))
                .category(testCategory)
                .stockQuantity(10)
                .imageUrls("https://example.com/image.jpg")
                .sizes("S,M,L")
                .colors("Black,White")
                .rating(4.5)
                .reviewCount(20)
                .build();
    }

    @Test
    @DisplayName("GetProducts: returns paged response")
    void getProducts_returnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(testProduct), pageable, 1);
        when(productRepository.findWithFilters(null, null, null, null, pageable)).thenReturn(page);

        ProductDTO.PagedProductResponse response = productService.getProducts(null, null, null, null, pageable);

        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getContent().get(0).getName()).isEqualTo("Test Dress");
    }

    @Test
    @DisplayName("GetProductById: existing id returns product")
    void getProductById_success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        ProductDTO.ProductResponse response = productService.getProductById(1L);

        assertThat(response.getName()).isEqualTo("Test Dress");
        assertThat(response.getPrice()).isEqualTo(new BigDecimal("99.99"));
        assertThat(response.getImageUrls()).contains("https://example.com/image.jpg");
        assertThat(response.getSizes()).containsExactly("S", "M", "L");
    }

    @Test
    @DisplayName("GetProductById: missing id throws ResourceNotFoundException")
    void getProductById_notFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("CreateProduct: valid request saves and returns product")
    void createProduct_success() {
        ProductDTO.ProductRequest req = new ProductDTO.ProductRequest();
        req.setName("New Dress");
        req.setDescription("Lovely");
        req.setPrice(new BigDecimal("120.00"));
        req.setCategoryId(1L);
        req.setStockQuantity(5);
        req.setImageUrls(List.of("https://example.com/img.jpg"));
        req.setSizes(List.of("M", "L"));
        req.setColors(List.of("Red"));

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        ProductDTO.ProductResponse response = productService.createProduct(req);

        assertThat(response).isNotNull();
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("DeleteProduct: existing product gets deleted")
    void deleteProduct_success() {
        when(productRepository.existsById(1L)).thenReturn(true);

        assertThatCode(() -> productService.deleteProduct(1L)).doesNotThrowAnyException();
        verify(productRepository).deleteById(1L);
    }

    @Test
    @DisplayName("DeleteProduct: missing product throws ResourceNotFoundException")
    void deleteProduct_notFound() {
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> productService.deleteProduct(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
