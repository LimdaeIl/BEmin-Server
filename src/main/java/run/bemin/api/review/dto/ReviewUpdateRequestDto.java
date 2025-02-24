package run.bemin.api.review.dto;

import lombok.Getter;
import run.bemin.api.review.domain.ReviewRating;

@Getter
public class ReviewUpdateRequestDto {
  private int reviewRating;
  private String description;

  public ReviewRating toReviewRating() {
    return ReviewRating.fromValue(reviewRating);
  }
}
