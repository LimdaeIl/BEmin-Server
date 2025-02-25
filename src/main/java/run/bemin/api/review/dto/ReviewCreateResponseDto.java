package run.bemin.api.review.dto;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import run.bemin.api.review.domain.ReviewRating;
import run.bemin.api.review.entity.Review;

@Getter
@Builder
public class ReviewCreateResponseDto {
  private String paymentId;
  private String orderId;
  private String storeId;
  private String reviewId;
  private ReviewRating reviewRating;
  private String description;
  private String createdBy;
  private LocalDateTime createdAt;

  public static ReviewCreateResponseDto from(Review review) {
    return ReviewCreateResponseDto.builder()
        .paymentId(String.valueOf(review.getPayment().getPaymentId()))
        .orderId(String.valueOf(review.getOrder().getOrderId()))
        .storeId(String.valueOf(review.getStore().getId()))
        .reviewId(String.valueOf(review.getReviewId()))
        .reviewRating(review.getRating())
        .description(review.getDescription())
        .createdBy(review.getCreatedBy())
        .createdAt(review.getCreatedAt())
        .build();
  }
}
