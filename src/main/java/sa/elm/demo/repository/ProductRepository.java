package sa.elm.demo.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import sa.elm.demo.models.entity.Product;
import sa.elm.demo.models.entity.User;
import sa.elm.demo.models.entity.enums.ProductStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
  Page<Product> findByUser(User user, Pageable pageable);

  Page<Product> findByStatus(ProductStatus productStatus, Pageable pageable);

  @Query("SELECT COUNT(p) FROM product p WHERE p.createdAt >= :from AND p.createdAt <= :to")
  Long countTotalProducts(OffsetDateTime from, OffsetDateTime to);

  @Query("SELECT COUNT(p) FROM product p WHERE p.status = :status AND p.createdAt >= :from AND  p.createdAt <= :to")
  Long countProductsByStatus(ProductStatus status, OffsetDateTime from, OffsetDateTime to);

  @Query("SELECT SUM(p.price) FROM product p WHERE p.status = 'ACTIVE' AND p.createdAt >= :from AND p.createdAt <= :to")
  BigDecimal sumActiveProductPrices(OffsetDateTime from, OffsetDateTime to);

  @Query("SELECT p FROM product p WHERE p.createdAt >= :from AND p.createdAt <= :to ORDER BY p.price ASC, p.id DESC")
  List<Product> findLowestPricedProduct(OffsetDateTime from, OffsetDateTime to);

  @Query("SELECT p FROM product p WHERE p.createdAt >= :from AND p.createdAt <= :to ORDER BY p.price DESC, p.id DESC")
  List<Product> findHighestPricedProduct(OffsetDateTime from, OffsetDateTime to);


  @Query("SELECT p.id FROM product p WHERE p.user.id = :userId")
  List<Long> findAllProductIdsByUserId(Long userId);

}
