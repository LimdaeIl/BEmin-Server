package run.bemin.api.payment.service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;
import run.bemin.api.general.exception.ErrorCode;
import run.bemin.api.order.entity.Order;
import run.bemin.api.order.repo.OrderRepository;
import run.bemin.api.payment.domain.PaymentStatus;
import run.bemin.api.payment.dto.CreatePaymentDto;
import run.bemin.api.payment.dto.PaymentCancelDto;
import run.bemin.api.payment.dto.PaymentDto;
import run.bemin.api.payment.dto.PaymentStatusResponseDto;
import run.bemin.api.payment.entity.Payment;
import run.bemin.api.payment.exception.PaymentException;
import run.bemin.api.payment.repository.PaymentRepository;
import run.bemin.api.user.entity.User;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

  private final OrderRepository orderRepository;
  private final PaymentRepository paymentRepository;

  /*
   * 메서드명 : getPaymentStatus
   * 목적 : 결제 상태 조회
   * */
  public PaymentStatusResponseDto getPaymentStatus(UUID paymentId) {
    Payment payment = paymentRepository.findById(paymentId)
        .orElseThrow(() -> new PaymentException(ErrorCode.PAYMENT_NOT_FOUND));

    return PaymentStatusResponseDto.from(payment);
  }

  /*
   * 메서드명 : getUserPayments
   * 목적 : 사용자의 결제 내역 조회
   * */
  public List<PaymentDto> getUserPayments(User user) {
    // 사용자 결제 내역 조회
    List<Payment> payments = paymentRepository.findByCreatedBy(user.getUserEmail());

    // dto 변환 후 반환
    return payments.stream()
        .map(PaymentDto::from)
        .collect(Collectors.toList());
  }

  /*
   * 메서드명 : createPayment
   * 목적 : 결제 생성하기
   * */
  @Transactional
  public PaymentDto createPayment(@RequestBody CreatePaymentDto createPaymentDto) {
    // 결제 금액이 0보다 작거나 같은 경우 예외 발생
    if (createPaymentDto.getAmount() < 0) {
      throw new PaymentException(ErrorCode.INVALID_INPUT_VALUE);
    }

    // 주문이 존재하는지 확인하기
    Order order = orderRepository.findById(UUID.fromString(createPaymentDto.getOrderId()))
        .orElseThrow(() -> new PaymentException(ErrorCode.ORDER_NOT_FOUND));

    Payment payment = Payment.builder()
        .order(order)
        .payment(createPaymentDto.getPaymentMethod())
        .status(PaymentStatus.COMPLETED)
        .amount(createPaymentDto.getAmount())
        .build();

    paymentRepository.save(payment);

    return PaymentDto.from(payment);
  }

  /*
   * 메서드명 : paymentCancel
   * 목적 : 결제 취소
   * */
  @Transactional
  public PaymentCancelDto cancelPayment(User user, UUID paymentId) {
    Payment payment = paymentRepository.findById(paymentId)
        .orElseThrow(() -> new PaymentException(ErrorCode.PAYMENT_NOT_FOUND));

    // 이미 취소된 주문은 예외 발생
    if (payment.getStatus() == PaymentStatus.CANCELED) {
      throw new PaymentException(ErrorCode.PAYMENT_IS_CANCELED);
    }

    payment.cancelPayment(user.getUserEmail());

    return PaymentCancelDto.from(payment);
  }
}
