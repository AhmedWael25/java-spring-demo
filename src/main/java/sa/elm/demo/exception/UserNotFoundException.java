package sa.elm.demo.exception;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends ApplicationBusinessException {

  public UserNotFoundException(String message) {
    super(message, HttpStatus.NOT_FOUND);
  }

}
