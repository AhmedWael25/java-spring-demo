package sa.elm.demo.exception;

import org.springframework.http.HttpStatus;

public class OperationNotAllowedException extends ApplicationBusinessException {
  public OperationNotAllowedException(String message) {
    super(message, HttpStatus.CONFLICT);
  }
}
