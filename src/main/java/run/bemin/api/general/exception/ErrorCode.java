package run.bemin.api.general.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@Getter
public enum ErrorCode {

  /*
   * 기본 10개, 예외가 많을 것 같다면 20개 단위로 코드 번호 부여
   * 1. Common : C001 ~ C030
   */

  // Common
  INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST.value(), "C001", "잘못된 입력값입니다."),
  METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED.value(), "C002", "요청 메서드가 허용되지 않습니다."),
  ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "C003", "Entity가 존재하지 않습니다."),
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "C004", "서버에서 오류가 발생했습니다."),
  INVALID_TYPE_VALUE(HttpStatus.BAD_REQUEST.value(), "C005", "잘못된 타입입니다."),
  HANDLE_ACCESS_DENIED(HttpStatus.FORBIDDEN.value(), "C006", "권한이 없습니다."),
  INVALID_JSON_FORMAT(HttpStatus.BAD_REQUEST.value(), "C007", "JSON 형식과 맞지 않습니다."),
  INVALID_REQUEST_PARAMETER(HttpStatus.BAD_REQUEST.value(), "C008", "요청 파라미터의 타입이 잘못되었습니다."),
  FAIL_REQUEST_PARAMETER_VALIDATION(HttpStatus.BAD_REQUEST.value(), "C009", "요청 파라미터의 유효성이 맞지 않습니다."),
  BIND_ERROR(HttpStatus.BAD_REQUEST.value(), "C010", "바인딩 에러가 발생했습니다. 파라미터를 확인해주세요."),
  ILLEGAL_ARGUMENT(HttpStatus.BAD_REQUEST.value(), "C011", "illegalArgument error"),
  NOT_FOUND(HttpStatus.NOT_FOUND.value(), "C012", "페이지를 찾을 수 없습니다."),
  INVALID_ACCESS(HttpStatus.UNAUTHORIZED.value(), "C013", "권한이 존재하지 않습니다."),

  // 인증/인가 관련 오류
  AUTH_ACCESS_DENIED(HttpStatus.FORBIDDEN.value(), "A001", "접근이 거부되었습니다."),
  AUTH_REFRESH_TOKEN_MISSING(HttpStatus.BAD_REQUEST.value(), "A002", "Refresh Token 쿠키가 제공되지 않았습니다."),

  // Signup (회원가입 관련 오류)
  SIGNUP_DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST.value(), "S001", "이미 존재하는 이메일입니다."),
  SIGNUP_INVALID_EMAIL_FORMAT(HttpStatus.BAD_REQUEST.value(), "S002", "이메일 형식이 올바르지 않습니다."),
  SIGNUP_DUPLICATE_NICKNAME(HttpStatus.BAD_REQUEST.value(), "S003", "이미 존재하는 닉네임입니다."),
  SIGNUP_INVALID_NICKNAME_FORMAT(HttpStatus.BAD_REQUEST.value(), "S004", "닉네임 형식이 올바르지 않습니다."),
  SIGNUP_EMAIL_NICKNAME_REQUIRED(HttpStatus.BAD_REQUEST.value(), "S005", "이메일과 닉네임은 필수입니다."),


  // Signin (로그인 관련 오류)
  SIGNIN_UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED.value(), "L001", "인증되지 않은 사용자입니다."),
  SIGNIN_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED.value(), "L002", "비밀번호를 입력해주세요."),

  // User (유저 관련 오류)
  USER_PAGE_INDEX_INVALID(HttpStatus.BAD_REQUEST.value(), "U001", "페이지 인덱스는 0보다 작을 수 없습니다."),
  USER_LIST_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "U002", "조회된 사용자가 없습니다."),
  USER_RETRIEVAL_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "U003", "사용자 목록 조회에 실패했습니다."),
  USER_PAGE_SIZE_INVALID(HttpStatus.BAD_REQUEST.value(), "U004", "페이지 크기는 0보다 커야 합니다."),
  USER_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "U005", "해당 이메일의 사용자를 찾을 수 없습니다."),
  USER_DUPLICATE_NICKNAME(HttpStatus.CONFLICT.value(), "U006", "닉네임이 이미 존재합니다."),
  USER_NO_FIELD_UPDATED(HttpStatus.BAD_REQUEST.value(), "U007", "아무것도 변경하지 않았습니다."),
  USER_UNAUTHORIZED(HttpStatus.UNAUTHORIZED.value(), "U008", "인증된 사용자와 요청한 이메일이 일치하지 않습니다."),
  USER_ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "U009", "주소를 찾을 수 없습니다."),


  // Category (카테고리 관련 오류)
  CATEGORY_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "CC001", "해당 카테고리를 찾을 수 없습니다."),
  CATEGORY_ALREADY_EXISTS(HttpStatus.CONFLICT.value(), "CC002", "이미 존재하는 카테고리입니다."),
  CATEGORY_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "CC003", "카테고리 업데이트에 실패했습니다."),
  CATEGORY_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "CC004", "카테고리 삭제에 실패했습니다."),
  CATEGORY_NAME_INVALID(HttpStatus.BAD_REQUEST.value(), "CC005", "카테고리 이름이 유효하지 않습니다."),
  CATEGORY_IS_ACTIVE_INVALID(HttpStatus.BAD_REQUEST.value(), "CC006", "카테고리 활성화 여부가 유효하지 않습니다."),
  CATEGORY_PARENT_INVALID(HttpStatus.BAD_REQUEST.value(), "CC007", "부모 카테고리 ID가 유효하지 않습니다."),
  CATEGORY_DISABLED(HttpStatus.FORBIDDEN.value(), "CC009", "비활성화된 카테리입니다."),
  CATEGORY_ACCESS_DENIED(HttpStatus.FORBIDDEN.value(), "CC010", "카테고리에 대한 권한이 없습니다."),

  // Store (가게 관련 오류)
  STORE_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "ST001", "해당 가게를 찾을 수 없습니다."),
  STORE_ALREADY_EXISTS(HttpStatus.CONFLICT.value(), "ST002", "이미 존재하는 가게입니다."),
  STORE_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "ST003", "가게 정보 업데이트에 실패했습니다."),
  STORE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR.value(), "ST004", "가게 삭제에 실패했습니다."),
  STORE_NAME_INVALID(HttpStatus.BAD_REQUEST.value(), "ST005", "가게 이름이 유효하지 않습니다."),
  STORE_PHONE_INVALID(HttpStatus.BAD_REQUEST.value(), "ST006", "가게 전화번호가 유효하지 않습니다."),
  STORE_MINIMUM_PRICE_INVALID(HttpStatus.BAD_REQUEST.value(), "ST007", "최소 주문 금액이 유효하지 않습니다."),
  STORE_RATING_INVALID(HttpStatus.BAD_REQUEST.value(), "ST008", "가게 평점이 유효하지 않습니다."),
  STORE_ACCESS_DENIED(HttpStatus.FORBIDDEN.value(), "ST009", "가게에 대한 권한이 없습니다."),
  STORE_DISABLED(HttpStatus.FORBIDDEN.value(), "ST010", "비활성화된 가게입니다."),
  STORE_ALREADY_DELETE(HttpStatus.BAD_REQUEST.value(), "STO11", "이미 삭제된 가게입니다."),

  // Product (상품 관련 오류)
  PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "PR001", "상품을 찾을 수 없습니다."),
  DELETED_PRODUCT(HttpStatus.BAD_REQUEST.value(), "PR002", "이미 삭제된 상품입니다."),

  // Payment Error Code (결제 관련 오류)
  ORDER_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "P001", "주문이 존재하지 않습니다."),
  PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "P002", "결제 내역이 존재하지 않습니다."),
  PAYMENT_IS_CANCELED(HttpStatus.NOT_FOUND.value(), "P003", "이미 취소된 결제 내역입니다."),

  // Review (리뷰 관련 오류)
  REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "R001", "해당 리뷰를 찾을 수 없습니다."),
  REVIEW_FORBIDDEN(HttpStatus.FORBIDDEN.value(), "R002", "본인이 작성한 리뷰만 수정할 수 있습니다."),

  // Order (주문 관련 오류)
  ORDER_NOT_FOUND2(HttpStatus.NOT_FOUND.value(), "OR001", "주문이 존재하지 않습니다."),
  ORDER_USER_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "OR002", "유저를 조회할 수 없습니다."),
  ORDER_INVALID_STATUS_CODE(HttpStatus.BAD_REQUEST.value(), "OR003", "잘못된 주문 요청입니다."),
  ORDER_INVALID_TYPE_CODE(HttpStatus.BAD_REQUEST.value(), "OR004", "잘못된 상태 변경 요청입니다."),
  ORDER_NULL_VALUE(HttpStatus.BAD_REQUEST.value(), "OR005", "빈 값이 입력되었습니다."),
  ORDER_CANT_CANCELLED(HttpStatus.BAD_REQUEST.value(), "OR006", "주문을 취소할 수 없습니다."),

  // Image (S3 및 이미지 최적화)
  S3_UPLOAD_FAIL(HttpStatus.INTERNAL_SERVER_ERROR.value(), "IM001", "이미지 업로드에 실패했습니다."),
  S3_INVALID_FORMAT(HttpStatus.BAD_REQUEST.value(), "IM002", "데이터 형식이 올바르지 않습니다.");

  private final int status;
  private final String code;
  private String message;

  ErrorCode(final int status, final String code, final String message) {
    this.status = status;
    this.code = code;
    this.message = message;
  }

  public void updateIllegalArgumentExceptionMessage(final String message) {
    if (this == ILLEGAL_ARGUMENT) {
      this.message = message;
    } else {
      throw new UnsupportedOperationException("Cannot set message for this error code");
    }
  }
}