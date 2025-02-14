package run.bemin.api.order.entity;

import lombok.Getter;

@Getter
public enum OrderStatus {
  CANCELLED(0, "주문취소"),
  DELIVERY_ORDER(1, "배달주문"),
  TAKEOUT_ORDER(2, "포장주문"),
  PENDING(10, "주문확인중"),
  COOKING(11, "조리중"),
  OUT_FOR_DELIVERY(20, "배달중"),
  DELIVERY_COMPLETED(21, "배달완료"),
  TAKEOUT_COMPLETED(30, "포장완료"),
  TAKEOUT_HANDOVER_COMPLETED(31, "포장전달완료");

  private final int code;
  private final String desc;

  OrderStatus(int code, String desc) {
    this.code = code;
    this.desc = desc;
  }

  /**
   * @param code OrderStatus enum 코드 입력
   * @return 상태값 반환
   */
  public static OrderStatus fromCode(int code) {
    for (OrderStatus status : values()) {
      if (status.code == code) {
        return status;
      }
    }
    // 추후 globalException 코드 추가
    throw new IllegalArgumentException("Invalid OrderStatus code!! : " + code);
  }

  // 주문상태 전환의 흐름 규칙 정의
  public boolean canTransitionTo(OrderStatus newStatus) {
    return switch (this) {
      case PENDING -> newStatus == COOKING || newStatus == CANCELLED;
      case COOKING -> newStatus == OUT_FOR_DELIVERY || newStatus == TAKEOUT_COMPLETED || newStatus == CANCELLED;
      case OUT_FOR_DELIVERY -> newStatus == DELIVERY_COMPLETED || newStatus == CANCELLED;
      default -> false;
    };
  }
}
