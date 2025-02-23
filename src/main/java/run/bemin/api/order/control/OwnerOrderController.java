package run.bemin.api.order.control;

import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import run.bemin.api.general.response.ApiResponse;
import run.bemin.api.order.dto.OrderResponseCode;
import run.bemin.api.order.dto.request.CancelOrderRequest;
import run.bemin.api.order.dto.request.UpdateOrderRequest;
import run.bemin.api.order.dto.response.PagesResponse;
import run.bemin.api.order.dto.response.ReadOrderResponse;
import run.bemin.api.order.service.OrderOwnerService;
import run.bemin.api.order.service.OrderService;
import run.bemin.api.security.UserDetailsImpl;
import run.bemin.api.store.dto.StoreDto;
import run.bemin.api.store.service.StoreService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/owner-orders")
@PreAuthorize("hasRole('OWNER')")
public class OwnerOrderController {

  private final OrderOwnerService orderOwnerService;
  private final OrderService orderService;
  private final StoreService storeService;

  /**
   * 현재 OWNER 의 가게에 주문 내역을 출력
   * @param page 보낼 페이지
   * @param size 페이지 사이즈
   * @param sortOrder 날짜 desc, asc 정렬
   * @param date 검색할 날짜
   * @param user 현재 OWNER 의 유저 정본
   * @return 페이징처리된 ReadOrderResponse
   */
  @GetMapping("/check")
  public ResponseEntity<ApiResponse<PagesResponse<ReadOrderResponse>>> getOrdersByOwner(
      @RequestParam(value = "page", defaultValue = "0") int page,
      @RequestParam(value = "size", defaultValue = "10") int size,
      @RequestParam(value = "sortOrder", defaultValue = "desc") String sortOrder,
      @RequestParam(value = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
      @AuthenticationPrincipal UserDetailsImpl user
  ) {
    String userEmail = user.getUsername();
    StoreDto storeDto = storeService.getStoreByUserEmail(userEmail);
    PagesResponse<ReadOrderResponse> res = orderOwnerService.getOrdersByStoreIdAndDate(storeDto.id(), date, page, size, sortOrder);

    return ResponseEntity
        .status(OrderResponseCode.ORDER_FETCHED.getStatus())
        .body(ApiResponse.from(
            OrderResponseCode.ORDER_FETCHED.getStatus(),
            OrderResponseCode.ORDER_FETCHED.getMessage(),
            res
        ));
  }

  /**
   * 현재 OWNER 의 가게 주문 내역을 업데이트
   * 주로 음식의 상태값이나, 배달 기사 지정에 사용(orderStatus, riderTel)
   * @param req 음식 상태 코드, 배달기사 전화번호가 요청으로 들어옴
   * @param user 현재 들어온 OWNER 의 유저 정보
   * @return 업데이트된 Order 를 ReadOrderResponse 로 보낸다.
   */
  @PatchMapping("/update")
  public ResponseEntity<ApiResponse<ReadOrderResponse>> updateOrder(
      @RequestBody @Valid UpdateOrderRequest req,
      @AuthenticationPrincipal UserDetailsImpl user
  ) {
    orderOwnerService.updateOrder(req, user);
    ReadOrderResponse res = orderService.getOrderById(req.getOrderId());

    return ResponseEntity
        .status(OrderResponseCode.ORDER_FETCHED.getStatus())
        .body(ApiResponse.from(
            OrderResponseCode.ORDER_UPDATED.getStatus(),
            OrderResponseCode.ORDER_UPDATED.getMessage(),
            res
        ));
  }

  /**
   * OWNER 가 자기 가게에 들어온 주문을 취소
   * @param req 주문을 취소할 Order 의 UUID
   * @param user 현재 접속한 OWNER 의 유저 정보
   * @return 주문취소 업데이트된 Order 를 ReadOrderResponse 로 보낸다.
   */
  @PatchMapping("/cancel")
  public ResponseEntity<ApiResponse<ReadOrderResponse>> cancelOrder(
      @RequestBody @Valid CancelOrderRequest req,
      @AuthenticationPrincipal UserDetailsImpl user
  ) {
    orderOwnerService.cancelOrder(req, user);
    ReadOrderResponse res = orderService.getOrderById(req.getOrderId());

    return ResponseEntity
        .status(OrderResponseCode.ORDER_CANCELED.getStatus())
        .body(ApiResponse.from(
            OrderResponseCode.ORDER_CANCELED.getStatus(),
            OrderResponseCode.ORDER_CANCELED.getMessage(),
            res
        ));
  }
}
