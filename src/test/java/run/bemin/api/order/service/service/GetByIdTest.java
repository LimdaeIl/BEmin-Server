package run.bemin.api.order.service.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import run.bemin.api.order.dto.response.ReadOrderResponse;
import run.bemin.api.order.entity.Order;
import run.bemin.api.order.entity.OrderAddress;
import run.bemin.api.order.entity.OrderType;
import run.bemin.api.order.exception.OrderNotFoundException;
import run.bemin.api.order.repo.OrderRepository;
import run.bemin.api.order.service.OrderService;
import run.bemin.api.user.entity.User;
import run.bemin.api.user.entity.UserRoleEnum;

@ExtendWith(MockitoExtension.class)
public class GetByIdTest {

  @Mock
  private OrderRepository orderRepository;

  @InjectMocks
  private OrderService orderService;

  private UUID testOrderId;
  private Order testOrder;
  private User testUser;

  /**
   * 테스트용 User, Order 엔티티 생성
   */
  @BeforeEach
  void setUp() {
    testOrderId = UUID.randomUUID();

    testUser = User.builder()
        .userEmail("test@test.com")
        .password("aaaaaaaaa")
        .name("qwerty")
        .phone("01000000000")
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

    // 테스트용으로 orderId및 totalPrice 강제로 설정
    ReflectionTestUtils.setField(testOrder, "orderId", testOrderId);
    ReflectionTestUtils.setField(testOrder, "totalPrice", 15000);
  }

  /**
   * 정상적인 주문 ID를 조회할 때 응답이 올바른 지 테스트
   */
  @Test
  void testGetOrderById_Success() {
    // Given
    when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));

    //When
    ReadOrderResponse response = orderService.getOrderById(testOrderId);

    //Then
    assertNotNull(response);
    assertEquals(testOrder.getOrderId(), response.getOrderId());
    assertEquals(testOrder.getStoreId(), response.getStoreId());
    assertEquals(testOrder.getStoreName(), response.getStoreName());
    assertEquals(testOrder.getOrderType().getCode(), response.getOrderType());
    assertEquals(testOrder.getOrderStatus().getCode(), response.getOrderStatus());
    assertEquals(testOrder.getTotalPrice(), response.getTotalPrice());

    // orderRepository.findById가 한번이라도 호출 되었는지 검증
    verify(orderRepository, times(1)).findById(testOrderId);
  }

  /**
   * 존재하지 않는 주문 ID를 조회할 때 예외 발생 테스트
   */
  @Test
  void testGetOrderById_NotFound() {
    //Given
    when(orderRepository.findById(testOrderId)).thenReturn(Optional.empty());

    //When & Then
    assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(testOrderId));

    // orderRepository.findById()가 한 번 호출되었는지 검증
    verify(orderRepository, times(1)).findById(testOrderId);
  }
}
