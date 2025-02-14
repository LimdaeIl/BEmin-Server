package run.bemin.api.order.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class OrderStatusConverter implements AttributeConverter<OrderStatus, Integer> {

  @Override
  public Integer convertToDatabaseColumn(OrderStatus orderStatus) {
    if (orderStatus == null) {
      // 추후 exception 적용
      return null;
    }
    return orderStatus.getCode();
  }

  @Override
  public OrderStatus convertToEntityAttribute(Integer integer) {
    if (integer == null) {
      // 추후 exception 적용
      return null;
    }
    return OrderStatus.fromCode(integer);
  }
}
