package sa.elm.demo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sa.elm.demo.exception.*;
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
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class UsersService {

  private final JwtUtil jwtUtil;
  private final UserRepository userRepository;
  private final PasswordEncoder bCryptPasswordEncoder;

  public void registerUser(RegistrationRequest registrationRequest) {

    checkIfUserExistBefore(registrationRequest.getEmail(), registrationRequest.getUsername());

    String encodedPassword = hashPassword(registrationRequest.getPassword());

    User user = User.builder()
        .email(registrationRequest.getEmail())
        .username(registrationRequest.getUsername())
        .password(encodedPassword)
        .userStatus(UserStatusEnum.ACTIVE)
        .userRole(UserRoleEnum.CLIENT)
        .build();
    userRepository.save(user);
  }

  private String hashPassword(String password) {
    return bCryptPasswordEncoder.encode(password);
  }

  public LoginResponse loginUser(LoginRequest loginRequest) {

    Optional<User> userOptional = userRepository.findByUsername(loginRequest.getUsername());
    if (userOptional.isEmpty()) {
      throw new AuthenticationException("Username or Password wrong");
    }
    User user = userOptional.get();
    if (!bCryptPasswordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
      throw new AuthenticationException("Username or Password wrong");
    }
    if (user.getUserStatus() == UserStatusEnum.INACTIVE) {
      throw new UserNotActiveException("User Is Not Active");
    }
    Map<String, Object> claims = new HashMap<>();
    claims.put("role", user.getUserRole());
    claims.put("name", user.getUsername());
    String jwt = jwtUtil.generateToken(claims, String.valueOf(user.getId()));

    return LoginResponse.builder().token(jwt).build();
  }

  public void createNewAdminOrDealer(UserCreationRequest userCreationRequest) {

    checkIfUserExistBefore(userCreationRequest.getEmail(), userCreationRequest.getUsername());

    String encodedPassword = hashPassword(userCreationRequest.getPassword());

    User user = User.builder()
        .email(userCreationRequest.getEmail())
        .username(userCreationRequest.getUsername())
        .password(encodedPassword)
        .userStatus(UserStatusEnum.ACTIVE)
        .userRole(userCreationRequest.getRole() == UserCreationRequest.RoleEnum.ADMIN ? UserRoleEnum.ADMIN : UserRoleEnum.DEALER)
        .build();
    userRepository.save(user);
  }

  private void checkIfUserExistBefore(String email, String username) {
    Optional<User> optionalUserByEmail = userRepository.findByEmail(email);
    if (optionalUserByEmail.isPresent()) {
      throw new UserAlreadyExistsException("Email Already Exist");
    }
    Optional<User> optionalUserByUsername = userRepository.findByUsername(username);
    if (optionalUserByUsername.isPresent()) {
      throw new UserAlreadyExistsException("Username Already Exist");
    }
  }

  public User findUserById(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> {
          log.error("User with Id:{} Not Found", id);
          return new UserNotFoundException("User Not Found");
        });
  }

  public void changeUserStatus(Long id) {

    User user = findUserById(id);

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    SecurityUser principal = (SecurityUser) authentication.getPrincipal();
    Long loggedInAdminId = principal.getId();

    if (loggedInAdminId.equals(user.getId())) {
      throw new OperationNotAllowedException("Admin Cannot Change their own status");
    }

    if (user.getUserStatus() == UserStatusEnum.ACTIVE) {
      user.setUserStatus(UserStatusEnum.INACTIVE);
    } else {
      user.setUserStatus(UserStatusEnum.ACTIVE);
    }
    userRepository.save(user);
  }

  public Long getTotalUsers(UserRoleEnum roleEnum, OffsetDateTime from, OffsetDateTime to) {
    return userRepository.countTotalUsersBasedOnRole(roleEnum, from, to);
  }

  public Long getTotalUsersBasedOnStatus(UserRoleEnum userRoleEnum, UserStatusEnum userStatusEnum, OffsetDateTime from, OffsetDateTime to) {
    return userRepository.countTotalUsersBasedOnRoleAndStatus(userRoleEnum, userStatusEnum, from, to);
  }

  public Long getTotalDealersWithNoProducts(OffsetDateTime from, OffsetDateTime to) {
    return userRepository.countDealersWithNoProducts(from, to);
  }

  public Long getTotalDealersWithProducts(OffsetDateTime from, OffsetDateTime to) {
    return userRepository.countDealersWithProducts(from, to);
  }


}
