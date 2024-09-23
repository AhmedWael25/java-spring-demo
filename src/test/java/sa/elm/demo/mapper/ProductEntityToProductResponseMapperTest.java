package sa.elm.demo.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import sa.elm.demo.models.entity.Product;
import sa.elm.demo.models.entity.User;
import sa.elm.demo.models.entity.enums.ProductStatus;
import sa.elm.models.ProductItem;
import sa.elm.models.ProductSummary;
import sa.elm.models.ProductsResponse;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductEntityToProductResponseMapperTest {

  @InjectMocks
  private ProductEntityToProductResponseMapper mapper;

  private Product product1;
  private Product product2;

  @BeforeEach
  void setUp() {
    mapper = new ProductEntityToProductResponseMapper();
    User dealer = User.builder()
        .username("dealer_user")
        .email("dealer@example.com")
        .build();

    product1 = Product.builder()
        .id(1L)
        .name("Product 1")
        .price(BigDecimal.valueOf(100))
        .status(ProductStatus.ACTIVE)
        .user(dealer)
        .build();

    product2 = Product.builder()
        .id(2L)
        .name("Product 2")
        .price(BigDecimal.valueOf(200))
        .status(ProductStatus.INACTIVE)
        .user(dealer)
        .build();
  }

  @Test
  void testMapForDealer() {
    Page<Product> productPage = new PageImpl<>(List.of(product1, product2));

    ProductsResponse response = mapper.mapForDealer(productPage);

    assertEquals(2, response.getTotal());
    assertEquals(2, response.getItems().size());
    assertEquals("Product 1", response.getItems().get(0).getName());
    assertEquals(ProductItem.StatusEnum.ACTIVE, response.getItems().get(0).getStatus());
    assertEquals(ProductItem.StatusEnum.INACTIVE, response.getItems().get(1).getStatus());
  }

  @Test
  void testMapForClient() {
    Page<Product> productPage = new PageImpl<>(List.of(product1, product2));

    ProductsResponse response = mapper.mapForClient(productPage);

    assertEquals(2, response.getTotal());
    assertEquals(2, response.getItems().size());
    assertEquals("Product 1", response.getItems().get(0).getName());
    assertEquals("dealer_user", response.getItems().get(0).getDealerName());
    assertEquals("Product 2", response.getItems().get(1).getName());
  }

  @Test
  void testMapForAdmin() {
    Page<Product> productPage = new PageImpl<>(List.of(product1, product2));

    ProductsResponse response = mapper.mapForAdmin(productPage);

    assertEquals(2, response.getTotal());
    assertEquals(2, response.getItems().size());
    assertEquals("Product 1", response.getItems().get(0).getName());
    assertEquals("dealer_user", response.getItems().get(0).getDealerName());
    assertEquals("Product 2", response.getItems().get(1).getName());
    assertEquals(ProductItem.StatusEnum.INACTIVE, response.getItems().get(1).getStatus());
  }

  @Test
  void testMapToProductSummary() {
    ProductSummary summary = mapper.mapToProductSummary(product1);

    assertEquals("Product 1", summary.getName());
    assertEquals("dealer_user", summary.getDealerName());
    assertEquals(BigDecimal.valueOf(100), summary.getPrice());
  }
}