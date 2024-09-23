package sa.elm.demo.exception;

import org.springframework.http.HttpStatus;

public class AuthenticationException extends ApplicationBusinessException {
  public AuthenticationException(String message) {
    super(message, HttpStatus.UNAUTHORIZED);
  }
}
