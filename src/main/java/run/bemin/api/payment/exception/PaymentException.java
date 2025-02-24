package run.bemin.api.payment.exception;

import lombok.Getter;
import run.bemin.api.general.exception.ErrorCode;

@Getter
public class PaymentException extends RuntimeException {
  private final ErrorCode errorCode;

  public PaymentException(ErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  public ErrorCode getErrorCode() {
    return errorCode;
  }
}
