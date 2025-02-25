package run.bemin.api.review;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.Statistics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import run.bemin.api.auth.util.JwtUtil;
import run.bemin.api.order.entity.Order;
import run.bemin.api.order.entity.OrderAddress;
import run.bemin.api.order.entity.OrderStatus;
import run.bemin.api.order.entity.OrderType;
import run.bemin.api.order.repo.OrderRepository;
import run.bemin.api.payment.domain.PaymentMethod;
import run.bemin.api.payment.domain.PaymentStatus;
import run.bemin.api.payment.entity.Payment;
import run.bemin.api.payment.repository.PaymentRepository;
import run.bemin.api.review.dto.ReviewCreateRequestDto;
import run.bemin.api.review.dto.ReviewCreateResponseDto;
import run.bemin.api.review.service.ReviewService;
import run.bemin.api.store.entity.Store;
import run.bemin.api.store.entity.StoreAddress;
import run.bemin.api.store.repository.StoreRepository;
import run.bemin.api.user.entity.User;
import run.bemin.api.user.entity.UserRoleEnum;
import run.bemin.api.user.repository.UserRepository;

@SpringBootTest
@ActiveProfiles("test")
public class ReviewIntegrationTest {

  private static final Logger log = LoggerFactory.getLogger(ReviewIntegrationTest.class);

  @Autowired
  private ReviewService reviewService;

  @Autowired
  private StoreRepository storeRepository;

  @Autowired
  private OrderRepository orderRepository;

  @Autowired
  private PaymentRepository paymentRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private EntityManagerFactory entityManagerFactory;

  @Autowired
  private JwtUtil jwtUtil;

  // RabbitTemplate 를 통해 RabbitMQ 연결 상태 확인 (선택 사항)
  @Autowired
  private RabbitTemplate rabbitTemplate;

  private User testUser;
  private Store testStore;
  private Order testOrder;
  private Payment testPayment;

  @BeforeEach
  public void setup() {
    // 테스트용 User 생성
    testUser = User.builder()
        .userEmail("testuser@example.com")
        .password("password")  // 실제 환경에서는 암호화된 값 사용
        .name("Test User")
        .nickname("Tester")
        .phone("01012345678")
        .role(UserRoleEnum.CUSTOMER)
        .build();
    userRepository.saveAndFlush(testUser);

    // 테스트용 StoreAddress 및 Store 생성
    StoreAddress storeAddress = StoreAddress.builder()
        .zoneCode("12345")
        .bcode("1234567890")
        .jibunAddress("지번 주소 예시")
        .roadAddress("도로명 주소 예시")
        .detail("상세 주소 예시")
        .build();
    testStore = Store.create("Test Store", "01087654321", 10000, true, storeAddress, testUser);
    storeAddress.setStore(testStore);
    testStore = storeRepository.save(testStore);

    // 테스트용 OrderAddress 는 OrderAddress.of(...) 정적 팩토리 메서드를 이용
    OrderAddress orderAddress = OrderAddress.of("12345", "지번 주소 예시", "도로명 주소 예시", "상세 주소 예시");
    testOrder = Order.builder()
        .user(testUser)
        .storeId(testStore.getId())
        .orderType(OrderType.DELIVERY)
        .storeName(testStore.getName())
        .orderAddress(orderAddress)
        .build();
    // 리뷰 작성 가능한 상태 (예: DELIVERY_COMPLETED)로 설정
    testOrder.changeOrderStatus(OrderStatus.DELIVERY_COMPLETED);
    testOrder = orderRepository.save(testOrder);

    // 테스트용 Payment 생성
    testPayment = Payment.builder()
        .order(testOrder)
        .payment(PaymentMethod.CREDIT_CARD)
        .amount(20000)
        .status(PaymentStatus.COMPLETED)
        .build();
    testPayment = paymentRepository.save(testPayment);
  }

