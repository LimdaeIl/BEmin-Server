package run.bemin.api.order.service.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import run.bemin.api.order.dto.request.CancelOrderRequest;
import run.bemin.api.order.entity.Order;
import run.bemin.api.order.entity.OrderAddress;
import run.bemin.api.order.entity.OrderDomainService;
import run.bemin.api.order.entity.OrderStatus;
import run.bemin.api.order.entity.OrderType;
import run.bemin.api.order.exception.OrderCantCancelled;
import run.bemin.api.order.exception.OrderNotFoundException;
import run.bemin.api.order.repo.OrderRepository;
import run.bemin.api.order.service.OrderService;
import run.bemin.api.user.entity.User;
import run.bemin.api.user.entity.UserRoleEnum;

@ExtendWith(MockitoExtension.class)
public class CancelOrderTest {

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private OrderDomainService orderDomainService;

  @InjectMocks
  private OrderService orderService;

  private UUID testOrderId;
  private Order testOrder;
  private User testUser;

  @BeforeEach
  void setUp() {
    testOrderId = UUID.randomUUID();

    testUser = User.builder()
        .userEmail("test@test.com")
        .password("qwerty123")
        .name("test-name")
        .phone("010-1111-1111")
        .role(UserRoleEnum.CUSTOMER)
        .build();

    testOrder = Order.builder()
        .user(testUser)
        .storeId(UUID.randomUUID())
        .orderType(OrderType.DELIVERY)
        .storeName("카페인 addict")
        .orderAddress(OrderAddress.of(
            "4145011100",
            "경기 하남시 미사동 609",
            "경기 하남시 미사대로 261-2",
            "302동"
        ))
        .build();
    // 주문 ID 강제 설정
    ReflectionTestUtils.setField(testOrder, "orderId", testOrderId);
  }

  /**
   * 주문이 정상적으로 취소되는 경우
   */
  @Test
  void testCancelOrder_Success() {
    // Given
    testOrder.changeOrderStatus(OrderStatus.PENDING);
    CancelOrderRequest req = CancelOrderRequest.builder()
        .orderId(testOrderId).build();

    when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));

    doAnswer(invocation -> {
      Order order = invocation.getArgument(0);
      order.cancelOrder(); // 실제 주문 취소 처리
      return null;
    }).when(orderDomainService).cancelOrder(any(Order.class));

    // When
    orderService.cancelOrder(req);

    // Then
    verify(orderDomainService, times(1)).cancelOrder(any(Order.class));
    verify(orderRepository, times(1)).save(any(Order.class));

    // 추가 검증
    assertTrue(testOrder.getCancelled()); // 취소 여부 확인
    assertEquals(OrderStatus.CANCELLED, testOrder.getOrderStatus()); // 상태 변경 확인
  }


  /**
   * 주문 상태가 10(PENDING)이 아닌 경우 예외 발생
   */
  @Test
  void testCancelOrder_Fail_InvalidStatus() {
    // Given
    testOrder.changeOrderStatus(OrderStatus.OUT_FOR_DELIVERY); // 상태가 10이 아닌걸 설정
    CancelOrderRequest req = CancelOrderRequest.builder()
        .orderId(testOrderId).build();

    when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));

    // When & Then
    assertThrows(OrderCantCancelled.class, () -> orderService.cancelOrder(req));

    verify(orderDomainService, never()).cancelOrder(any());
    verify(orderRepository, never()).save(any());
  }

  /**
   * 존재하지 않는 주문을 취소하려 할 때 예외 발생
   */
  @Test
  void testCancelOrder_Fail_OrderNotFound() {
    // Given
    CancelOrderRequest req = CancelOrderRequest.builder()
        .orderId(testOrderId).build();

    when(orderRepository.findById(testOrderId)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(OrderNotFoundException.class, () -> orderService.cancelOrder(req));

    verify(orderDomainService, never()).cancelOrder(any());
    verify(orderRepository, never()).save(any());
  }
}
