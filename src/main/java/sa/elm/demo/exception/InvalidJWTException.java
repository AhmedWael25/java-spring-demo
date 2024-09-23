package sa.elm.demo.exception;

import org.springframework.security.core.AuthenticationException;

public class InvalidJWTException extends AuthenticationException {
  public InvalidJWTException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public InvalidJWTException(String msg) {
    super(msg);
  }
}
