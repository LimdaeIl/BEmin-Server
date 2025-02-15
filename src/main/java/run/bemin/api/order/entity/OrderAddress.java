package run.bemin.api.order.entity;

import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class OrderAddress {

  private String bcode; // 법정동 코드
  private String jibunAddress; // 지번 주소
  private String roadAddress; // 도로명 주소
  private String detailAddress; // 상세주소

  /**
   * 주소 입력에 대한 유효성 검사
   */
  public static OrderAddress of(String bcode, String jibunAddress, String roadAddress, String detailAddress) {
    if (bcode == null || jibunAddress == null || roadAddress == null || detailAddress == null) {
      // 추후 글로벌 익셉션 적용
      throw new IllegalArgumentException("OrderAddress's parameters must not be null!!");
    }
    return new OrderAddress(bcode, jibunAddress, roadAddress, detailAddress);
  }


}
