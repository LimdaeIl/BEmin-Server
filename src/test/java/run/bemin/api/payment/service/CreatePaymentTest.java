package run.bemin.api.payment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.bemin.api.general.exception.ErrorCode;
import run.bemin.api.order.entity.Order;
import run.bemin.api.order.entity.OrderAddress;
import run.bemin.api.order.entity.OrderType;
import run.bemin.api.order.repo.OrderRepository;
import run.bemin.api.payment.domain.PaymentMethod;
import run.bemin.api.payment.domain.PaymentStatus;
import run.bemin.api.payment.dto.CreatePaymentDto;
import run.bemin.api.payment.dto.PaymentDto;
import run.bemin.api.payment.entity.Payment;
import run.bemin.api.payment.exception.PaymentException;
import run.bemin.api.payment.repository.PaymentRepository;
import run.bemin.api.user.entity.User;
import run.bemin.api.user.entity.UserRoleEnum;

@ExtendWith(MockitoExtension.class)
class CreatePaymentTest {

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private PaymentRepository paymentRepository;

  @InjectMocks
  private PaymentService paymentService;

  private UUID orderId;
  private User testUser;
  private Order testOrder;
  private Payment payment;
  private CreatePaymentDto request;

  @BeforeEach
  void setUp() {
    orderId = UUID.randomUUID();
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

    request = new CreatePaymentDto(
        orderId.toString(),
        PaymentMethod.CREDIT_CARD,
        10000
    );
  }

  @Test
  @DisplayName("주문이 존재하면 정상적으로 생성되어야 한다")
  void createPayment_ValidInput_ShouldCreatePayment() throws NoSuchFieldException, IllegalAccessException {
    // Given
    UUID orderUUID = UUID.randomUUID();

    // Reflection을 사용하여 orderId 설정
    Field orderIdField = Order.class.getDeclaredField("orderId");
    orderIdField.setAccessible(true);
    orderIdField.set(testOrder, orderUUID);

    // any(UUID.class)를 사용하여 stubbing
    when(orderRepository.findById(any(UUID.class))).thenReturn(Optional.of(testOrder));
    when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

    // When
    PaymentDto result = paymentService.createPayment(request);

    // Then
    assertNotNull(result);
    assertEquals(payment.getPayment(), result.getPaymentMethod());
    assertEquals(PaymentStatus.COMPLETED.getValue(), result.getStatus());
    assertEquals(payment.getAmount(), result.getAmount());
    assertEquals(orderUUID.toString(), result.getOrderId());

    verify(orderRepository).findById(any(UUID.class));
    verify(paymentRepository).save(any(Payment.class));

    System.out.println("생성된 orderId " + result.getOrderId());
  }

  @Test
  @DisplayName("주문이 존재하지 않으면 PaymentException이 발생해야 한다")
  void createPayment_OrderNotFound_ShouldThrowException() {
    // Given
    when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

    // When & Then
    assertThrows(PaymentException.class, () -> paymentService.createPayment(request));
    verify(orderRepository).findById(orderId);
    verify(paymentRepository, never()).save(any(Payment.class));
  }


  @Test
  @DisplayName("결제 금액이 0일 때 PaymentException이 발생해야 한다")
  void createPayment_ZeroAmount_ShouldThrowException() {
    // Given
    CreatePaymentDto zeroAmountCreatePaymentDto = new CreatePaymentDto(
        orderId.toString(),
        PaymentMethod.CREDIT_CARD,
        -10
    );

    // When & Then
    PaymentException exception = assertThrows(PaymentException.class,
        () -> paymentService.createPayment(zeroAmountCreatePaymentDto));

    assertEquals(ErrorCode.INVALID_INPUT_VALUE, exception.getErrorCode());
    verify(orderRepository, never()).findById(any(UUID.class));
    verify(paymentRepository, never()).save(any(Payment.class));
  }
}
