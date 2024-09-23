package sa.elm.demo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import sa.elm.demo.exception.NotAuthorizedToChangeStatusOfProduct;
import sa.elm.demo.exception.ProductNotFoundException;
import sa.elm.demo.mapper.ProductEntityToProductResponseMapper;
import sa.elm.demo.models.entity.Product;
import sa.elm.demo.models.entity.User;
import sa.elm.demo.models.entity.enums.ProductStatus;
import sa.elm.demo.models.entity.enums.UserRoleEnum;
import sa.elm.demo.models.entity.enums.UserStatusEnum;
import sa.elm.demo.models.security.SecurityUser;
import sa.elm.demo.repository.ProductRepository;
import sa.elm.models.ProductCreationRequest;
import sa.elm.models.ProductsResponse;
import sa.elm.models.StatisticsResponse;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductsServiceTest {

  @Mock
  private UsersService usersService;

  @Mock
  private ProductRepository productRepository;

  @Mock
  private ProductEntityToProductResponseMapper entityToProductResponseMapper;

  @InjectMocks
  private ProductsService productsService;

  @Mock
  private SecurityContext securityContext;

  @Mock
  private Authentication authentication;

  @Mock
  private SecurityUser securityUser;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }


  @Test
  void testGetAllDealerProducts_ShouldSuccess() {
    when(securityUser.getId()).thenReturn(1L);
    when(authentication.getPrincipal()).thenReturn(securityUser);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);

    User user = new User();
    user.setId(1L);
    when(usersService.findUserById(anyLong())).thenReturn(user);

    Page<Product> productPage = new PageImpl<>(List.of(new Product()));
    when(productRepository.findByUser(any(User.class), any(PageRequest.class))).thenReturn(productPage);

    ProductsResponse productsResponse = ProductsResponse.builder().build();
    when(entityToProductResponseMapper.mapForDealer(any(Page.class))).thenReturn(productsResponse);

    ProductsResponse response = productsService.getAllDealerProducts(10, 0);

    assertNotNull(response);
    verify(usersService, times(1)).findUserById(anyLong());
    verify(productRepository, times(1)).findByUser(any(User.class), any(PageRequest.class));
  }

  @Test
  void testAddNewProduct_ShouldSuccess() {
    when(securityUser.getId()).thenReturn(1L);
    when(authentication.getPrincipal()).thenReturn(securityUser);
    when(securityContext.getAuthentication()).thenReturn(authentication);
    SecurityContextHolder.setContext(securityContext);

    User user = new User();
    user.setId(1L);
    when(usersService.findUserById(anyLong())).thenReturn(user);

    ProductCreationRequest request = new ProductCreationRequest();
    request.setName("New Product");
    request.setPrice(BigDecimal.valueOf(100));

    productsService.addNewProduct(request);

    verify(productRepository, times(1)).save(any(Product.class));
  }

  @Test
  void testChangeProductStatus_ShouldSuccess_ACTIVE_To_INACTIVE() {
    Product product = new Product();
    product.setId(1L);
    product.setStatus(ProductStatus.ACTIVE);

    SecurityUser dealer = new SecurityUser(String.valueOf(2L),
        true,
        List.of(new SimpleGrantedAuthority("DEALER")));
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(dealer);
    when(usersService.findUserById(eq(2L))).thenReturn(User.builder().id(2L).build());
    SecurityContextHolder.setContext(securityContext);

    when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
    when(productRepository.findAllProductIdsByUserId(anyLong())).thenReturn(List.of(1L));

    productsService.changeProductStatus(1L);

    verify(productRepository, times(1)).save(any(Product.class));
    assertEquals(ProductStatus.INACTIVE, product.getStatus());
  }

  @Test
  void testChangeProductStatus_ShouldSuccess_INACTIVE_To_ACTIVE() {
    Product product = new Product();
    product.setId(1L);
    product.setStatus(ProductStatus.INACTIVE);

    SecurityUser dealer = new SecurityUser(String.valueOf(2L),
        true,
        List.of(new SimpleGrantedAuthority("DEALER")));
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(dealer);
    when(usersService.findUserById(eq(2L))).thenReturn(User.builder().id(2L).build());
    SecurityContextHolder.setContext(securityContext);

    when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
    when(productRepository.findAllProductIdsByUserId(anyLong())).thenReturn(List.of(1L));

    productsService.changeProductStatus(1L);

    verify(productRepository, times(1)).save(any(Product.class));
    assertEquals(ProductStatus.ACTIVE, product.getStatus());
  }

  @Test
  void testChangeProductStatus_Should_GIVE_FORBIDDEN() {
    Product product = new Product();
    product.setId(1L);
    product.setStatus(ProductStatus.ACTIVE);

    SecurityUser dealer = new SecurityUser(String.valueOf(2L),
        true,
        List.of(new SimpleGrantedAuthority("DEALER")));
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(dealer);
    when(usersService.findUserById(eq(2L))).thenReturn(User.builder().id(2L).build());
    SecurityContextHolder.setContext(securityContext);

    when(productRepository.findById(anyLong())).thenReturn(Optional.of(product));
    when(productRepository.findAllProductIdsByUserId(anyLong())).thenReturn(List.of(2L));

    assertThrows(NotAuthorizedToChangeStatusOfProduct.class, () -> productsService.changeProductStatus(1L));
  }

  @Test
  void testChangeProductStatus_ShouldFail() {
    Product product = new Product();
    product.setId(1L);
    product.setStatus(ProductStatus.INACTIVE);

    when(productRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThrows(ProductNotFoundException.class, () -> productsService.changeProductStatus(1L));
  }


  @Test
  void testGetAllActiveProducts_ShouldSuccess() {
    Page<Product> productPage = new PageImpl<>(List.of(new Product()));
    when(productRepository.findByStatus(any(ProductStatus.class), any(PageRequest.class))).thenReturn(productPage);

    ProductsResponse productsResponse = ProductsResponse.builder().build();
    when(entityToProductResponseMapper.mapForClient(any(Page.class))).thenReturn(productsResponse);

    ProductsResponse response = productsService.getAllActiveProducts(10, 0);

    assertNotNull(response);
    verify(productRepository, times(1)).findByStatus(any(ProductStatus.class), any(PageRequest.class));
  }

  @Test
  void testGetProductStatistics_ShouldSuccess() {
    OffsetDateTime from = OffsetDateTime.now().minusDays(10);
    OffsetDateTime to = OffsetDateTime.now();

    when(productRepository.countTotalProducts(any(OffsetDateTime.class), any(OffsetDateTime.class))).thenReturn(100L);
    when(productRepository.countProductsByStatus(eq(ProductStatus.ACTIVE), any(OffsetDateTime.class), any(OffsetDateTime.class))).thenReturn(80L);
    when(productRepository.countProductsByStatus(eq(ProductStatus.INACTIVE), any(OffsetDateTime.class), any(OffsetDateTime.class))).thenReturn(20L);
    when(productRepository.sumActiveProductPrices(any(OffsetDateTime.class), any(OffsetDateTime.class))).thenReturn(BigDecimal.valueOf(50000));

    when(usersService.getTotalUsers(any(UserRoleEnum.class), any(OffsetDateTime.class), any(OffsetDateTime.class))).thenReturn(50L);
    when(usersService.getTotalUsersBasedOnStatus(eq(UserRoleEnum.CLIENT), eq(UserStatusEnum.ACTIVE), any(OffsetDateTime.class), any(OffsetDateTime.class))).thenReturn(30L);
    when(usersService.getTotalUsersBasedOnStatus(eq(UserRoleEnum.CLIENT), eq(UserStatusEnum.INACTIVE), any(OffsetDateTime.class), any(OffsetDateTime.class))).thenReturn(20L);
    when(usersService.getTotalDealersWithProducts(any(OffsetDateTime.class), any(OffsetDateTime.class))).thenReturn(25L);
    when(usersService.getTotalDealersWithNoProducts(any(OffsetDateTime.class), any(OffsetDateTime.class))).thenReturn(10L);

    StatisticsResponse response = productsService.getProductStatistics(from.toLocalDate(), to.toLocalDate());

    assertNotNull(response);
    assertEquals(100L, response.getProducts().getTotalProducts());
    assertEquals(80L, response.getProducts().getActive());
    assertEquals(20L, response.getProducts().getInactive());
    assertEquals(BigDecimal.valueOf(50000), response.getProducts().getTotalPrice());

    verify(usersService, times(1)).getTotalUsers(eq(UserRoleEnum.CLIENT), any(OffsetDateTime.class), any(OffsetDateTime.class));
    verify(usersService, times(1)).getTotalUsers(eq(UserRoleEnum.DEALER), any(OffsetDateTime.class), any(OffsetDateTime.class));
    verify(usersService, times(1)).getTotalUsersBasedOnStatus(eq(UserRoleEnum.CLIENT), eq(UserStatusEnum.ACTIVE), any(OffsetDateTime.class), any(OffsetDateTime.class));
    verify(usersService, times(1)).getTotalUsersBasedOnStatus(eq(UserRoleEnum.CLIENT), eq(UserStatusEnum.INACTIVE), any(OffsetDateTime.class), any(OffsetDateTime.class));
  }
}


