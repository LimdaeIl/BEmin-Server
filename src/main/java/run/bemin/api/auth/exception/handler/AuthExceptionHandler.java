package run.bemin.api.auth.exception.handler;

import static run.bemin.api.general.exception.ErrorCode.AUTH_REFRESH_TOKEN_MISSING;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import run.bemin.api.auth.exception.AuthAccessDeniedException;
import run.bemin.api.auth.exception.RefreshTokenMissingException;
import run.bemin.api.general.exception.ErrorCode;
import run.bemin.api.general.exception.ErrorResponse;

@Order(1)
@Slf4j
@RestControllerAdvice
public class AuthExceptionHandler {

  @ExceptionHandler({AuthAccessDeniedException.class, AccessDeniedException.class})
  public ResponseEntity<ErrorResponse> handleAccessDeniedExceptions(Exception e) {
    log.error("Access denied for authentication", e);
    final ErrorResponse response = ErrorResponse.of(ErrorCode.AUTH_ACCESS_DENIED);
    return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler(RefreshTokenMissingException.class)
  public ResponseEntity<ErrorResponse> handleRefreshTokenMissingException(RefreshTokenMissingException e) {
    log.error("Refresh Token 쿠키 미제공", e);
    final ErrorResponse response = ErrorResponse.of(AUTH_REFRESH_TOKEN_MISSING);
    return new ResponseEntity<>(response, HttpStatus.valueOf(AUTH_REFRESH_TOKEN_MISSING.getStatus()));
  }
}
