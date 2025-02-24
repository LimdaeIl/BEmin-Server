package run.bemin.api.auth.exception;

public class RefreshTokenMismatchException extends RuntimeException {
  public RefreshTokenMismatchException(String message) {
    super(message);
  }
}
