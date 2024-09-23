package sa.elm.demo.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RestController;
import sa.elm.api.UsersApi;
import sa.elm.demo.service.UsersService;
import sa.elm.models.UserCreationRequest;

@Slf4j
@RequiredArgsConstructor
@RestController("/users")
public class UsersController implements UsersApi {

  private final UsersService usersService;

  @Secured("ADMIN")
  @Override
  public ResponseEntity<String> createUser(UserCreationRequest userCreationRequest) {
    usersService.createNewAdminOrDealer(userCreationRequest);
    return ResponseEntity.ok().build();
  }

  @Secured("ADMIN")
  @Override
  public ResponseEntity<String> changeUserStatus(Long id) {
    usersService.changeUserStatus(id);
    return ResponseEntity.ok().build();
  }
}
