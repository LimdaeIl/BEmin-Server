package run.bemin.api.payment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import run.bemin.api.order.entity.Order;
import run.bemin.api.order.entity.OrderAddress;
import run.bemin.api.order.entity.OrderType;
import run.bemin.api.payment.domain.PaymentMethod;
import run.bemin.api.payment.domain.PaymentStatus;
import run.bemin.api.payment.dto.PaymentCancelDto;
import run.bemin.api.payment.entity.Payment;
import run.bemin.api.payment.exception.PaymentException;
import run.bemin.api.payment.repository.PaymentRepository;
import run.bemin.api.user.entity.User;
import run.bemin.api.user.entity.UserRoleEnum;

@ExtendWith(MockitoExtension.class)
public class CancelPaymentTest {

  @Mock
  private PaymentRepository paymentRepository;

  @InjectMocks
  private PaymentService paymentService;
  private User testUser;
  private UUID orderId;
  private UUID paymentId;
  private Order testOrder;
  private Payment testPayment;

  @BeforeEach
  void setUp() {
    orderId = UUID.randomUUID();
    paymentId = UUID.randomUUID();

    testUser = User.builder()
        .userEmail("test@test.com")
        .password("password")
        .name("Test User")
        .phone("01012345678")
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

    testPayment = Payment.builder()
        .order(testOrder)
        .payment(PaymentMethod.CREDIT_CARD)
        .amount(10000)
        .status(PaymentStatus.COMPLETED)
        .build();

    // Reflection을 사용하여 orderId 설정
    ReflectionTestUtils.setField(testOrder, "orderId", orderId);
    ReflectionTestUtils.setField(testPayment, "paymentId", paymentId);
  }

  @Test
  @DisplayName("유효한 입력으로 결제 취소 성공")
  void cancelPayment_ValidInput_ShouldCancelPayment() {
    when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(testPayment));

    // When
    PaymentCancelDto paymentCancelDto = paymentService.cancelPayment(testUser, paymentId);

    // Then
    assertNotNull(paymentCancelDto);
    assertEquals(paymentId, paymentCancelDto.getPaymentId());
    assertEquals(PaymentMethod.CREDIT_CARD, paymentCancelDto.getPaymentMethod());
    assertEquals(10000, paymentCancelDto.getAmount());
    assertEquals(PaymentStatus.CANCELED.getValue(), paymentCancelDto.getStatus());

    verify(paymentRepository).findById(paymentId);

//    System.out.println("paymentId : " + paymentCancelDto.getPaymentId());
//    System.out.println("payment Status : " + paymentCancelDto.getStatus());
  }

  @Test
  @DisplayName("존재하지 않는 결제 ID로 취소 시도 시 예외 발생")
  void cancelPayment_InvalidPaymentId_ShouldThrowException() {
    // Given
    when(paymentRepository.findById(paymentId)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(PaymentException.class, () -> paymentService.cancelPayment(testUser, paymentId));
    verify(paymentRepository).findById(paymentId);
  }

  @Test
  @DisplayName("이미 취소된 결제 취소 시도 시 예외 발생")
  void cancelPayment_AlreadyCanceledPayment_ShouldThrowException() {
    // Given
    testPayment.updateStatus(PaymentStatus.CANCELED);
    when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(testPayment));

    // When & Then
    assertThrows(PaymentException.class, () -> paymentService.cancelPayment(testUser, paymentId));
    verify(paymentRepository).findById(paymentId);
  }
}
