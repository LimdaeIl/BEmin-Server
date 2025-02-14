package run.bemin.api.order.entity;

import lombok.Getter;

@Getter
public enum OrderType {
  DELIVERY(1, "배달주문"),
  TAKEOUT(2, "포장주문");

  private final int code;
  private final String description;

  OrderType(int code, String description) {
    this.code = code;
    this.description = description;
  }

  public static OrderType fromCode(int code) {
    for (OrderType type : OrderType.values()) {
      if (type.getCode() == code) {
        return type;
      }
    }
    // 추후 글로벌 익셉션으로...
    throw new IllegalArgumentException("Not available OrderType code: " + code);
  }
}
