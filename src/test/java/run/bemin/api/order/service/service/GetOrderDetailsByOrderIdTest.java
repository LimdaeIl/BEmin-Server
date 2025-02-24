package run.bemin.api.order.service.service;

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
import run.bemin.api.order.dto.ProductDetailDTO;
import run.bemin.api.order.entity.Order;
import run.bemin.api.order.entity.OrderAddress;
import run.bemin.api.order.entity.OrderDetail;
import run.bemin.api.order.entity.OrderType;
import run.bemin.api.order.exception.OrderNotFoundException;
import run.bemin.api.order.repo.OrderRepository;
import run.bemin.api.order.service.OrderService;
import run.bemin.api.user.entity.User;
import run.bemin.api.user.entity.UserRoleEnum;

@ExtendWith(MockitoExtension.class)
public class GetOrderDetailsByOrderIdTest {

  @Mock
  private OrderRepository orderRepository;

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
        .password("password123")
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
   * 정상적인 orderId로 OrderDetail 리스트를 가져오는 경우
   */
  @Test
  void testGetOrderDetailsByOrderId_Success() {
    //Given
    OrderDetail detail1 = OrderDetail.builder()
        .productId(UUID.randomUUID())
        .productName("아메리카노")
        .price(4000)
        .quantity(2)
        .build();

    OrderDetail detail2 = OrderDetail.builder()
        .productId(UUID.randomUUID())
        .productName("카페라떼")
        .price(4500)
        .quantity(1)
        .build();

    testOrder.addOrderDetail(detail1);
    testOrder.addOrderDetail(detail2);

    when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));

    // When
    List<ProductDetailDTO> result = orderService.getOrderDetailsByOrderId(testOrderId);

    // Then
    assertNotNull(result);
    assertEquals(2, result.size());

    assertEquals("아메리카노", result.get(0).getProductName());
    assertEquals(4000, result.get(0).getPrice());
    assertEquals(2, result.get(0).getQuantity());

    assertEquals("카페라떼", result.get(1).getProductName());
    assertEquals(4500, result.get(1).getPrice());
    assertEquals(1, result.get(1).getQuantity());

    assertEquals(12500, testOrder.getTotalPrice());

    verify(orderRepository, times(1)).findById(testOrderId);
  }

  /**
   * orderId에 해당하는 Order는 있지만, OrderDetail이 없는 경우
   */
  @Test
  void testGetOrderDetailsByOrderId_Empty() {
    // Given
    when(orderRepository.findById(testOrderId)).thenReturn(Optional.of(testOrder));

    // When
    List<ProductDetailDTO> result = orderService.getOrderDetailsByOrderId(testOrderId);

    // Then
    assertNotNull(result);
    assertTrue(result.isEmpty());

    verify(orderRepository, times(1)).findById(testOrderId);
  }

  /**
   * 존재하지 않는 orderId로 조회할 때 예외 발생 검증
   */
  @Test
  void testGetOrderDetailsByOrderId_NotFound() {
    // Given
    when(orderRepository.findById(testOrderId)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(OrderNotFoundException.class, () -> orderService.getOrderDetailsByOrderId(testOrderId));

    verify(orderRepository, times(1)).findById(testOrderId);
  }
}
