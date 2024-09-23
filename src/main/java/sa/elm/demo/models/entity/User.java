package sa.elm.demo.models.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import sa.elm.demo.models.entity.enums.UserRoleEnum;
import sa.elm.demo.models.entity.enums.UserStatusEnum;

import java.time.OffsetDateTime;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity(name = "user")
@Table(name = "USERS")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "USERNAME")
  private String username;

  @Column(name = "PASSWORD")
  private String password;

  @Column(name = "EMAIL")
  private String email;

  @Enumerated(EnumType.STRING)
  @Column(name = "STATUS")
  private UserStatusEnum userStatus;

  @Enumerated(EnumType.STRING)
  @Column(name = "ROLE")
  private UserRoleEnum userRole;

  @CreationTimestamp
  @Column(name = "CREATED_AT")
  private OffsetDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "UPDATED_AT")
  private OffsetDateTime updatedAt;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Product> products;
}
