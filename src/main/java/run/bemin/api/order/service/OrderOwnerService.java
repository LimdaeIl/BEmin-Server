package run.bemin.api.order.service;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.bemin.api.order.dto.request.CancelOrderRequest;
import run.bemin.api.order.dto.request.UpdateOrderRequest;
import run.bemin.api.order.dto.response.PagesResponse;
import run.bemin.api.order.dto.response.ReadOrderResponse;
import run.bemin.api.order.entity.Order;
import run.bemin.api.order.entity.OrderDomainService;
import run.bemin.api.order.entity.OrderStatus;
import run.bemin.api.order.exception.OrderCantCancelled;
import run.bemin.api.order.exception.OrderNotFoundException;
import run.bemin.api.order.repo.OrderDetailRepository;
import run.bemin.api.order.repo.OrderRepository;
import run.bemin.api.security.UserDetailsImpl;
import run.bemin.api.store.entity.Store;
import run.bemin.api.store.repository.StoreRepository;

@PreAuthorize("hasRole('OWNER')")
@Service
@RequiredArgsConstructor
public class OrderOwnerService {

  private final OrderRepository orderRepository;
  private final OrderDetailRepository orderDetailRepository;
  private final StoreRepository storeRepository;
  private final OrderDomainService orderDomainService;

  /**
   * 점주가 자신의 상점에 들어온 주문을, 날짜로 검색 할 수 있게 만듬
   *
   * @param storeId // 스토어 ID
   * @param date // 날짜
   * @param page // 페이지 지정
   * @param size // 페이지 사이즈
   * @param sortOrder // 주문 시간순서 오름차, 내림차
   * @return // 주문서 response 들을 List 형식으로 반환
   */
  @Transactional(readOnly = true)
  public PagesResponse<ReadOrderResponse> getOrdersByStoreIdAndDate(UUID storeId, LocalDate date, int page, int size, String sortOrder) {
    Sort sort = sortOrder.equals("asc") ? Sort.by("createdAt").ascending() : Sort.by("createdAt").descending();
    Pageable pageable = PageRequest.of(page, size, sort);

    Page<Order> orders = orderRepository.findByStoreIdAndOrderDate(storeId, date, pageable);

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

    return PagesResponse.<ReadOrderResponse>builder()
        .data(data)
        .pageNumber(orders.getNumber())
        .pageSize(orders.getSize())
        .totalPages(orders.getTotalPages())
        .totalElements(orders.getTotalElements())
        .build();
  }

  /**
   * Order 를 업데이트 하는 기능.
   * 음식 상태, 배달기사 연락처를 업데이트할 수 있다.
   * @param req riderTel(배달기사 연락처), orderStatus(음식 상태), orderId(음식 ID)
   * @param user 점주의 user 엔티티
   */
  @Transactional
  public void updateOrder(UpdateOrderRequest req, UserDetailsImpl user) {
    // 1. Order, Store 객체 찾기
    Order order = orderRepository.findById(req.getOrderId())
        .orElseThrow(() -> new OrderNotFoundException(req.getOrderId()));

    Store store = storeRepository.findByOwner_UserEmail(user.getUsername())
        .stream().findFirst().orElseThrow(() -> new OrderCantCancelled("유효하지 않는 OWNER 요청"));

    // 2. 도메인 서비스로 비즈니스 로직 실행
    if (!order.getStoreId().equals(store.getId())) return;

    orderDomainService.updateOrder(order, req);
    orderRepository.save(order);
  }

  /**
   * 주문 취소 기능
   * 이미 OrderStatus 가 DELIVERY_COMPLETED, TAKEOUT_COMPLETED 처럼 완료된 경우를 제외하고 취소가 가능하다
   * @param req orderId
   * @param user 가게 사장님의 user
   */
  @Transactional
  public void cancelOrder(CancelOrderRequest req, UserDetailsImpl user) {
    // 1. Order, Store 객체 찾기
    Order order = orderRepository.findById(req.getOrderId())
        .orElseThrow(() -> new OrderNotFoundException(req.getOrderId()));

    Store store = storeRepository.findByOwner_UserEmail(user.getUsername())
        .stream().findFirst().orElseThrow(() -> new OrderCantCancelled("유효하지 않는 OWNER 요청"));

    // 2. 비즈니스 로직 실행
    if (!order.getStoreId().equals(store.getId())) {
      throw new OrderCantCancelled("해당 점포의 주문이 아닙니다.");
    }

    // 3. 주문 상태 확인: 이미 완료된 주문은 취소 불가능
    if (order.getOrderStatus() == OrderStatus.DELIVERY_COMPLETED ||
        order.getOrderStatus() == OrderStatus.TAKEOUT_COMPLETED) {
      throw new OrderCantCancelled("이미 완료된 주문은 취소할 수 없습니다.");
    }

    orderDomainService.cancelOrder(order);
    orderRepository.save(order);
  }
}
