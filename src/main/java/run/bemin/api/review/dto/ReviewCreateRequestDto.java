package run.bemin.api.review.dto;

import lombok.Getter;
import lombok.Setter;
import run.bemin.api.review.domain.ReviewRating;

@Setter
@Getter
public class ReviewCreateRequestDto {
  private String paymentId;
  private String orderId;
  private String storeId;
  private int reviewRating;
  private String description;

  public ReviewRating toReviewRating() {
    return ReviewRating.fromValue(reviewRating);
  }
}
