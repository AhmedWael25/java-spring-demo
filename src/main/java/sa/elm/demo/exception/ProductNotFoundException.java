package sa.elm.demo.exception;

import org.springframework.http.HttpStatus;

public class ProductNotFoundException extends ApplicationBusinessException {

  public ProductNotFoundException(String message) {
    super(message, HttpStatus.NOT_FOUND);
  }

}
