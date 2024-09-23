package sa.elm.demo.exception;

import org.springframework.http.HttpStatus;

public class UserNotActiveException extends ApplicationBusinessException {

  public UserNotActiveException(String message) {
    super(message, HttpStatus.FORBIDDEN);
  }

}
