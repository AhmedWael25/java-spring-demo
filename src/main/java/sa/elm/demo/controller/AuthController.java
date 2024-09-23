package sa.elm.demo.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import sa.elm.api.AuthApi;
import sa.elm.demo.service.UsersService;
import sa.elm.models.LoginRequest;
import sa.elm.models.LoginResponse;
import sa.elm.models.RegistrationRequest;

@Slf4j
@AllArgsConstructor
@RestController
public class AuthController implements AuthApi {

  private final UsersService usersService;

  @Override
  public ResponseEntity<Void> registerUser(RegistrationRequest registrationRequest) {
    usersService.registerUser(registrationRequest);
    return ResponseEntity.ok().build();
  }

  @Override
  public ResponseEntity<LoginResponse> loginUser(LoginRequest loginRequest) {
    LoginResponse loginResponse = usersService.loginUser(loginRequest);
    return ResponseEntity.ok(loginResponse);
  }

}
