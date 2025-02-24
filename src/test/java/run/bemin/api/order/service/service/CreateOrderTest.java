package run.bemin.api.order.service.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;
import run.bemin.api.order.dto.ProductDetailDTO;
import run.bemin.api.order.dto.request.CreateOrderRequest;
import run.bemin.api.order.entity.Order;
import run.bemin.api.order.entity.OrderAddress;
import run.bemin.api.order.entity.OrderDomainService;
import run.bemin.api.order.entity.OrderType;
import run.bemin.api.order.repo.OrderRepository;
import run.bemin.api.order.service.OrderService;
import run.bemin.api.user.entity.User;
import run.bemin.api.user.entity.UserRoleEnum;
import run.bemin.api.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
public class CreateOrderTest {

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private UserRepository userRepository;

  @InjectMocks
  private OrderService orderService;

  @Mock
  private OrderDomainService orderDomainService;

  private User testUser;

  @BeforeEach
  void setUp() {
    testUser = User.builder()
        .userEmail("test@test.com")
        .password("aaaaaaaaa")
        .name("qwerty")
        .phone("01000000000")
        .role(UserRoleEnum.CUSTOMER)
        .build();
  }

  @Test
  @Transactional
  void testCreateOrder() {
    String userEmail = "test@test.com";
    CreateOrderRequest req = createOrderRequest();

    // Mock 설정
    when(userRepository.findByUserEmail(userEmail)).thenReturn(Optional.of(testUser));
    when(orderDomainService.createOrder(any(User.class), any(UUID.class), any(OrderType.class), any(String.class), any(OrderAddress.class)))
        .thenAnswer(invocation -> {
          User user = invocation.getArgument(0);
          return Order.builder()
              .user(user)
              .storeId(req.getStoreId())
              .orderType(OrderType.fromCode(req.getOrderType()))
              .storeName(req.getStoreName())
              .orderAddress(req.getAddress())
              .build();
        });
    when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

    // When
    Order result = orderService.createOrder(req, testUser);

    // Then
    assertNotNull(result);

    assertEquals(testUser.getUserEmail(), result.getUser().getUserEmail());
    assertEquals(req.getStoreId(), result.getStoreId());
    assertEquals(req.getStoreName(), result.getStoreName());
    assertFalse(result.getOrderDetails().isEmpty()); // OrderDetails가 비어 있지 않아야 함
    assertEquals(2, result.getOrderDetails().size()); // 2개의 제품이 추가되었는지 확인

    verify(userRepository, times(1)).findByUserEmail(userEmail);
    verify(orderRepository, times(1)).save(any(Order.class));
  }


  private CreateOrderRequest createOrderRequest() {
    return CreateOrderRequest.builder()
        .storeId(UUID.randomUUID())
        .orderType(1)
        .storeName("카페인 addict")
        .address(OrderAddress.of(
            "4145011100",
            "경기 하남시 미사동 609",
            "경기 하남시 미사대로 261-2",
            "302동"
        ))
        .products(List.of(
            ProductDetailDTO.builder()
                .productId(UUID.randomUUID())
                .productName("아메리카노")
                .quantity(3)
                .price(4000)
                .build(),
            ProductDetailDTO.builder()
                .productId(UUID.randomUUID())
                .productName("Latte는 말이야")
                .quantity(2)
                .price(4500)
                .build()
            ))
        .build();
  }
}
