package run.bemin.api.review.service;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static run.bemin.api.user.entity.QUser.user;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import run.bemin.api.order.entity.Order;
import run.bemin.api.order.entity.OrderAddress;
import run.bemin.api.order.entity.OrderType;
import run.bemin.api.order.repo.OrderRepository;
import run.bemin.api.payment.entity.Payment;
import run.bemin.api.payment.repository.PaymentRepository;
import run.bemin.api.review.dto.ReviewCreateRequestDto;
import run.bemin.api.review.dto.ReviewCreateResponseDto;
import run.bemin.api.review.entity.Review;
import run.bemin.api.review.messaging.ReviewEvent;
import run.bemin.api.review.messaging.ReviewProducer;
import run.bemin.api.review.repository.ReviewRepository;
import run.bemin.api.store.entity.Store;
import run.bemin.api.store.repository.StoreRepository;
import run.bemin.api.user.entity.User;
import run.bemin.api.user.entity.UserRoleEnum;

@ExtendWith(MockitoExtension.class)
public class CreateReviewTest {

  @Mock
  private ReviewRepository reviewRepository;

  @Mock
  private OrderRepository orderRepository;

  @Mock
  private StoreRepository storeRepository;

  @Mock
  private PaymentRepository paymentRepository;

  @Mock
  private ReviewProducer reviewProducer;

  @InjectMocks
  private ReviewService reviewService;

  private User testUser;
  private Store testStore;
  private UUID orderId;
  private UUID paymentId;
  private UUID storeId;
  private Order testOrder;
  private Payment testPayment;
  private ReviewCreateRequestDto reviewCreateRequestDto;

  @BeforeEach
  void setUp() {
    orderId = UUID.randomUUID();
    paymentId = UUID.randomUUID();
    storeId = UUID.randomUUID();

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

    reviewCreateRequestDto = new ReviewCreateRequestDto(
        paymentId.toString(),
        orderId.toString(),
        storeId.toString(),
        5,
        "맛있어요!"
    );
  }

  @Test
  @DisplayName("유효한 입력으로 리뷰 생성 성공")
  void createReview_ValidInput_ShouldCreateReview() {
    when(orderRepository.findById(any(UUID.class))).thenReturn(Optional.of(testOrder));
    when(storeRepository.findById(any(UUID.class))).thenReturn(Optional.of(testStore));
    when(paymentRepository.findById(any(UUID.class))).thenReturn(Optional.of(testPayment));
    when(reviewRepository.findAverageRatingByStore(any(UUID.class))).thenReturn(4.5);

    // When
    ReviewCreateResponseDto responseDto = reviewService.createReview(user, requestDto);

    // Then
    assertNotNull(responseDto);
    verify(reviewRepository).save(any(Review.class));
    verify(reviewProducer).sendReviewEvent(any(ReviewEvent.class));
  }
}
