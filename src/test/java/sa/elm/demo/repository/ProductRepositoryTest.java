package sa.elm.demo.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import sa.elm.demo.models.entity.Product;
import sa.elm.demo.models.entity.User;
import sa.elm.demo.models.entity.enums.ProductStatus;
import sa.elm.demo.models.entity.enums.UserRoleEnum;
import sa.elm.demo.models.entity.enums.UserStatusEnum;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class ProductRepositoryTest {

  @Autowired
  private ProductRepository productRepository;
  @Autowired
  private UserRepository userRepository;

  private User user;
  private Product product1;
  private Product product2;

  @BeforeEach
  void setUp() {
    user = User.builder()
        .username("dummy")
        .email("dummy@example.com")
        .password("dummyPassword")
        .userRole(UserRoleEnum.DEALER)
        .userStatus(UserStatusEnum.ACTIVE)
        .build();
    user = userRepository.save(user);

    product1 = Product.builder()
        .name("Product 1")
        .price(BigDecimal.valueOf(100.00))
        .status(ProductStatus.ACTIVE)
        .user(user)
        .createdAt(OffsetDateTime.now())
        .build();

    product2 = Product.builder()
        .name("Product 2")
        .price(BigDecimal.valueOf(200.00))
        .status(ProductStatus.INACTIVE)
        .user(user)
        .createdAt(OffsetDateTime.now())
        .build();

    productRepository.save(product1);
    productRepository.save(product2);
  }

  @Test
  void testFindByUser() {
    PageRequest pageRequest = PageRequest.of(0, 10);

    Page<Product> productPage = productRepository.findByUser(user, pageRequest);

    assertEquals(2, productPage.getTotalElements());
    assertEquals("Product 1", productPage.getContent().get(0).getName());
  }

  @Test
  void testFindByStatus() {
    PageRequest pageRequest = PageRequest.of(0, 10);

    Page<Product> activeProductPage = productRepository.findByStatus(ProductStatus.ACTIVE, pageRequest);

    assertEquals(1, activeProductPage.getTotalElements());
    assertEquals("Product 1", activeProductPage.getContent().get(0).getName());
  }

  @Test
  void testCountTotalProducts() {
    Long count = productRepository.countTotalProducts(OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusDays(1));
    assertEquals(2, count);
  }

  @Test
  void testCountProductsByStatus() {
    Long activeCount = productRepository.countProductsByStatus(ProductStatus.ACTIVE, OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusDays(1));
    Long inactiveCount = productRepository.countProductsByStatus(ProductStatus.INACTIVE, OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusDays(1));

    assertEquals(1, activeCount);
    assertEquals(1, inactiveCount);
  }

  @Test
  void testSumActiveProductPrices() {
    BigDecimal totalSum = productRepository.sumActiveProductPrices(OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusDays(1));

    assertTrue(BigDecimal.valueOf(100.00).compareTo(totalSum) == 0);
  }

  @Test
  void testFindLowestPricedProduct() {
    List<Product> lowestPricedProducts = productRepository.findLowestPricedProduct(OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusDays(1));

    assertEquals(2, lowestPricedProducts.size());
    assertEquals("Product 1", lowestPricedProducts.get(0).getName());
  }

  @Test
  void testFindHighestPricedProduct() {
    List<Product> highestPricedProducts = productRepository.findHighestPricedProduct(OffsetDateTime.now().minusDays(1), OffsetDateTime.now().plusDays(1));

    assertEquals(2, highestPricedProducts.size());
    assertEquals("Product 2", highestPricedProducts.get(0).getName());
  }
}