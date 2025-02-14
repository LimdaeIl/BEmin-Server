package run.bemin.api.order.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class OrderTypeConverter implements AttributeConverter<OrderType, Integer> {

  @Override
  public Integer convertToDatabaseColumn(OrderType orderType) {
    if (orderType == null) {
      // 추후 exception 설정
      return null;
    }
    return orderType.getCode();
  }

  @Override
  public OrderType convertToEntityAttribute(Integer code) {
    if (code == null) {
      // 추후 exception 설정
      return null;
    }
    return OrderType.fromCode(code);
  }
}
