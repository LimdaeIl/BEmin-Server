package run.bemin.api.auth.exception.handler;

import static run.bemin.api.general.exception.ErrorCode.FAIL_REQUEST_PARAMETER_VALIDATION;
import static run.bemin.api.general.exception.ErrorCode.SIGNIN_INVALID_CREDENTIALS;
import static run.bemin.api.general.exception.ErrorCode.SIGNIN_UNAUTHORIZED_USER;
import static run.bemin.api.general.exception.ErrorCode.SIGNUP_INVALID_EMAIL_FORMAT;

import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import run.bemin.api.auth.controller.SessionController;
import run.bemin.api.auth.exception.SigninInvalidCredentialsException;
import run.bemin.api.auth.exception.SigninUnauthorizedException;
import run.bemin.api.general.exception.ErrorResponse;

@Order(1)
@Slf4j
@RestControllerAdvice(assignableTypes = SessionController.class)
public class SigninExceptionHandler {

  @ExceptionHandler(SigninUnauthorizedException.class)
  public ResponseEntity<ErrorResponse> handleSigninUnauthorizedException(SigninUnauthorizedException e) {
    log.error("Signin unauthorized", e);
    final ErrorResponse response = ErrorResponse.of(SIGNIN_UNAUTHORIZED_USER);
    return new ResponseEntity<>(response, HttpStatus.valueOf(SIGNIN_UNAUTHORIZED_USER.getStatus()));
  }

  @ExceptionHandler(SigninInvalidCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleSigninInvalidCredentialsException(SigninInvalidCredentialsException e) {
    log.error("Signin invalid credentials", e);
    final ErrorResponse response = ErrorResponse.of(SIGNIN_INVALID_CREDENTIALS);
    return new ResponseEntity<>(response, HttpStatus.valueOf(SIGNIN_INVALID_CREDENTIALS.getStatus()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleSigninValidation(MethodArgumentNotValidException e) {
    log.error("Signin validation error", e);
    List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();

    for (FieldError fieldError : fieldErrors) {

      if ("userEmail".equals(fieldError.getField())) {
        final ErrorResponse response = ErrorResponse.of(SIGNUP_INVALID_EMAIL_FORMAT);
        return new ResponseEntity<>(response, HttpStatus.valueOf(SIGNUP_INVALID_EMAIL_FORMAT.getStatus()));
      }

      if ("password".equals(fieldError.getField())) {
        final ErrorResponse response = ErrorResponse.of(SIGNIN_INVALID_CREDENTIALS);
        return new ResponseEntity<>(response, HttpStatus.valueOf(SIGNIN_INVALID_CREDENTIALS.getStatus()));
      }
    }

    final ErrorResponse response = ErrorResponse.of(FAIL_REQUEST_PARAMETER_VALIDATION);
    return new ResponseEntity<>(response, HttpStatus.valueOf(FAIL_REQUEST_PARAMETER_VALIDATION.getStatus()));
  }
}
