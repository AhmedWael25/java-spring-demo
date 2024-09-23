package sa.elm.demo.models.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import sa.elm.demo.models.entity.enums.ProductStatus;

import java.math.BigDecimal;
import java.time.OffsetDateTime;


@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity(name = "product")
@Table(name = "PRODUCTS")
public class Product {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "NAME")
  private String name;

  @Column(name = "PRICE")
  private BigDecimal price;

  @Enumerated(EnumType.STRING)
  @Column(name = "STATUS")
  private ProductStatus status;

  @ManyToOne
  @JoinColumn(name = "USER_ID")
  private User user;

  @CreationTimestamp
  @Column(name = "CREATED_AT")
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "UPDATED_AT")
  private OffsetDateTime updatedAt;

}
