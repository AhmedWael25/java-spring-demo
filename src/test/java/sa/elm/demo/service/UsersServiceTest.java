package sa.elm.demo.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import sa.elm.demo.exception.AuthenticationException;
import sa.elm.demo.exception.OperationNotAllowedException;
import sa.elm.demo.exception.UserAlreadyExistsException;
import sa.elm.demo.exception.UserNotFoundException;
import sa.elm.demo.models.entity.User;
import sa.elm.demo.models.entity.enums.UserRoleEnum;
import sa.elm.demo.models.entity.enums.UserStatusEnum;
import sa.elm.demo.models.security.SecurityUser;
import sa.elm.demo.repository.UserRepository;
import sa.elm.demo.util.JwtUtil;
import sa.elm.models.LoginRequest;
import sa.elm.models.LoginResponse;
import sa.elm.models.RegistrationRequest;
import sa.elm.models.UserCreationRequest;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UsersServiceTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private JwtUtil jwtUtil;

  @Mock
  private PasswordEncoder bCryptPasswordEncoder;

  @Mock
  private Authentication authentication;

  @Mock
  private SecurityContext securityContext;

  @InjectMocks
  private UsersService usersService;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testRegisterUser_ShouldSuccess() {
    RegistrationRequest registrationRequest = new RegistrationRequest("dummy", "dummy@example.com", "password");

    when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
    when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
    when(bCryptPasswordEncoder.encode(anyString())).thenReturn("hashed_password");

    usersService.registerUser(registrationRequest);

    verify(userRepository, times(1)).save(any(User.class));
  }

  @Test
  void testRegisterUser_EmailAlreadyExists() {
    RegistrationRequest registrationRequest = new RegistrationRequest("dummy", "dummy@example.com", "password");
    User existingUser = new User();
    when(userRepository.findByEmail(registrationRequest.getEmail())).thenReturn(Optional.of(existingUser));

    assertThrows(UserAlreadyExistsException.class, () -> usersService.registerUser(registrationRequest));

    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void testRegisterUser_UsernameAlreadyExists() {
    RegistrationRequest registrationRequest = new RegistrationRequest("dummy", "dummy@example.com", "password");
    User existingUser = new User();
    when(userRepository.findByEmail(registrationRequest.getEmail())).thenReturn(Optional.empty());
    when(userRepository.findByUsername(registrationRequest.getUsername())).thenReturn(Optional.of(existingUser));

    assertThrows(UserAlreadyExistsException.class, () -> usersService.registerUser(registrationRequest));

    verify(userRepository, never()).save(any(User.class));
  }

  @Test
  void testLoginUser_ShouldSuccess() {
    LoginRequest loginRequest = new LoginRequest("dummy", "password");
    User user = new User();
    user.setUsername("dummy");
    user.setPassword("hashed_password");
    user.setId(1L);
    user.setUserRole(UserRoleEnum.CLIENT);

    when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
    when(bCryptPasswordEncoder.matches(anyString(), anyString())).thenReturn(true);

    Map<String, Object> claims = new HashMap<>();
    claims.put("role", user.getUserRole());
    claims.put("name", user.getUsername());

    when(jwtUtil.generateToken(anyMap(), anyString())).thenReturn("jwt_token");

    LoginResponse response = usersService.loginUser(loginRequest);

    assertNotNull(response);
    assertEquals("jwt_token", response.getToken());
  }

  @Test
  void testLoginUser_InvalidUsername() {
    LoginRequest loginRequest = new LoginRequest("dummy", "password");

    when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

    assertThrows(AuthenticationException.class, () -> usersService.loginUser(loginRequest));
  }

  @Test
  void testLoginUser_InvalidPassword() {
    LoginRequest loginRequest = new LoginRequest("dummy", "wrong_password");
    User user = new User();
    user.setUsername("dummy");
    user.setPassword("hashed_password");

    when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(user));
    when(bCryptPasswordEncoder.matches(anyString(), anyString())).thenReturn(false);

    assertThrows(AuthenticationException.class, () -> usersService.loginUser(loginRequest));
  }

  @Test
  void testFindUserById_ShouldSuccess() {
    User user = new User();
    user.setId(1L);

    when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));

    User foundUser = usersService.findUserById(1L);

    assertNotNull(foundUser);
    assertEquals(1L, foundUser.getId());
  }

  @Test
  void testFindUserById_UserNotFound() {
    when(userRepository.findById(anyLong())).thenReturn(Optional.empty());

    assertThrows(UserNotFoundException.class, () -> usersService.findUserById(1L));
  }


  @Test
  void testAdminChangeOtherUserStatus_ShouldSuccess() {
    User user = new User();
    user.setId(2L);
    user.setUserStatus(UserStatusEnum.ACTIVE);

    SecurityUser admin = new SecurityUser(String.valueOf(1L),
        true,
        List.of(new SimpleGrantedAuthority("ADMIN")));

    when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(admin);
    SecurityContextHolder.setContext(securityContext);


    usersService.changeUserStatus(2L);

    verify(userRepository, times(1)).save(any(User.class));
    assertEquals(UserStatusEnum.INACTIVE, user.getUserStatus());
  }


  @Test
  void testAdminChangeTheirStatus_ShouldFail() {
    User user = new User();
    user.setId(2L);
    user.setUserStatus(UserStatusEnum.ACTIVE);

    SecurityUser admin = new SecurityUser(String.valueOf(2L),
        true,
        List.of(new SimpleGrantedAuthority("ADMIN")));

    when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
    when(securityContext.getAuthentication()).thenReturn(authentication);
    when(authentication.getPrincipal()).thenReturn(admin);
    SecurityContextHolder.setContext(securityContext);

    assertThrows(OperationNotAllowedException.class, () -> usersService.changeUserStatus(2L));
  }

  @Test
  void testGetTotalUsers_ShouldSuccess() {
    when(userRepository.countTotalUsersBasedOnRole(any(UserRoleEnum.class), any(OffsetDateTime.class), any(OffsetDateTime.class)))
        .thenReturn(10L);

    Long totalUsers = usersService.getTotalUsers(UserRoleEnum.DEALER, OffsetDateTime.now(), OffsetDateTime.now());

    assertEquals(10L, totalUsers);
  }

  @Test
  void testGetTotalUsersBasedOnStatus_ShouldSuccess() {
    when(userRepository.countTotalUsersBasedOnRoleAndStatus(any(UserRoleEnum.class), any(UserStatusEnum.class), any(OffsetDateTime.class), any(OffsetDateTime.class)))
        .thenReturn(5L);

    Long totalUsers = usersService.getTotalUsersBasedOnStatus(UserRoleEnum.CLIENT, UserStatusEnum.ACTIVE, OffsetDateTime.now(), OffsetDateTime.now());

    assertEquals(5L, totalUsers);
  }

  @Test
  void testGetTotalDealersWithNoProducts_ShouldSuccess() {
    when(userRepository.countDealersWithNoProducts(any(OffsetDateTime.class), any(OffsetDateTime.class)))
        .thenReturn(2L);

    Long totalDealersWithNoProducts = usersService.getTotalDealersWithNoProducts(OffsetDateTime.now(), OffsetDateTime.now());

    assertEquals(2L, totalDealersWithNoProducts);
  }

  @Test
  void testGetTotalDealersWithProducts_ShouldSuccess() {
    when(userRepository.countDealersWithProducts(any(OffsetDateTime.class), any(OffsetDateTime.class)))
        .thenReturn(7L);

    Long totalDealersWithProducts = usersService.getTotalDealersWithProducts(OffsetDateTime.now(), OffsetDateTime.now());

    assertEquals(7L, totalDealersWithProducts);
  }

  @Test
  void testCreateNewAdminOrDealer_ShouldSuccess() {
    UserCreationRequest userCreationRequest = UserCreationRequest.builder()
        .username("dummy_name")
        .email("email@email.com")
        .role(UserCreationRequest.RoleEnum.ADMIN)
        .password("dummy_password")
        .build();

    when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
    when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());
    when(bCryptPasswordEncoder.encode(anyString())).thenReturn("hashed_password");

    usersService.createNewAdminOrDealer(userCreationRequest);

    verify(userRepository, times(1)).save(any(User.class));
  }

}