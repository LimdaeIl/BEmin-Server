package run.bemin.api.review.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import run.bemin.api.general.auditing.AuditableEntity;
import run.bemin.api.order.entity.Order;
import run.bemin.api.payment.entity.Payment;
import run.bemin.api.review.domain.ReviewRating;
import run.bemin.api.review.domain.ReviewRatingConverter;
import run.bemin.api.review.domain.ReviewStatus;
import run.bemin.api.store.entity.Store;
import run.bemin.api.user.entity.User;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID reviewId;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "payment_id", nullable = false, unique = true)
  private Payment payment;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false, unique = true)
  private Order order;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "store_id", nullable = false)
  private Store store;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "userEmail", nullable = false)
  private User user;

  @Convert(converter = ReviewRatingConverter.class) // 변환기 적용
  @Column(nullable = false)
  private ReviewRating rating;

  @Column(columnDefinition = "text")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ReviewStatus status = ReviewStatus.ACTIVE;

  private String deletedBy;
  private LocalDateTime deletedAt;

  // 숫자로 변환해서 반환하는 메서드
//  public int getNumericReviewRating() {
//    return ReviewRatingConverter.toNumeric(this.rating);
//  }

  // 리뷰 수정하기
  public void updateReview(ReviewRating reviewRating, String description) {
    this.rating = reviewRating;
    this.description = description;
  }

  // 리뷰 삭제하기
  public void deleteReview(String deletedBy) {
    this.status = ReviewStatus.DELETED;
    this.deletedAt = LocalDateTime.now();
    this.deletedBy = deletedBy;
  }

  @Builder
  public Review(Payment payment, Order order, Store store, User user, ReviewRating reviewRating, String description) {
    this.payment = payment;
    this.order = order;
    this.store = store;
    this.user = user;
    this.rating = reviewRating;
    this.description = description;
    this.status = ReviewStatus.ACTIVE;
  }
}
