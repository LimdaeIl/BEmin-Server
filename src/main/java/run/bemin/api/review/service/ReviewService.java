package run.bemin.api.review.service;

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import run.bemin.api.general.exception.ErrorCode;
import run.bemin.api.order.entity.Order;
import run.bemin.api.order.entity.OrderStatus;
import run.bemin.api.order.repo.OrderRepository;
import run.bemin.api.payment.entity.Payment;
import run.bemin.api.payment.repository.PaymentRepository;
import run.bemin.api.review.dto.PageInfoDto;
import run.bemin.api.review.dto.PagedReviewResponseDto;
import run.bemin.api.review.dto.ReviewCreateRequestDto;
import run.bemin.api.review.dto.ReviewCreateResponseDto;
import run.bemin.api.review.dto.ReviewDeleteResponseDto;
import run.bemin.api.review.dto.ReviewResponseDto;
import run.bemin.api.review.dto.ReviewUpdateRequestDto;
import run.bemin.api.review.dto.ReviewUpdateResponseDto;
import run.bemin.api.review.entity.Review;
import run.bemin.api.review.exception.ReviewException;
import run.bemin.api.review.messaging.ReviewEvent;
import run.bemin.api.review.messaging.ReviewProducer;
import run.bemin.api.review.repository.ReviewRepository;
import run.bemin.api.store.entity.Store;
import run.bemin.api.store.repository.StoreRepository;
import run.bemin.api.user.entity.User;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewService {

  private final PaymentRepository paymentRepository;
  private final ReviewRepository reviewRepository;
  private final OrderRepository orderRepository;
  private final StoreRepository storeRepository;
  private final ReviewProducer reviewProducer;

  // order 찾기
  private Order getOrder(UUID orderId) {
    Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new ReviewException(ErrorCode.ORDER_NOT_FOUND));
    return order;
  }

  // payment 찾기
  private Payment getPayment(UUID paymentId) {
    Payment payment = paymentRepository.findById(paymentId)
        .orElseThrow(() -> new ReviewException(ErrorCode.PAYMENT_NOT_FOUND));
    return payment;
  }

  // store 찾기
  private Store getStore(UUID storeId) {
    Store store = storeRepository.findById(storeId)
        .orElseThrow(() -> new ReviewException(ErrorCode.STORE_NOT_FOUND));
    return store;
  }

  // review 찾기
  private Review getReview(UUID reviewId) {
    Review review = reviewRepository.findById(reviewId)
        .orElseThrow(() -> new ReviewException(ErrorCode.REVIEW_NOT_FOUND));
    return review;
  }

  // 특정 가게 리뷰 페이징 처리
  public PagedReviewResponseDto getPagedReviewsByStore(UUID storeId, Pageable pageable) {
    Page<ReviewResponseDto> reviewsPage = reviewRepository.findReviewsByStoreId(storeId, pageable);

    // 리뷰 목록
    List<ReviewResponseDto> reviews = reviewsPage.getContent();

    // 페이징 정보
    PageInfoDto pageInfo = new PageInfoDto(
        reviewsPage.getSize(),
        reviewsPage.getNumber(),
        reviewsPage.getTotalElements(),
        reviewsPage.getTotalPages()
    );

    return new PagedReviewResponseDto(storeId, reviews, pageInfo);
  }

  private boolean isOrderStatusReviewable(OrderStatus orderStatus) {
    return orderStatus == OrderStatus.DELIVERY_COMPLETED ||
        orderStatus == OrderStatus.TAKEOUT_COMPLETED ||
        orderStatus == OrderStatus.TAKEOUT_HANDOVER_COMPLETED;
  }

  // 리뷰 생성
  @Transactional
  public ReviewCreateResponseDto createReview(User user, ReviewCreateRequestDto requestDto) {
    // order 가져오기
    Order order = getOrder(UUID.fromString(requestDto.getOrderId()));

    // 주문 상태 체크: 허용된 상태가 아니면 예외 발생
    if (!isOrderStatusReviewable(order.getOrderStatus())) {
      throw new ReviewException(ErrorCode.ORDER_NOT_REVIEWABLE);
    }

    // store 가져오기
    Store store = getStore(UUID.fromString(requestDto.getStoreId()));

    // payment 가져오기
    Payment payment = getPayment(UUID.fromString(requestDto.getPaymentId()));

    Review review = Review.builder()
        .payment(payment)
        .order(order)
        .store(store)
        .user(user)
        .reviewRating(requestDto.toReviewRating())
        .description(requestDto.getDescription())
        .build();

    reviewRepository.save(review);

    // 평균 평점 계산
    double newAvgRating = reviewRepository.findAverageRatingByStore(review.getStore().getId());

    // RabbitMQ로 store 평점 갱신하기
    ReviewEvent event = new ReviewEvent(review.getStore().getId(), newAvgRating);
    reviewProducer.sendReviewEvent(event);

    return ReviewCreateResponseDto.from(review);
  }

  // 리뷰 수정하기
  @Transactional
  public ReviewUpdateResponseDto updateReview(User user, UUID reviewId, ReviewUpdateRequestDto request) {
    // 리뷰 조회
    Review review = getReview(reviewId);

    // 리뷰 작성자와 현재 로그인한 사용자 비교
    if (!review.getUser().equals(user)) {
      throw new ReviewException(ErrorCode.REVIEW_CANNOT_FIX);
    }

    // 리뷰 수정
    review.updateReview(request.toReviewRating(), request.getDescription());

    return ReviewUpdateResponseDto.from(review);
  }

  // 리뷰 삭제하기
  @Transactional
  public ReviewDeleteResponseDto deleteReview(User user, UUID reviewId) {
    // 리뷰 조회
    Review review = getReview(reviewId);

    // 리뷰 작성자와 현재 로그인한 사용자 비교
    if (!review.getUser().equals(user)) {
      throw new ReviewException(ErrorCode.REVIEW_CANNOT_FIX);
    }

    review.deleteReview(user.getUserEmail());

    // 평균 평점 계산
    double newAvgRating = reviewRepository.findAverageRatingByStore(review.getStore().getId());

    // RabbitMQ로 store 평점 갱신하기
    ReviewEvent event = new ReviewEvent(review.getStore().getId(), newAvgRating);
    reviewProducer.sendReviewEvent(event);

    return ReviewDeleteResponseDto.from(review);
  }

  // 특정 가게의 평균 평점 계산
  @Transactional
  public Double getAvgRatingByStore(UUID storeId) {
    double avg = reviewRepository.findAverageRatingByStore(storeId);
    return avg;
  }
}