  @Test
  @DisplayName("[RabbitMQ, Redis] 가게 평점 통합 테스트")
  @Disabled
  public void testCreateReviewAndRatingUpdate() throws Exception {
    // JWT 토큰 생성
    String authToken = jwtUtil.createAccessToken(testUser.getUserEmail(), testUser.getRole());
    log.info("JWT Token: {}", authToken);

    // ReviewCreateRequestDto 준비
    ReviewCreateRequestDto requestDto = new ReviewCreateRequestDto(
        testPayment.getPaymentId().toString(),
        testOrder.getOrderId().toString(),
        testStore.getId().toString(),
        5,
        "Delicious food!"
    );

    log.info("=== Review Request Information ===");
    log.info("Order ID: {}", requestDto.getOrderId());
    log.info("Store ID: {}", requestDto.getStoreId());
    log.info("Payment ID: {}", requestDto.getPaymentId());
    log.info("Description: {}", requestDto.getDescription());
    log.info("Review Rating: {}", requestDto.getReviewRating());
    log.info("==================================");

    // 리뷰 생성 호출 – 실제 RabbitMQ로 이벤트 전송됨
    ReviewCreateResponseDto responseDto = reviewService.createReview(testUser, requestDto);
    assertNotNull(responseDto, "리뷰 생성 응답은 null 이 아니어야 합니다.");

    log.info("=== Created Review Details ===");
    log.info("Review ID: {}", responseDto.getReviewId());
    log.info("Review Rating: {}", responseDto.getReviewRating());
    log.info("Review Description: {}", responseDto.getDescription());
    log.info("Review Created By: {}", responseDto.getCreatedBy());
    log.info("Review Created At: {}", responseDto.getCreatedAt());
    log.info("================================");

    // 충분한 시간 대기하여 이벤트가 처리되도록 함
    Thread.sleep(5000);

    // 갱신된 가게 정보 조회
    Store updatedStore = storeRepository.findByIdWithReviews(testStore.getId())
        .orElseThrow(() -> new RuntimeException("테스트용 Store 를 찾을 수 없습니다."));

    log.info("=== Updated Store Details ===");
    log.info("Store ID: {}", updatedStore.getId());
    log.info("Store Name: {}", updatedStore.getName());
    log.info("Store Phone: {}", updatedStore.getPhone());
    log.info("Store Minimum Price: {}", updatedStore.getMinimumPrice());
    log.info("Store Active Status: {}", updatedStore.getIsActive());
    log.info("Store Rating: {}", updatedStore.getRating());
    log.info("Store Owner: {}", updatedStore.getOwner().getUserEmail());
    log.info("Store Created At: {}", updatedStore.getCreatedAt());
    log.info("================================");

    log.info("=== Reviews in the Store ===");
    log.info("Total Reviews: {}", updatedStore.getReviews().size());
    updatedStore.getReviews().forEach(review -> {
      log.info("Review ID: {}", review.getReviewId());
      log.info("Review Rating: {}", review.getRating());
      log.info("Review Description: {}", review.getDescription());
      log.info("Review Status: {}", review.getStatus());
      log.info("Review Created At: {}", review.getCreatedAt());
      log.info("Review Created By: {}", review.getCreatedBy());
      log.info("--------------------------------");
    });
    log.info("================================");

    // 검증: 리뷰가 하나이고 5점이면 평균 평점은 5.0이어야 함
    assertEquals(5.0, updatedStore.getRating().doubleValue(), "가게의 평점이 5.0이어야 합니다.");
  }

  @Test
  @DisplayName("[RabbitMQ, Redis] 10000개의 리뷰 데이터 생성 및 평점 업데이트 테스트 - 각 리뷰마다 다른 주문 사용")
  @Disabled
  public void testCreate10000ReviewsWithUniqueOrders() throws Exception {
    Statistics stats = entityManagerFactory.unwrap(SessionFactory.class).getStatistics();
    stats.setStatisticsEnabled(true);
    stats.clear();

    long startTime = System.currentTimeMillis();
    // 여러 리뷰를 위해 기본적인 주문/결제 데이터를 복제하거나 새로 생성합니다.
    for (int i = 0; i < 10000; i++) {
      // 새로운 주문 생성 (주문마다 고유한 ID를 갖도록)
      Order newOrder = Order.builder()
          .user(testUser)
          .storeId(testStore.getId())
          .orderType(OrderType.DELIVERY)
          .storeName(testStore.getName())
          .orderAddress(OrderAddress.of("12345", "지번 주소", "도로명 주소", "상세 주소"))
          .build();
      newOrder.changeOrderStatus(OrderStatus.DELIVERY_COMPLETED);
      newOrder = orderRepository.save(newOrder);

      // 새로운 결제 생성
      Payment newPayment = Payment.builder()
          .order(newOrder)
          .payment(PaymentMethod.CREDIT_CARD)
          .amount(20000)
          .status(PaymentStatus.COMPLETED)
          .build();
      newPayment = paymentRepository.save(newPayment);

      // 1, 2, 3, 4, 5 점을 순환하도록 설정 (평균은 3.0)
      int rating = 1 + (i % 5);
      String description = "Review #" + i;
      ReviewCreateRequestDto requestDto = new ReviewCreateRequestDto(
          newPayment.getPaymentId().toString(),
          newOrder.getOrderId().toString(),
          testStore.getId().toString(),
          rating,
          description
      );
      if (i % 100 == 0) {
        log.info("리뷰 생성 진행: {}번째 리뷰", i);
      }
      reviewService.createReview(testUser, requestDto);
    }

    // 비동기 처리 대기
    Thread.sleep(15000);

    Store updatedStore = storeRepository.findByIdWithReviews(testStore.getId())
        .orElseThrow(() -> new RuntimeException("테스트용 Store 를 찾을 수 없습니다."));

    log.info("생성된 리뷰 총 개수: {}", updatedStore.getReviews().size());

    double expectedRating = 3.0;
    log.info("예상 평점: {}", expectedRating);
    log.info("실제 평점: {}", updatedStore.getRating());

    assertEquals(expectedRating, updatedStore.getRating().doubleValue(), 0.01, "가게 평점은 3.0이어야 합니다.");

    long endTime = System.currentTimeMillis();
    log.info("Test execution time: {} ms", (endTime - startTime));
    log.info("Query count: {}", stats.getQueryExecutionCount());
  }
}
