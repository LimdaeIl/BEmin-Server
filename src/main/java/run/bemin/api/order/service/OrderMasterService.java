package run.bemin.api.order.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.bemin.api.order.dto.request.DeleteOrderRequest;
import run.bemin.api.order.entity.Order;
import run.bemin.api.order.entity.OrderDomainService;
import run.bemin.api.order.exception.OrderNotFoundException;
import run.bemin.api.order.repo.OrderRepository;
import run.bemin.api.security.UserDetailsImpl;

@PreAuthorize("hasRole('MASTER')")
@Service
@RequiredArgsConstructor
public class OrderMasterService {

  private final OrderRepository orderRepository;
  private final OrderDomainService orderDomainService;

  /**
   * 주문 소프트 삭제
   * @param deleteOrderRequest 취소할 OrderId
   * @param user 운영자의 아이디
   */
  @Transactional
  public void deleteOrder(DeleteOrderRequest deleteOrderRequest, UserDetailsImpl user) {
    // 1. Order 찾기
    Order order = orderRepository.findById(deleteOrderRequest.getOrderId())
        .orElseThrow(() -> new OrderNotFoundException(deleteOrderRequest.getOrderId()));

    orderDomainService.deleteOrder(order, user);
    orderRepository.save(order);
  }
}
