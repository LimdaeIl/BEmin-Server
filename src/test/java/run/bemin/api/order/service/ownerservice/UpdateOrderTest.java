package run.bemin.api.order.service.ownerservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import run.bemin.api.order.dto.request.UpdateOrderRequest;
import run.bemin.api.order.entity.Order;
import run.bemin.api.order.entity.OrderDomainService;
import run.bemin.api.order.entity.OrderStatus;
import run.bemin.api.order.entity.OrderType;
import run.bemin.api.order.exception.OrderNotFoundException;
import run.bemin.api.order.exception.OrderStatusException;
import run.bemin.api.order.repo.OrderRepository;
import run.bemin.api.order.service.OrderOwnerService;
import run.bemin.api.security.UserDetailsImpl;
import run.bemin.api.store.entity.Store;
import run.bemin.api.store.repository.StoreRepository;
import run.bemin.api.user.entity.User;
import run.bemin.api.user.entity.UserRoleEnum;

@ExtendWith(MockitoExtension.class)
public class UpdateOrderTest {

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private StoreRepository storeRepository;

  @Mock
  private OrderDomainService orderDomainService;

  @InjectMocks
  private OrderOwnerService orderOwnerService;

  private UUID testOrderId;
  private UUID testStoreId;
  private Order testOrder;
  private Store testStore;
  private User testOwner;
  private UserDetailsImpl testUserDetails;

  @BeforeEach
  void setUp() {
    testOrderId = UUID.randomUUID();
    testStoreId = UUID.randomUUID();

    testOwner = User.builder()
        .userEmail("owner@test.com")
        .password("password1234")
        .name("점주")
        .phone("010-2222-2222")
        .role(UserRoleEnum.OWNER)
        .build();

    testStore = Store.builder()
        .name("테스트 가게")
        .owner(testOwner)
        .build();

    testOrder = Order.builder()
        .user(testOwner)
        .storeId(testStoreId)
        .orderType(OrderType.fromCode(1))
        .storeName("카페인 addict")
        .build();

    ReflectionTestUtils.setField(testOrder, "orderId", testOrderId);
    ReflectionTestUtils.setField(testStore, "id", testStoreId);

    testUserDetails = new UserDetailsImpl(testOwner);
  }

  /**
   * 주문 업데이트가 정상적으로 이루어지는 경우
   * (Success)
   */
  @Test
  void testUpdateOrder_Success() {
    // Given
    UpdateOrderRequest req = UpdateOrderRequest.builder()
        .orderId(testOrderId)
        .riderTel("010-3333-3333")
        .statusCode(OrderStatus.COOKING.getCode())
        .build();

    when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
    when(storeRepository.findByOwner_UserEmail(testOwner.getUserEmail())).thenReturn(List.of(testStore));
    doNothing().when(orderDomainService).updateOrder(any(Order.class), any(UpdateOrderRequest.class));

    // When
    orderOwnerService.updateOrder(req, testUserDetails);

    // Then
    verify(orderDomainService, times(1)).updateOrder(testOrder, req);
    verify(orderRepository, times(1)).save(testOrder);
  }

  /**
   * 중간 단계를 건너뛰어 상태 변경이 실패하는 경우 (PENDING → OUT_FOR_DELIVERY 불가능)
   */
  @Test
  void testUpdateOrder_Failure_InvalidTransition() {
    // Given
    UpdateOrderRequest req = UpdateOrderRequest.builder()
        .orderId(testOrderId)
        .riderTel("010-3333-3333")
        .statusCode(OrderStatus.OUT_FOR_DELIVERY.getCode()) // PENDING → OUT_FOR_DELIVERY (잘못된 상태 변경)
        .build();

    // 주문 상태가 현재 PENDING 상태라고 가정
    testOrder.changeOrderStatus(OrderStatus.PENDING);

    when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
    when(storeRepository.findByOwner_UserEmail(testOwner.getUserEmail())).thenReturn(List.of(testStore));

    // 도메인 서비스가 잘못된 상태 변경을 허용하지 않도록 설정
    doThrow(new OrderStatusException("Invalid order status transition"))
        .when(orderDomainService)
        .updateOrder(any(Order.class), any(UpdateOrderRequest.class));

    // When & Then
    assertThrows(OrderStatusException.class, () -> orderOwnerService.updateOrder(req, testUserDetails));

    // 도메인 서비스가 호출되었으나, 예외 발생
    verify(orderDomainService, times(1)).updateOrder(testOrder, req);

    // 주문이 저장되지 않아야 함
    verify(orderRepository, never()).save(any(Order.class));
  }


  /**
   * 주문이 존재하지 않는 경우 예외 발생
   */
  @Test
  void testUpdateOrder_Fail_OrderNotFound() {
    // Given
    UpdateOrderRequest req = UpdateOrderRequest.builder()
        .orderId(UUID.randomUUID()) // 존재하지 않는 주문 ID
        .riderTel("010-4444-4444")
        .statusCode(OrderStatus.OUT_FOR_DELIVERY.getCode())
        .build();

    when(orderRepository.findById(req.getOrderId())).thenReturn(Optional.empty());

    // When & Then
    assertThrows(OrderNotFoundException.class, () -> orderOwnerService.updateOrder(req, testUserDetails));

    verify(orderDomainService, never()).updateOrder(any(), any());
    verify(orderRepository, never()).save(any());
  }

  /**
   * 점주의 가게가 주문과 일치하지 않는 경우 업데이트되지 않아야 함
   */
  @Test
  void testUpdateOrder_Fail_StoreMismatch() {
    // Given
    UpdateOrderRequest req = UpdateOrderRequest.builder()
        .orderId(testOrderId)
        .riderTel("010-5555-5555")
        .statusCode(OrderStatus.OUT_FOR_DELIVERY.getCode())
        .build();

    Store anotherStore = Store.builder()
        .name("다른 가게")
        .owner(testOwner)
        .build();

    ReflectionTestUtils.setField(anotherStore, "id", UUID.randomUUID());

    when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
    when(storeRepository.findByOwner_UserEmail(testOwner.getUserEmail())).thenReturn(List.of(anotherStore)); // 다른 가게 반환

    // When
    orderOwnerService.updateOrder(req, testUserDetails);

    // Then
    verify(orderDomainService, never()).updateOrder(any(), any());
    verify(orderRepository, never()).save(any());
  }

}
