package sa.elm.demo.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import sa.elm.demo.models.entity.User;
import sa.elm.demo.models.entity.enums.UserRoleEnum;
import sa.elm.demo.models.entity.enums.UserStatusEnum;

import java.time.OffsetDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
public class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;

  @BeforeEach
  void setUp() {
    User user1 = User.builder()
        .username("dummy1")
        .email("dummy1@example.com")
        .password("password")
        .userRole(UserRoleEnum.CLIENT)
        .userStatus(UserStatusEnum.ACTIVE)
        .createdAt(OffsetDateTime.now().minusDays(5))
        .build();

    User user2 = User.builder()
        .username("dummy2")
        .email("dummy2@example.com")
        .password("password")
        .userRole(UserRoleEnum.DEALER)
        .userStatus(UserStatusEnum.INACTIVE)
        .createdAt(OffsetDateTime.now().minusDays(3))
        .build();

    userRepository.save(user1);
    userRepository.save(user2);
  }

  @Test
  void testFindByUsername() {
    Optional<User> foundUser = userRepository.findByUsername("dummy1");
    assertTrue(foundUser.isPresent());
    assertEquals("dummy1", foundUser.get().getUsername());
  }

  @Test
  void testFindByEmail() {
    Optional<User> foundUser = userRepository.findByEmail("dummy1@example.com");
    assertTrue(foundUser.isPresent());
    assertEquals("dummy1@example.com", foundUser.get().getEmail());
  }

  @Test
  void testCountTotalUsersBasedOnRole() {
    OffsetDateTime from = OffsetDateTime.now().minusDays(10);
    OffsetDateTime to = OffsetDateTime.now().plusDays(1);

    Long clientCount = userRepository.countTotalUsersBasedOnRole(UserRoleEnum.CLIENT, from, to);
    Long dealerCount = userRepository.countTotalUsersBasedOnRole(UserRoleEnum.DEALER, from, to);

    assertEquals(1, clientCount);
    assertEquals(1, dealerCount);
  }

  @Test
  void testCountTotalUsersBasedOnRoleAndStatus() {
    OffsetDateTime from = OffsetDateTime.now().minusDays(10);
    OffsetDateTime to = OffsetDateTime.now().plusDays(1);

    Long activeClientCount = userRepository.countTotalUsersBasedOnRoleAndStatus(UserRoleEnum.CLIENT, UserStatusEnum.ACTIVE, from, to);
    Long inactiveDealerCount = userRepository.countTotalUsersBasedOnRoleAndStatus(UserRoleEnum.DEALER, UserStatusEnum.INACTIVE, from, to);

    assertEquals(1, activeClientCount);
    assertEquals(1, inactiveDealerCount);
  }

  @Test
  void testCountDealersWithNoProducts() {
    OffsetDateTime from = OffsetDateTime.now().minusDays(10);
    OffsetDateTime to = OffsetDateTime.now().plusDays(1);

    long dealerNoProductsCount = userRepository.countDealersWithNoProducts(from, to);

    assertEquals(1, dealerNoProductsCount);
  }

  @Test
  void testCountDealersWithProducts() {
    OffsetDateTime from = OffsetDateTime.now().minusDays(10);
    OffsetDateTime to = OffsetDateTime.now().plusDays(1);

    long dealerWithProductsCount = userRepository.countDealersWithProducts(from, to);

    assertEquals(0, dealerWithProductsCount);
  }
}