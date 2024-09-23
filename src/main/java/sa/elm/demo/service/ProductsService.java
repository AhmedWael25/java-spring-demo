package sa.elm.demo.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
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
import sa.elm.models.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@AllArgsConstructor
@Service
public class ProductsService {

  private final UsersService usersService;
  private final ProductRepository productRepository;
  private final ProductEntityToProductResponseMapper entityToProductResponseMapper;


  public ProductsResponse getAllDealerProducts(Integer limit, Integer offset) {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
    User user = usersService.findUserById(securityUser.getId());

    PageRequest pageRequest = PageRequest.of(offset / limit, limit);
    Page<Product> productPage = productRepository.findByUser(user, pageRequest);

    return entityToProductResponseMapper.mapForDealer(productPage);
  }

  public void addNewProduct(ProductCreationRequest productCreationRequest) {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();

    User user = usersService.findUserById(securityUser.getId());

    Product newProduct = Product.builder()
        .name(productCreationRequest.getName())
        .price(productCreationRequest.getPrice())
        .user(user)
        .status(ProductStatus.ACTIVE).build();
    productRepository.save(newProduct);
  }

  public void changeProductStatus(Long id) {
    Product product = findProductById(id);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
    User user = usersService.findUserById(securityUser.getId());
    List<Long> allProductIdsOfThatDealer = productRepository.findAllProductIdsByUserId(user.getId());

    if (!allProductIdsOfThatDealer.contains(product.getId())) {
      throw new NotAuthorizedToChangeStatusOfProduct("Not Authorized to change this Product Status");
    }

    if (product.getStatus() == ProductStatus.ACTIVE) {
      product.setStatus(ProductStatus.INACTIVE);
    } else {
      product.setStatus(ProductStatus.ACTIVE);
    }
    productRepository.save(product);
  }

  public ProductsResponse getAllActiveProducts(Integer limit, Integer offset) {

    PageRequest pageRequest = PageRequest.of(offset / limit, limit);
    Page<Product> productPage = productRepository.findByStatus(ProductStatus.ACTIVE, pageRequest);

    return entityToProductResponseMapper.mapForClient(productPage);
  }


  private Product findProductById(Long id) {

    Optional<Product> optionalProduct = productRepository.findById(id);
    return optionalProduct.orElseThrow(() -> {
          log.error("Product with Id:{} not found", id);
          return new ProductNotFoundException("Product Not Found");
        }
    );
  }

  public ProductsResponse getAllProducts(Integer limit, Integer offset) {

    PageRequest pageRequest = PageRequest.of(offset / limit, limit);
    Page<Product> productPage = productRepository.findAll(pageRequest);

    return entityToProductResponseMapper.mapForAdmin(productPage);
  }

  public StatisticsResponse getProductStatistics(LocalDate from, LocalDate to) {

    ZoneOffset offset = ZoneOffset.UTC;
    OffsetDateTime fromAsOffsetDateTime = Objects.nonNull(from) ? from.atStartOfDay().atOffset(offset) : OffsetDateTime.now();
    OffsetDateTime toAsOffsetDateTime = Objects.nonNull(to) ? to.atStartOfDay().atOffset(offset) : OffsetDateTime.now();

    // Product statistics
    Long totalProducts = productRepository.countTotalProducts(fromAsOffsetDateTime, toAsOffsetDateTime);
    Long totalActiveProducts = productRepository.countProductsByStatus(ProductStatus.ACTIVE, fromAsOffsetDateTime, toAsOffsetDateTime);
    Long totalInactiveProducts = productRepository.countProductsByStatus(ProductStatus.INACTIVE, fromAsOffsetDateTime, toAsOffsetDateTime);
    BigDecimal totalSumActivePrices = productRepository.sumActiveProductPrices(fromAsOffsetDateTime, toAsOffsetDateTime);
    List<Product> highestPricedProduct = productRepository.findHighestPricedProduct(fromAsOffsetDateTime, toAsOffsetDateTime);
    List<Product> lowestPricedProduct = productRepository.findLowestPricedProduct(fromAsOffsetDateTime, toAsOffsetDateTime);
    ProductStatisticsResponse productStatisticsResponse = ProductStatisticsResponse.builder()
        .totalProducts(totalProducts)
        .active(totalActiveProducts)
        .inactive(totalInactiveProducts)
        .totalPrice(totalSumActivePrices)
        .highest(!CollectionUtils.isEmpty(highestPricedProduct) ?
            entityToProductResponseMapper.mapToProductSummary(highestPricedProduct.get(0)) : null)
        .lowest(!CollectionUtils.isEmpty(lowestPricedProduct) ?
            entityToProductResponseMapper.mapToProductSummary(lowestPricedProduct.get(0)) : null)
        .build();

    // Client statistics
    Long totalClients = usersService.getTotalUsers(UserRoleEnum.CLIENT, fromAsOffsetDateTime, toAsOffsetDateTime);
    Long totalActiveClients = usersService.getTotalUsersBasedOnStatus(UserRoleEnum.CLIENT, UserStatusEnum.ACTIVE, fromAsOffsetDateTime, toAsOffsetDateTime);
    Long totalInactiveClient = usersService.getTotalUsersBasedOnStatus(UserRoleEnum.CLIENT, UserStatusEnum.INACTIVE, fromAsOffsetDateTime, toAsOffsetDateTime);
    ClientsStatisticsResponse clientsStatisticsResponse = ClientsStatisticsResponse.builder()
        .total(totalClients)
        .active(totalActiveClients)
        .inactive(totalInactiveClient)
        .build();

    // Dealer statistics
    Long totalDealers = usersService.getTotalUsers(UserRoleEnum.DEALER, fromAsOffsetDateTime, toAsOffsetDateTime);
    Long totalDealersWithProducts = usersService.getTotalDealersWithProducts(fromAsOffsetDateTime, toAsOffsetDateTime);
    Long totalDealersWithNoProducts = usersService.getTotalDealersWithNoProducts(fromAsOffsetDateTime, toAsOffsetDateTime);
    DealersStatisticsResponse dealersStatisticsResponse = DealersStatisticsResponse.builder()
        .total(totalDealers)
        .hasProducts(totalDealersWithProducts)
        .hasNoProducts(totalDealersWithNoProducts)
        .build();

    return StatisticsResponse.builder()
        .products(productStatisticsResponse)
        .dealers(dealersStatisticsResponse)
        .clients(clientsStatisticsResponse)
        .build();
  }

}

