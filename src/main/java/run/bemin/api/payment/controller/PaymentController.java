package run.bemin.api.payment.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import run.bemin.api.general.response.ApiResponse;
import run.bemin.api.payment.dto.CreatePaymentDto;
import run.bemin.api.payment.dto.PaymentCancelDto;
import run.bemin.api.payment.dto.PaymentDto;
import run.bemin.api.payment.dto.PaymentStatusResponseDto;
import run.bemin.api.payment.service.PaymentService;
import run.bemin.api.security.UserDetailsImpl;
import run.bemin.api.user.entity.User;
import run.bemin.api.user.service.UserService;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "결제", description = "PaymentController")
public class PaymentController {

  private final PaymentService paymentService;
  private final UserService userService;

  @PreAuthorize("hasRole('MASTER') or hasRole('MANAGER')")
  @GetMapping("/payments/status")
  @Operation(summary = "결제 상태 조회", description = "결제 ID에 대해 결제 상태 조회")
  public ResponseEntity<ApiResponse<PaymentStatusResponseDto>> getPaymentStatus(@RequestParam UUID paymentId) {
    PaymentStatusResponseDto response = paymentService.getPaymentStatus(paymentId);

    return ResponseEntity.ok(ApiResponse.from(HttpStatus.OK, "성공", response));
  }

  @GetMapping("/user/payments")
  @Operation(summary = "사용자의 결제 내역 조회", description = "특정 사용자의 모든 결제 내역 조회")
  public ResponseEntity<ApiResponse<List<PaymentDto>>> getUserPayments(
      @AuthenticationPrincipal UserDetailsImpl userDetails) {
    User user = userService.findByUserEmail(userDetails.getUsername());

    List<PaymentDto> payments = paymentService.getUserPayments(user);

    return ResponseEntity.ok(ApiResponse.from(HttpStatus.OK, "성공", payments));
  }

  @PostMapping("/payments")
  @Operation(summary = "결제 생성하기", description = "주문이 완료되었는지 검사 후, 결제 진행하기")
  public ResponseEntity<ApiResponse<PaymentDto>> createPayment(@RequestBody CreatePaymentDto createPaymentDto) {
    PaymentDto payment = paymentService.createPayment(createPaymentDto);

    return ResponseEntity.ok(ApiResponse.from(HttpStatus.CREATED, "성공", payment));
  }

  @DeleteMapping("/payments/cancel")
  @Operation(summary = "결제 취소하기", description = "결제 취소하기")
  public ResponseEntity<ApiResponse<PaymentCancelDto>> deletePayment(
      @AuthenticationPrincipal UserDetailsImpl userDetails,
      @RequestParam UUID paymentId) {
    User user = userService.findByUserEmail(userDetails.getUsername());
    PaymentCancelDto payment = paymentService.cancelPayment(user, paymentId);

    return ResponseEntity.ok(ApiResponse.from(HttpStatus.OK, "성공", payment));
  }
}
