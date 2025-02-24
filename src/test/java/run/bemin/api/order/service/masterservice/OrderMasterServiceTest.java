package run.bemin.api.order.service.masterservice;

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
import run.bemin.api.order.dto.request.DeleteOrderRequest;
import run.bemin.api.order.entity.Order;
import run.bemin.api.order.entity.OrderDomainService;
import run.bemin.api.order.exception.OrderNotFoundException;
import run.bemin.api.order.repo.OrderRepository;
import run.bemin.api.order.service.OrderMasterService;
import run.bemin.api.security.UserDetailsImpl;

@ExtendWith(MockitoExtension.class)
public class OrderMasterServiceTest {

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private OrderDomainService orderDomainService;

  @InjectMocks
  private OrderMasterService orderMasterService;

  private UUID testOrderId;
  private Order testOrder;
  private UserDetailsImpl testMasterUser;

  @BeforeEach
  void setUp() {
    testOrderId = UUID.randomUUID();

    testOrder = mock(Order.class);

    testMasterUser = mock(UserDetailsImpl.class);
  }

  /**
   * 정상적인 주문 삭제 (소프트 삭제)
   */
  @Test
  void testDeleteOrder_Success() {
    // Given
    DeleteOrderRequest req = DeleteOrderRequest.builder()
        .orderId(testOrderId)
        .build();

    when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));

    // When
    orderMasterService.deleteOrder(req, testMasterUser);

    // Then
    verify(orderDomainService, times(1)).deleteOrder(testOrder, testMasterUser);
    verify(orderRepository, times(1)).save(testOrder);
  }

  /**
   * 존재하지 않는 주문 삭제 시 예외 발생 (OrderNotFoundException)
   */
  @Test
  void testDeleteOrder_Fail_OrderNotFound() {
    // Given
    DeleteOrderRequest req = DeleteOrderRequest.builder()
        .orderId(testOrderId)
        .build();

    when(orderRepository.findById(testOrderId)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(OrderNotFoundException.class, () -> orderMasterService.deleteOrder(req, testMasterUser));

    // 도메인 서비스와 저장 로직이 호출되지 않아야 함
    verify(orderDomainService, never()).deleteOrder(any(), any());
    verify(orderRepository, never()).save(any());
  }
}
