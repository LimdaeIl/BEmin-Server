package run.bemin.api.order.service.ownerservice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
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
import org.springframework.test.util.ReflectionTestUtils;
import run.bemin.api.order.dto.response.PagesResponse;
import run.bemin.api.order.dto.response.ReadOrderResponse;
import run.bemin.api.order.entity.Order;
import run.bemin.api.order.entity.OrderAddress;
import run.bemin.api.order.entity.OrderType;
import run.bemin.api.order.repo.OrderRepository;
import run.bemin.api.order.service.OrderOwnerService;
import run.bemin.api.user.entity.User;
import run.bemin.api.user.entity.UserRoleEnum;

@ExtendWith(MockitoExtension.class)
public class GetOrdersByStoreIdAndDateTest {

  @Mock
  private OrderRepository orderRepository;

  @InjectMocks
  private OrderOwnerService orderOwnerService;

  private User testUser;
  private UUID testStoreId;
  private LocalDate orderCreatedDate;   // 주문이 생성된 날짜 (2024-02-23)
  private LocalDate searchOtherDate;    // 검색할 다른 날짜 (2024-02-22)
  private Order testOrder1;
  private Order testOrder2;

  @BeforeEach
  void setUp() {
    testStoreId = UUID.randomUUID();
    orderCreatedDate = LocalDate.of(2024, 2, 23);  // 주문이 생성된 날짜
    searchOtherDate = LocalDate.of(2024, 2, 22);   // 검색할 날짜 (주문이 없는 날짜)

    testUser = User.builder()
        .userEmail("test@test.com")
        .password("aaaaaaaaa")
        .name("qwerty")
        .phone("01000000000")
        .role(UserRoleEnum.OWNER)
        .build();

    testOrder1 = Order.builder()
        .user(testUser)
        .storeId(testStoreId)
        .storeName("테스트 상점")
        .orderType(OrderType.DELIVERY)
        .orderAddress(OrderAddress.of("4145011100", "경기 하남시 미사동 609", "경기 하남시 미사대로 261-2", "302동"))
        .build();

    testOrder2 = Order.builder()
        .user(testUser)
        .storeId(testStoreId)
        .storeName("테스트 상점")
        .orderType(OrderType.TAKEOUT)
        .orderAddress(OrderAddress.of("4145011100", "서울 강남구 삼성동", "서울 강남구 테헤란로 123", "101호"))
        .build();

    // orderId 강제 설정 (orderId가 null이면, 테스트에서 비교 시 문제 발생 가능)
    ReflectionTestUtils.setField(testOrder1, "orderId", UUID.randomUUID());
    ReflectionTestUtils.setField(testOrder2, "orderId", UUID.randomUUID());
  }

  /**
   * 점주가 자신의 상점에 들어온 주문을,
   * 날짜로 검색하는 기능 테스트
   * 주문 날짜와 검색 날짜를 orderCreatedDate 로 하여 같게 하는 경우
   * (성공)
   */
  @Test
  void testGetOrderByStoreIdAndDate_Success() {
    // Given
    int page = 0;
    int size = 10;
    String sortOrder = "asc";
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());

    List<Order> orderList = List.of(testOrder1, testOrder2);
    Page<Order> orderPage = new PageImpl<>(orderList, pageable, orderList.size());

    when(orderRepository.findByStoreIdAndOrderDate(testStoreId, orderCreatedDate, pageable)).thenReturn(orderPage);

    // When
    PagesResponse<ReadOrderResponse> res = orderOwnerService.getOrdersByStoreIdAndDate(testStoreId, orderCreatedDate, page, size, sortOrder);

    // Then
    assertNotNull(res);
    assertFalse(res.getData().isEmpty());
    assertEquals(2, res.getData().size());

    assertEquals(testOrder1.getOrderId(), res.getData().get(0).getOrderId());
    assertEquals(testOrder1.getStoreName(), res.getData().get(0).getStoreName());

    assertEquals(testOrder2.getOrderId(), res.getData().get(1).getOrderId());
    assertEquals(testOrder2.getStoreName(), res.getData().get(1).getStoreName());

    verify(orderRepository, times(1)).findByStoreIdAndOrderDate(testStoreId, orderCreatedDate, pageable);
  }

  /**
   * 해당 날짜에 주문이 없는 경우, 빈 결과를 반환해야 한다.
   */
  @Test
  void testGetOrderByStoreIdAndDate_Empty() {
    // Given
    int page = 0;
    int size = 10;
    String sortOrder = "asc";
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());

    Page<Order> emptyPage = Page.empty();

    when(orderRepository.findByStoreIdAndOrderDate(testStoreId, searchOtherDate, pageable)).thenReturn(emptyPage);

    // When
    PagesResponse<ReadOrderResponse> res = orderOwnerService.getOrdersByStoreIdAndDate(testStoreId, searchOtherDate, page, size, sortOrder);

    // Then
    assertNotNull(res);
    assertTrue(res.getData().isEmpty());
    assertEquals(0, res.getTotalElements());

    verify(orderRepository, times(1)).findByStoreIdAndOrderDate(testStoreId, searchOtherDate, pageable);
  }

  /**
   * 생성 날짜와 검색 날짜가 다른 경우
   * 2024-02-23 주문 생성
   * 2024-02-22 검색 -> 주문이 없어야 한다!
   */
  @Test
  void testGetOrdersByStoreIdAndWrongDate_Empty_AfterSavingOrders() {
    // Given
    int page = 0;
    int size = 10;
    String sortOrder = "asc";
    Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending());

    //  주문이 실제로 존재하는 상태를 만들기
    List<Order> orderList = List.of(testOrder1, testOrder2);
    Page<Order> orderPage = new PageImpl<>(orderList, pageable, orderList.size());

    when(orderRepository.findByStoreIdAndOrderDate(testStoreId, orderCreatedDate, pageable)).thenReturn(orderPage);
    when(orderRepository.findByStoreIdAndOrderDate(testStoreId, searchOtherDate, pageable)).thenReturn(Page.empty()); // 검색 날짜에는 주문 없음

    //  1. 실제 주문이 있는 날짜 검색 (2024-02-23)
    PagesResponse<ReadOrderResponse> res1 = orderOwnerService.getOrdersByStoreIdAndDate(testStoreId, orderCreatedDate, page, size, sortOrder);
    assertNotNull(res1);
    assertFalse(res1.getData().isEmpty());  // 주문이 있어야 함
    assertEquals(2, res1.getTotalElements()); // 주문 2개 확인

    // 2. 존재하지 않는 날짜 검색 (2024-02-22)
    PagesResponse<ReadOrderResponse> res2 = orderOwnerService.getOrdersByStoreIdAndDate(testStoreId, searchOtherDate, page, size, sortOrder);
    assertNotNull(res2);
    assertTrue(res2.getData().isEmpty());  // 주문이 없어야 함
    assertEquals(0, res2.getTotalElements()); // 총 주문 개수 0 확인

    //  Repository 호출 검증
    verify(orderRepository, times(1)).findByStoreIdAndOrderDate(testStoreId, orderCreatedDate, pageable);
    verify(orderRepository, times(1)).findByStoreIdAndOrderDate(testStoreId, searchOtherDate, pageable);
  }
}
