package run.bemin.api.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateOrderRequest {
  @NotNull
  private Integer statusCode; // 상태코드
  private Integer TypeCode;
  private String riderTel; // 배달기사 전화번호
  private String bcode; // 법정동 코드
  private String jibunAddress; // 지번 주소
  private String roadAddress; // 새 도로명 주소
  private String detailAddress; // 새 상세주소
}
