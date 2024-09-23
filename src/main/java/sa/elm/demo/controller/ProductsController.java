package sa.elm.demo.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RestController;
import sa.elm.api.ProductsApi;
import sa.elm.demo.service.ProductsService;
import sa.elm.models.ProductCreationRequest;
import sa.elm.models.ProductsResponse;
import sa.elm.models.StatisticsResponse;

import java.time.LocalDate;

@Slf4j
@AllArgsConstructor
@RestController("/products")
public class ProductsController implements ProductsApi {

  public static final String X_TOTAL_COUNT = "X-TOTAL-COUNT";
  private final ProductsService productsService;

  @Secured("DEALER")
  @Override
  public ResponseEntity<ProductsResponse> getProducts(Integer limit, Integer offset) {
    ProductsResponse productsResponse = productsService.getAllDealerProducts(limit, offset);
    return ResponseEntity.ok()
        .header(X_TOTAL_COUNT, String.valueOf(productsResponse.getTotal()))
        .body(productsResponse);
  }

  @Secured("DEALER")
  @Override
  public ResponseEntity<Void> addProduct(ProductCreationRequest productCreationRequest) {
    productsService.addNewProduct(productCreationRequest);
    return ResponseEntity.ok().build();
  }

  @Secured("DEALER")
  @Override
  public ResponseEntity<Void> changeProductStatus(Long id) {
    productsService.changeProductStatus(id);
    return ResponseEntity.ok().build();
  }


  @Secured("CLIENT")
  @Override
  public ResponseEntity<ProductsResponse> getActiveProducts(Integer limit, Integer offset) {
    ProductsResponse productsResponse = productsService.getAllActiveProducts(limit, offset);
    return ResponseEntity.ok()
        .header(X_TOTAL_COUNT, String.valueOf(productsResponse.getTotal()))
        .body(productsResponse);
  }

  @Secured("ADMIN")
  @Override
  public ResponseEntity<ProductsResponse> getAllProducts(Integer limit, Integer offset) {
    ProductsResponse productsResponse = productsService.getAllProducts(limit, offset);
    return ResponseEntity.ok()
        .header(X_TOTAL_COUNT, String.valueOf(productsResponse.getTotal()))
        .body(productsResponse);
  }

  @Secured("ADMIN")
  @Override
  public ResponseEntity<StatisticsResponse> getProductStatistics(LocalDate from, LocalDate to) {
    StatisticsResponse productStatisticsResponse = productsService.getProductStatistics(from, to);
    return ResponseEntity.ok(productStatisticsResponse);
  }


}
