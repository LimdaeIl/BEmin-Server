package run.bemin.api.payment.controller;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
public class PaymentController {

  private final PaymentService paymentService;
  private final UserService userService;

  @GetMapping("/payments/status")
  public ResponseEntity<ApiResponse<PaymentStatusResponseDto>> getPaymentStatus(
      @RequestParam UUID paymentId) {
    PaymentStatusResponseDto response = paymentService.getPaymentStatus(paymentId);

    return ResponseEntity.ok(ApiResponse.from(HttpStatus.OK, "성공", response));
  }

  @GetMapping("/user/payments")
  public ResponseEntity<ApiResponse<List<PaymentDto>>> getUserPayments(UserDetailsImpl userDetails) {
    User user = userService.findByUserEmail(userDetails.getUsername());

    List<PaymentDto> payments = paymentService.getUserPayments(user);

    return ResponseEntity.ok(ApiResponse.from(HttpStatus.OK, "성공", payments));
  }

  @PostMapping("/payments")
  public ResponseEntity<ApiResponse<PaymentDto>> createPayment(@RequestBody CreatePaymentDto createPaymentDto) {
    PaymentDto payment = paymentService.createPayment(createPaymentDto);

    return ResponseEntity.ok(ApiResponse.from(HttpStatus.CREATED, "성공", payment));
  }

  @DeleteMapping("/payments/cancel")
  public ResponseEntity<ApiResponse<PaymentCancelDto>> deletePayment(
      @AuthenticationPrincipal UserDetailsImpl userDetails,
      @RequestParam UUID paymentId) {
    User user = userService.findByUserEmail(userDetails.getUsername());
    PaymentCancelDto payment = paymentService.cancelPayment(user, paymentId);

    return ResponseEntity.ok(ApiResponse.from(HttpStatus.OK, "성공", payment));
  }
}
