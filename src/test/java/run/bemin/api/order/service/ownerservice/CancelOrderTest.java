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
import run.bemin.api.order.dto.request.CancelOrderRequest;
import run.bemin.api.order.entity.Order;
import run.bemin.api.order.entity.OrderAddress;
import run.bemin.api.order.entity.OrderDomainService;
import run.bemin.api.order.entity.OrderStatus;
import run.bemin.api.order.entity.OrderType;
import run.bemin.api.order.exception.OrderCantCancelled;
import run.bemin.api.order.exception.OrderNotFoundException;
import run.bemin.api.order.repo.OrderRepository;
import run.bemin.api.order.service.OrderOwnerService;
import run.bemin.api.security.UserDetailsImpl;
import run.bemin.api.store.entity.Store;
import run.bemin.api.store.repository.StoreRepository;
import run.bemin.api.user.entity.User;
import run.bemin.api.user.entity.UserRoleEnum;
import run.bemin.api.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class CancelOrderTest {

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private StoreRepository storeRepository;

  @Mock
  private OrderDomainService orderDomainService;

  @InjectMocks
  private OrderOwnerService orderOwnerService;

  private UUID testOrderId;
  private UUID testStoreId;
  private Order testOrder;
  private User testOwner;
  private Store testStore;
  private UserDetailsImpl testUserDetails;

  @BeforeEach
  void setUp() {
    testOrderId = UUID.randomUUID();
    testStoreId = UUID.randomUUID();

    testOwner = User.builder()
        .userEmail("owner@test.com")
        .password("password123")
        .name("Test Owner")
        .phone("010-0000-0000")
        .role(UserRoleEnum.OWNER)
        .build();

    testStore = Store.builder()
        .owner(testOwner)
        .name("Test Store")
        .build();

    testOrder = Order.builder()
        .storeId(testStoreId)
        .user(testOwner)
        .storeName(testStore.getName())
        .orderType(OrderType.DELIVERY)
        .orderAddress(OrderAddress.of(
            "12345",
            "서울 강남구",
            "테헤란로",
            "101호"
        )).build();

    testUserDetails = new UserDetailsImpl(testOwner);

    ReflectionTestUtils.setField(testStore, "id", testStoreId);
  }

  /**
   * 정상적인 주문 취소 (DELIVERY_COMPLETED, TAKEOUT_COMPLETED 상태가 아닌 경우)
   */
  @Test
  void testCancelOrder_Success() {
    //Given
    testOrder.changeOrderStatus(OrderStatus.PENDING);

    CancelOrderRequest req = CancelOrderRequest.builder()
        .orderId(testOrderId)
        .build();

    when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
    when(storeRepository.findByOwner_UserEmail(testUserDetails.getUsername())).thenReturn(List.of(testStore));

    // 도메인 서비스가 정상적으로 호출되도록 설정
    doNothing().when(orderDomainService).cancelOrder(testOrder);

    // When
    orderOwnerService.cancelOrder(req, testUserDetails);

    // Then
    verify(orderDomainService, times(1)).cancelOrder(testOrder);
    verify(orderDomainService, times(1)).cancelOrder(testOrder);
    verify(orderRepository, times(1)).findById(testOrderId);
  }

  /**
   * 이미 완료된 주문 취소 시도 (DELIVERY_COMPLETED, TAKEOUT_COMPLETED 상태일 경우)
   */
  @Test
  void testCancelOrder_Fail_CompletedOrder() {
    // Given
    testOrder.changeOrderStatus(OrderStatus.DELIVERY_COMPLETED); // 이미 배달 완료된 상태

    CancelOrderRequest req = CancelOrderRequest.builder()
        .orderId(testOrderId)
        .build();

    when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));
    when(storeRepository.findByOwner_UserEmail(testOwner.getUserEmail())).thenReturn(List.of(testStore));
    // When & Then
    assertThrows(OrderCantCancelled.class, () -> orderOwnerService.cancelOrder(req, testUserDetails));

    // 주문 취소 로직이 호출되지 않아야 함
    verify(orderDomainService, never()).cancelOrder(any());
    verify(orderRepository, never()).save(any());
  }

  /**
   * 존재하지 않는 주문 ID로 취소 시도
   */
  @Test
  void testCancelOrder_Fail_OrderNotFound() {
    // Given
    CancelOrderRequest req = CancelOrderRequest.builder()
        .orderId(testOrderId)
        .build();

    when(orderRepository.findById(testOrderId)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(OrderNotFoundException.class, () -> orderOwnerService.cancelOrder(req, testUserDetails));

    // 주문 취소 로직이 호출되지 않아야 함
    verify(orderDomainService, never()).cancelOrder(any());
    verify(orderRepository, never()).save(any());
  }
}
