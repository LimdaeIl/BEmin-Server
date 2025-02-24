package run.bemin.api.order.service.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import run.bemin.api.order.dto.response.PagesResponse;
import run.bemin.api.order.dto.response.ReadOrderResponse;
import run.bemin.api.order.entity.Order;
import run.bemin.api.order.entity.OrderAddress;
import run.bemin.api.order.entity.OrderType;
import run.bemin.api.order.repo.OrderRepository;
import run.bemin.api.order.service.OrderService;
import run.bemin.api.user.entity.User;
import run.bemin.api.user.entity.UserRoleEnum;

@ExtendWith(MockitoExtension.class)
public class GetOrderByUserEmailTest {

  @Mock
  private OrderRepository orderRepository;

  @InjectMocks
  private OrderService orderService;

  private String testUserEmail;
  private Order testOrder;
  private User testUser;

  @BeforeEach
  void setUp() {
    testUserEmail = "test@test.com";

    testUser = User.builder()
        .userEmail(testUserEmail)
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
  }

  /**
   * 사용자 주문 내역이 정상 반환되는지 검증
   */
  @Test
  void testGetOrderByUserEmail_Success() {
    //Given
    int page = 0;
    int size = 10;
    String sortOrder = "asc";
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());

    List<Order> orderList = List.of(testOrder);
    Page<Order> orderPage = new PageImpl<>(orderList, pageable, orderList.size());

    when(orderRepository.findAllByUser_UserEmail(testUserEmail, pageable)).thenReturn(orderPage);

    // when
    PagesResponse<ReadOrderResponse> res = orderService.getOrdersByUserEmail(testUserEmail, page, size, sortOrder);

    // Then
    assertNotNull(res);
    assertFalse(res.getData().isEmpty());
    assertEquals(1, res.getData().size());
    assertEquals(testOrder.getOrderId(), res.getData().get(0).getOrderId());
  }

  /**
   * 사용자 주문 내역이 없는 경우 빈 데이터가 반환되는지 검증
   */
  @Test
  void testGetOrderByUserEmail_Empty() {
    //Given
    int page = 0;
    int size = 10;
    String sortOrder = "asc";
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());

    Page<Order> emptyPage = Page.empty();

    when(orderRepository.findAllByUser_UserEmail(testUserEmail, pageable)).thenReturn(emptyPage);

    // When
    PagesResponse<ReadOrderResponse> res = orderService.getOrdersByUserEmail(testUserEmail, page, size, sortOrder);

    // Then
    assertNotNull(res);
    assertTrue(res.getData().isEmpty());
    assertEquals(0, res.getTotalElements());

    // orderRepository.findAllByUser_UserEmail()이 한 번 호출되었는지 검증
    verify(orderRepository, times(1)).findAllByUser_UserEmail(testUserEmail, pageable);
  }

  /**
   * 페이징이 올바르게 동작하는지 검증
   */
  @Test
  void testGetOrdersByUserEmail_Pagination() {
    // Given
    int page = 1;
    int size = 5;
    String sortOrder = "desc";
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

    List<Order> orderList = List.of(testOrder, testOrder, testOrder, testOrder);
    Page<Order> orderPage = new PageImpl<>(orderList, pageable, orderList.size());

    when(orderRepository.findAllByUser_UserEmail(testUserEmail, pageable)).thenReturn(orderPage);

    // When
    PagesResponse<ReadOrderResponse> res = orderService.getOrdersByUserEmail(testUserEmail, page, size, sortOrder);

    // Then
    assertNotNull(res);
    assertEquals(4, res.getData().size());
    assertEquals(page, res.getPageNumber());
    assertEquals(size, res.getPageSize());

    // orderRepository.findAllByUser_UserEmail()이 한 번 호출되었는지 검증
    verify(orderRepository, times(1)).findAllByUser_UserEmail(testUserEmail, pageable);
  }
}
