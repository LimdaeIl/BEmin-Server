package run.bemin.api.review.dto;

import lombok.Builder;
import lombok.Getter;
import run.bemin.api.review.domain.ReviewRating;
import run.bemin.api.review.entity.Review;

@Getter
@Builder
public class ReviewUpdateResponseDto {
  private ReviewRating reviewRating;
  private String description;

  public static ReviewUpdateResponseDto from(Review review) {
    return ReviewUpdateResponseDto.builder()
        .reviewRating(review.getRating())
        .description(review.getDescription())
        .build();
  }
}
