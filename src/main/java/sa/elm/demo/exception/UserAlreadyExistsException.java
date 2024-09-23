package sa.elm.demo.exception;

import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends ApplicationBusinessException {

  public UserAlreadyExistsException(String message) {
    super(message, HttpStatus.BAD_REQUEST);
  }
}