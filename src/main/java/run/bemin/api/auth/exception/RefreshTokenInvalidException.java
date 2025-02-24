package run.bemin.api.auth.exception;

public class RefreshTokenInvalidException extends RuntimeException {
  public RefreshTokenInvalidException(String message) {
    super(message);
  }
}
