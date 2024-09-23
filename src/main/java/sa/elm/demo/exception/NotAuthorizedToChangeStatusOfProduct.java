package sa.elm.demo.exception;

import org.springframework.http.HttpStatus;

public class NotAuthorizedToChangeStatusOfProduct extends ApplicationBusinessException {

  public NotAuthorizedToChangeStatusOfProduct(String message) {
    super(message, HttpStatus.FORBIDDEN);
  }

}
