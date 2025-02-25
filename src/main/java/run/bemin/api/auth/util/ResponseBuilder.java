package run.bemin.api.auth.util;

import static org.springframework.http.HttpStatus.OK;

import org.springframework.http.ResponseEntity;
import run.bemin.api.general.response.ApiResponse;

public class ResponseBuilder {

  /**
   * 공통 응답 생성 메서드
   */
  public static <T> ResponseEntity<ApiResponse<T>> buildResponse(String headerValue, String message, T data,
                                                                 String headerKey) {
    return ResponseEntity.ok()
        .header(headerKey, headerValue)
        .body(ApiResponse.from(OK, message, data));
  }
}
