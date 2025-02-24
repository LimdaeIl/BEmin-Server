package run.bemin.api.review.dto;

import lombok.Getter;
import run.bemin.api.review.domain.ReviewRating;

@Getter
public class ReviewCreateRequestDto {
  private String paymentId;
  private String orderId;
  private String storeId;
  private int reviewRating;
  private String description;

  public ReviewCreateRequestDto(String paymentId,
                                String orderId,
                                String storeId,
                                int reviewRating,
                                String description) {
    this.paymentId = paymentId;
    this.orderId = orderId;
    this.storeId = storeId;
    this.reviewRating = reviewRating;
    this.description = description;
  }

  public ReviewRating toReviewRating() {
    return ReviewRating.fromValue(reviewRating);
  }
}
