package sa.elm.demo.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import sa.elm.demo.service.UsersService;
import sa.elm.models.LoginRequest;
import sa.elm.models.LoginResponse;
import sa.elm.models.RegistrationRequest;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthControllerTest {

  @Mock
  private UsersService usersService;

  @InjectMocks
  private AuthController authController;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testRegisterUser_ShouldSuccess() {
    RegistrationRequest registrationRequest = new RegistrationRequest();
    registrationRequest.setEmail("dummy@example.com");
    registrationRequest.setUsername("dummy");
    registrationRequest.setPassword("password");

    doNothing().when(usersService).registerUser(any(RegistrationRequest.class));

    ResponseEntity<Void> response = authController.registerUser(registrationRequest);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    verify(usersService, times(1)).registerUser(any(RegistrationRequest.class));
  }

  @Test
  void testLoginUser_ShouldSuccess() {
    LoginRequest loginRequest = new LoginRequest();
    loginRequest.setUsername("dummy");
    loginRequest.setPassword("password");

    LoginResponse loginResponse = LoginResponse.builder().build();
    loginResponse.setToken("mocked_token");

    when(usersService.loginUser(any(LoginRequest.class))).thenReturn(loginResponse);

    ResponseEntity<LoginResponse> response = authController.loginUser(loginRequest);

    assertEquals(HttpStatus.OK, response.getStatusCode());
    assertEquals("mocked_token", Objects.requireNonNull(response.getBody()).getToken());
    verify(usersService, times(1)).loginUser(any(LoginRequest.class));
  }
}