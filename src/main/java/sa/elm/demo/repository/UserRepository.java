package sa.elm.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import sa.elm.demo.models.entity.User;
import sa.elm.demo.models.entity.enums.UserRoleEnum;
import sa.elm.demo.models.entity.enums.UserStatusEnum;

import java.time.OffsetDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByUsername(String username);

  Optional<User> findByEmail(String email);

  @Query("SELECT COUNT(u) FROM user u WHERE u.userRole = :role AND u.createdAt >= :from AND  u.createdAt <= :to")
  Long countTotalUsersBasedOnRole(UserRoleEnum role, OffsetDateTime from, OffsetDateTime to);

  @Query("SELECT COUNT(u) FROM user u WHERE u.userRole = :role AND u.userStatus = :status AND u.createdAt >= :from AND u.createdAt <= :to")
  Long countTotalUsersBasedOnRoleAndStatus(UserRoleEnum role, UserStatusEnum status, OffsetDateTime from, OffsetDateTime to);

  @Query("SELECT COUNT(u) FROM user u WHERE u.userRole = 'DEALER' AND u.products IS EMPTY AND u.createdAt >= :from AND u.createdAt <= :to")
  long countDealersWithNoProducts(OffsetDateTime from, OffsetDateTime to);

  @Query("SELECT COUNT(u) FROM user u WHERE u.userRole = 'DEALER' AND u.products IS NOT EMPTY AND u.createdAt >= :from AND  u.createdAt <= :to")
  long countDealersWithProducts(OffsetDateTime from, OffsetDateTime to);

}
