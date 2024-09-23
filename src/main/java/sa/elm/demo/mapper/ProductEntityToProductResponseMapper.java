package sa.elm.demo.mapper;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import sa.elm.demo.models.entity.Product;
import sa.elm.demo.models.entity.enums.ProductStatus;
import sa.elm.models.ProductItem;
import sa.elm.models.ProductSummary;
import sa.elm.models.ProductsResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProductEntityToProductResponseMapper {

  public ProductsResponse mapForDealer(Page<Product> productPage) {

    return ProductsResponse.builder()
        .total(productPage.getTotalElements())
        .items(mapItemsForDealer(productPage.getContent()))
        .build();
  }

  private List<ProductItem> mapItemsForDealer(List<Product> productList) {
    List<ProductItem> productItemList = new ArrayList<>();
    if (!CollectionUtils.isEmpty(productList)) {
      productItemList = productList.stream()
          .map(this::mapItemForDealer)
          .collect(Collectors.toList());
    }
    return productItemList;
  }

  private ProductItem mapItemForDealer(Product product) {
    return ProductItem.builder()
        .id(product.getId())
        .name(product.getName())
        .price(product.getPrice())
        .status(product.getStatus() == ProductStatus.ACTIVE ? ProductItem.StatusEnum.ACTIVE : ProductItem.StatusEnum.INACTIVE)
        .build();
  }


  public ProductsResponse mapForClient(Page<Product> productPage) {

    return ProductsResponse.builder()
        .total(productPage.getTotalElements())
        .items(mapItemsForClient(productPage.getContent()))
        .build();
  }

  private List<ProductItem> mapItemsForClient(List<Product> productList) {
    List<ProductItem> productItemList = new ArrayList<>();
    if (!CollectionUtils.isEmpty(productList)) {
      productItemList = productList.stream()
          .map(this::mapItemForClient)
          .collect(Collectors.toList());
    }
    return productItemList;
  }

  private ProductItem mapItemForClient(Product product) {
    return ProductItem.builder()
        .id(product.getId())
        .name(product.getName())
        .dealerName(product.getUser().getUsername())
        .price(product.getPrice())
        .build();
  }

  public ProductsResponse mapForAdmin(Page<Product> productPage) {
    return ProductsResponse.builder()
        .total(productPage.getTotalElements())
        .items(mapItemsForAdmin(productPage.getContent()))
        .build();
  }

  private List<ProductItem> mapItemsForAdmin(List<Product> productList) {
    List<ProductItem> productItemList = new ArrayList<>();
    if (!CollectionUtils.isEmpty(productList)) {
      productItemList = productList.stream()
          .map(this::mapItemForAdmin)
          .collect(Collectors.toList());
    }
    return productItemList;
  }

  private ProductItem mapItemForAdmin(Product product) {
    return ProductItem.builder()
        .id(product.getId())
        .name(product.getName())
        .dealerName(product.getUser().getUsername())
        .price(product.getPrice())
        .status(product.getStatus() == ProductStatus.ACTIVE ? ProductItem.StatusEnum.ACTIVE : ProductItem.StatusEnum.INACTIVE)
        .build();
  }

  public ProductSummary mapToProductSummary(Product product) {
    return ProductSummary.builder()
        .name(product.getName())
        .dealerName(product.getUser().getUsername())
        .price(product.getPrice())
        .build();
  }
}
