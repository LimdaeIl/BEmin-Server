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
import run.bemin.api.auth.exception.RefreshTokenInvalidException;
import run.bemin.api.auth.exception.RefreshTokenMismatchException;
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
    log.error("Refresh Token cookie not provided", e);
    final ErrorResponse response = ErrorResponse.of(AUTH_REFRESH_TOKEN_MISSING);
    return new ResponseEntity<>(response, HttpStatus.valueOf(AUTH_REFRESH_TOKEN_MISSING.getStatus()));
  }

  @ExceptionHandler(RefreshTokenInvalidException.class)
  public ResponseEntity<ErrorResponse> handleRefreshTokenInvalidException(RefreshTokenInvalidException e) {
    log.error("Invalid or expired Refresh Token", e);
    final ErrorResponse response = ErrorResponse.of(ErrorCode.AUTH_REFRESH_TOKEN_INVALID);
    return new ResponseEntity<>(response, HttpStatus.valueOf(ErrorCode.AUTH_REFRESH_TOKEN_INVALID.getStatus()));
  }

  @ExceptionHandler(RefreshTokenMismatchException.class)
  public ResponseEntity<ErrorResponse> handleRefreshTokenMismatchException(RefreshTokenMismatchException e) {
    log.error("Stored Refresh Token does not match or has expired", e);
    final ErrorResponse response = ErrorResponse.of(ErrorCode.AUTH_REFRESH_TOKEN_MISMATCH);
    return new ResponseEntity<>(response, HttpStatus.valueOf(ErrorCode.AUTH_REFRESH_TOKEN_MISMATCH.getStatus()));
  }
}
