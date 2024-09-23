package sa.elm.demo.controller.advice;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import sa.elm.demo.exception.ApplicationBusinessException;
import sa.elm.models.ErrorResponse;

@RestControllerAdvice
public class ControllerAdvice {

  @ExceptionHandler(ApplicationBusinessException.class)
  public ResponseEntity<ErrorResponse> handleApplicationBusinessException(ApplicationBusinessException ex) {
    ErrorResponse errorResponse = ErrorResponse.builder().error(ex.getMessage()).build();
    return new ResponseEntity<>(errorResponse, ex.getErrorCode());
  }
}
