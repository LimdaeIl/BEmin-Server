package run.bemin.api.order.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import run.bemin.api.general.response.ApiResponse;
import run.bemin.api.order.dto.OrderResponseCode;
import run.bemin.api.order.dto.request.CancelOrderRequest;
import run.bemin.api.order.dto.request.CreateOrderRequest;
import run.bemin.api.order.dto.response.PagesResponse;
import run.bemin.api.order.dto.ProductDetailDTO;
import run.bemin.api.order.dto.response.ReadOrderResponse;
import run.bemin.api.order.entity.Order;
import run.bemin.api.order.service.OrderService;
import run.bemin.api.security.UserDetailsImpl;
import run.bemin.api.user.entity.User;
import run.bemin.api.user.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
@Tag(name = "주문(CUSTOMER)", description = "OrderController")
public class OrderController {

  private final OrderService orderService;
  private final UserService userService;

  /**
   * 주문 생성
   */
  @PostMapping("/order")
  @Operation(summary = "주문 생성", description = "고객이 주문을 생성하는 API 입니다.")
  public ResponseEntity<ApiResponse<Order>> createOrder(
      @AuthenticationPrincipal
      @Parameter(hidden = true) UserDetailsImpl userDetails,
      @RequestBody @Valid
      @Parameter(description = "주문 생성 요청 정보", required = true) CreateOrderRequest req
  ) {
    User user = userService.findByUserEmail(userDetails.getUsername());
    Order createdOrder = orderService.createOrder(req, user);

    return ResponseEntity
        .status(OrderResponseCode.ORDER_CREATED.getStatus())
        .body (ApiResponse.from(
            OrderResponseCode.ORDER_CREATED.getStatus(),
            OrderResponseCode.ORDER_CREATED.getMessage(),
            createdOrder
        ));
  }

  /**
   * 주문 내역 조회 (페이징 처리)
   */
  @GetMapping("/check")
  @Operation(summary = "주문 내역 조회", description = "고객이 자신의 주문을 확인하는 API 입니다.")
  public ResponseEntity<ApiResponse<PagesResponse<ReadOrderResponse>>> getOrdersByUserEmail(
      @RequestParam(value = "page", defaultValue = "0")
      @Parameter(description = "조회할 페이지 번호 (0부터 시작)", example = "0") int page,

      @RequestParam(value = "size", defaultValue = "10")
      @Parameter(description = "한 페이지에 표시할 주문 개수", example = "10") int size,

      @RequestParam(value = "sortOrder", defaultValue = "desc")
      @Parameter(description = "정렬 순서 (asc: 오름차순, desc: 내림차순)", example = "desc") String sortOrder,

      @AuthenticationPrincipal
      @Parameter(hidden = true) UserDetailsImpl user // JWT 인증을 위한 사용자 정보
  ) {
    String userEmail = user.getUsername();
    PagesResponse<ReadOrderResponse> response = orderService.getOrdersByUserEmail(userEmail, page, size, sortOrder);

    return ResponseEntity
        .status(OrderResponseCode.ORDER_FETCHED.getStatus())
        .body(ApiResponse.from(
            OrderResponseCode.ORDER_FETCHED.getStatus(),
            OrderResponseCode.ORDER_FETCHED.getMessage(),
            response
        ));
  }

  /**
   * 주문 상세 조회
   */
  @GetMapping("/detail")
  @Operation(summary = "주문 상세 조회", description = "고객이 order 에 대한 상품 상세 정보를 확인하는 API 입니다.")
  public ResponseEntity<ApiResponse<List<ProductDetailDTO>>> getOrderDetailsByOrderId(
      @RequestParam @Parameter(description = "조회할 주문의 UUID") UUID orderId
  ) {

    List<ProductDetailDTO> productDetails = orderService.getOrderDetailsByOrderId(orderId);

    return ResponseEntity
        .status(OrderResponseCode.ORDER_DETAIL_FETCHED.getStatus())
        .body(ApiResponse.from(
            OrderResponseCode.ORDER_DETAIL_FETCHED.getStatus(),
            OrderResponseCode.ORDER_DETAIL_FETCHED.getMessage(),
            productDetails
        ));
  }

  /**
   * 주문 취소
   */
  @PatchMapping("/cancel")
  @Operation(summary = "주문 취소(고객)", description = "고객이 order 에 대한 상품 상세 정보를 확인하는 API 입니다.")
  public ResponseEntity<ApiResponse<Void>> cancelOrder(
      @RequestBody @Valid @Parameter(description = "취소할 주문 UUID") CancelOrderRequest req) {
    orderService.cancelOrder(req);
    return ResponseEntity
        .status(OrderResponseCode.ORDER_CANCELED.getStatus())
        .build();
  }
}
