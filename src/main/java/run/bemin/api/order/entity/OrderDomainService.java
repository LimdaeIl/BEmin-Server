package run.bemin.api.order.entity;

import run.bemin.api.order.dto.UpdateOrderRequest;

/*
 * 주문 생성 로직
 * 주문 상태 변경
 * 배달기사님 연락처 변경
 * 주문취소 로직
 */
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

  /**
   * 주문상태 업데이트
   */
  public void updateOrder(Order order, UpdateOrderRequest req) {
    // 주소값의 유무를 확인 후, 새 주소 객체 생성
    if (hasAddress(req)) {
      OrderAddress address = OrderAddress.of(
          req.getBcode(),
          req.getJibunAddress(),
          req.getRoadAddress(),
          req.getDetailAddress()
      );
      order.changeOrderAddress(address);
    }

    // 배달기사 전화번호 변경
    if (req.getRiderTel() != null) {
      order.changeRiderTel(req.getRiderTel());
    }

    // 상태 변경
    if (req.getStatusCode() != null) {
      OrderStatus newStatus = OrderStatus.fromCode(req.getStatusCode());

      validateOrderStatusTransition(order.getOrderStatus(), newStatus);

      order.changeOrderStatus(newStatus);
    }
  }

  /**
   * 주문 취소 로직
   */
  public void cancelOrder(Order order) {
    validateOrderCancellation(order);

    order.cancelOrder();
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

  /**
   * 주문 상태 변경 검증 로직
   */
  private void validateOrderStatusTransition(OrderStatus prev, OrderStatus next) {
    if (!prev.canTransitionTo(next)) {
      throw new IllegalStateException("can not transition to " + prev + " to " + next);
    }
  }

  /**
   * 주소 검증 로직
   */
  private boolean hasAddress(UpdateOrderRequest req) {
    return req.getBcode() != null ||
        req.getJibunAddress() != null ||
        req.getRoadAddress() != null ||
        req.getDetailAddress() != null;
  }
}
