package run.bemin.api.order.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.bemin.api.order.dto.request.CancelOrderRequest;
import run.bemin.api.order.dto.request.CreateOrderRequest;
import run.bemin.api.order.dto.response.PagesResponse;
import run.bemin.api.order.dto.ProductDetailDTO;
import run.bemin.api.order.dto.response.ReadOrderResponse;
import run.bemin.api.order.dto.request.UpdateOrderRequest;
import run.bemin.api.order.entity.Order;
import run.bemin.api.order.entity.OrderAddress;
import run.bemin.api.order.entity.OrderDetail;
import run.bemin.api.order.entity.OrderDomainService;
import run.bemin.api.order.entity.OrderType;
import run.bemin.api.order.exception.OrderCantCancelled;
import run.bemin.api.order.exception.OrderNotFoundException;
import run.bemin.api.order.repo.OrderDetailRepository;
import run.bemin.api.order.repo.OrderRepository;
import run.bemin.api.user.entity.User;
import run.bemin.api.user.exception.UserNotFoundException;
import run.bemin.api.user.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class OrderService {

  private final OrderRepository orderRepository;
  private final OrderDetailRepository orderDetailRepository;
  private final OrderDomainService orderDomainService;
  private final UserRepository userRepository;

  /**
   * 주문 생성
   */
  @Transactional
  public Order createOrder(CreateOrderRequest req, User user) {
    // User 조회
    User foundUser = userRepository.findByUserEmail(user.getUserEmail())
        .orElseThrow(() -> new UserNotFoundException(user.getUserEmail()));

    // 1. OrderAddress 생성
    OrderAddress orderAddress = req.getAddress();

    // 2. OrderType 매핑
    OrderType orderType = OrderType.fromCode(req.getOrderType());

    // 3. 도메인 서비스로 검증 및 주문 객체 생성
    Order order = orderDomainService.createOrder(
        foundUser,
        req.getStoreId(),
        orderType,
        req.getStoreName(),
        orderAddress
    );

    // 4. OrderDetail 추가
    req.getProducts().stream()
        .map(product -> OrderDetail.builder()
            .productId(product.getProductId())
            .productName(product.getProductName())
            .quantity(product.getQuantity())
            .price(product.getPrice())
            .build())
        .forEach(order::addOrderDetail); // 연관 관계 설정

    // 4. Order 저장 (CascadeType.ALL로 OrderDetails들도 저장)
    return orderRepository.save(order);
  }

  /**
   * 하나의 주문 내역 조회
   */
  @Transactional(readOnly = true)
  public ReadOrderResponse getOrderById(UUID orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));
    return ReadOrderResponse.builder()
        .orderId(order.getOrderId())
        .storeId(order.getStoreId())
        .storeName(order.getStoreName())
        .orderType(order.getOrderType().getCode())
        .orderStatus(order.getOrderStatus().getCode())
        .orderAddress(order.getOrderAddress())
        .cancelled(order.getCancelled())
        .createdAt(order.getCreatedAt())
        .totalPrice(order.getTotalPrice())
        .build();
  }

  /**
   * 사용자의 주문 내역 조회(페이징 처리)
   *
   * @param userEmail 사용자 EMAIL (JWT에서 추출된 PK)
   * @param page   조회할 페이지 번호(0부터 시작)
   * @param size   한 페이지에 담겨지는 데이터의 갯수
   * @return 페이징 처리되어 반환되는 주문 목록
   */
  @Transactional(readOnly = true)
  public PagesResponse<ReadOrderResponse> getOrdersByUserEmail(String userEmail, int page, int size, String sortOrder) {
    Sort sort = sortOrder.equals("asc") ? Sort.by("createdAt").ascending() : Sort.by("createdAt").descending();
    Pageable pageable = PageRequest.of(page, size, sort);
    Page<Order> orders = orderRepository.findAllByUser_UserEmail(userEmail, pageable);

    // Order -> OrderResponse 변환
    List<ReadOrderResponse> data = orders.getContent().stream()
        .map(order -> ReadOrderResponse.builder()
            .orderId(order.getOrderId())
            .storeId(order.getStoreId())
            .storeName(order.getStoreName())
            .orderType(order.getOrderType().getCode())
            .orderStatus(order.getOrderStatus().getCode())
            .orderAddress(order.getOrderAddress())
            .cancelled(order.getCancelled())
            .createdAt(order.getCreatedAt())
            .totalPrice(order.getTotalPrice())
            .build())
        .toList();

    // PagesResponse 생성 및 변환
    return PagesResponse.<ReadOrderResponse>builder()
        .data(data)
        .pageNumber(orders.getNumber())
        .pageSize(orders.getSize())
        .totalPages(orders.getTotalPages())
        .totalElements(orders.getTotalElements())
        .build();
  }

  /**
   * orderId로 OrderDetail 들을 조회하여 List 로 반환
   * @param orderId // orderId
   * @return // ProductDetailDTO List
   */
  public List<ProductDetailDTO> getOrderDetailsByOrderId(UUID orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new OrderNotFoundException(orderId));

    return order.getOrderDetails().stream()
        .map(orderDetail -> ProductDetailDTO.builder()
            .productId(orderDetail.getProductId())
            .productName(orderDetail.getProductName())
            .quantity(orderDetail.getQuantity())
            .price(orderDetail.getPrice())
            .build())
        .toList();
  }

  /**
   * 사용자의 주문 취소
   * @param req // orderId
   */
  @Transactional
  public void cancelOrder(CancelOrderRequest req) {
    // 1. Order 객체 찾기
    Order order = orderRepository.findById(req.getOrderId())
        .orElseThrow(() -> new OrderNotFoundException(req.getOrderId()));

    // 2. 비즈니스 로직 실행 - 고객은 주문 확인 중(10)일 때만 취소 가능
    if (order.getOrderStatus().getCode() == 10) {
      orderDomainService.cancelOrder(order);
      orderRepository.save(order); // 변경된 주문 상태 저장
    } else {
      throw new OrderCantCancelled("Order Cancellation Not Allowed");
    }
  }

}
