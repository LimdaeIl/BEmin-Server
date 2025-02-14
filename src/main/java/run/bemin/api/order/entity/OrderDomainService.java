package run.bemin.api.order.entity;

import org.springframework.stereotype.Service;

/*
 * 주문 생성 로직
 * 주문 상태 변경
 * 배달기사님 연락처 변경
 * 주문취소 로직
 */
@Service
public class OrderDomainService {

  /**
   * 주문 생성 로직
   */
  public Order createOrder(User user, Store store, OrderType orderType, OrderAddress address) {
    validateOrderCreation(user, store, orderType, address);

    return Order.builder()
        .user(user)
        .store(store)
        .orderType(orderType)
        .orderAddress(address)
        .build();
  }

  /*
    주문상태 업데이트 (dto 구현 후에...)
   */

  /**
   * 주문 취소 로직
   */
  public void cancelOrder(Order order) {
    validateOrderCancellation(order);

    order.setCancelled(true);
    order.setOrderStatus(OrderStatus.CANCELLED);
  }

  /**
   * 주문 생성 검증 로직
   */
  private void validateOrderCreation(User user, Store store, OrderType orderType, OrderAddress address) {
    if (user == null || store == null || orderType == null) {
      throw new IllegalArgumentException("createOrder parameters missing!!");
    }

    if (orderType == OrderType.DELIVERY && address == null) {
      throw new IllegalArgumentException("delivery order's address parameter missing!!");
    }
  }

  /**
   * 주문 취소 검증 로직
   */
  private void validateOrderCancellation(Order order) {
    if (order.getOrderStatus() == OrderStatus.DELIVERY_COMPLETED ||
        order.getOrderStatus() == OrderStatus.TAKEOUT_COMPLETED) {
      throw new IllegalStateException("can not canceled already completed order!!");
    }
  }
}
