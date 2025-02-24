package run.bemin.api.order.entity;

import java.util.UUID;
import org.springframework.stereotype.Service;
import run.bemin.api.order.dto.request.UpdateOrderRequest;
import run.bemin.api.order.exception.OrderNullException;
import run.bemin.api.order.exception.OrderStatusException;
import run.bemin.api.security.UserDetailsImpl;
import run.bemin.api.user.entity.User;

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
  public Order createOrder(User user, UUID storeId, OrderType orderType, String storeName, OrderAddress address) {
    validateOrderCreation(user, storeId, orderType, address);

    return Order.builder()
        .user(user)
        .storeId(storeId)
        .orderType(orderType)
        .storeName(storeName)
        .orderAddress(address)
        .build();
  }

  /**
   * 주문상태 업데이트
   */
  public void updateOrder(Order order, UpdateOrderRequest req) {

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


  public void deleteOrder(Order order, UserDetailsImpl user) {
    order.softDelete(user.getUsername());
  }

  /**
   * 주문 생성 검증 로직
   */
  private void validateOrderCreation(User user, UUID storeId, OrderType orderType, OrderAddress address) {
    if (user == null || storeId == null || orderType == null) {
      throw new OrderNullException("createOrder parameters missing!!");
    }

    if (orderType == OrderType.DELIVERY && address == null) {
      throw new OrderNullException("delivery order's address parameter missing!!");
    }
  }

  /**
   * 주문 취소 검증 로직
   */
  private void validateOrderCancellation(Order order) {
    if (order.getOrderStatus() == OrderStatus.DELIVERY_COMPLETED ||
        order.getOrderStatus() == OrderStatus.TAKEOUT_COMPLETED) {
      throw new OrderStatusException("can not canceled already completed order!!");
    }
  }

  /**
   * 주문 상태 변경 검증 로직
   */
  private void validateOrderStatusTransition(OrderStatus prev, OrderStatus next) {
    if (!prev.canTransitionTo(next)) {
      throw new OrderStatusException("can not transition to " + prev + " to " + next);
    }
  }
}
