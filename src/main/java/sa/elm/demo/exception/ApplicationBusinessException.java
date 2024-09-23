package sa.elm.demo.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApplicationBusinessException extends RuntimeException {

  private final HttpStatus errorCode;

  public ApplicationBusinessException(String message, HttpStatus errorCode) {
    super(message);
    this.errorCode = errorCode;
  }
}