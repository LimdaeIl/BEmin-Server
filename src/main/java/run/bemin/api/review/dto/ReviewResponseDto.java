package run.bemin.api.review.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import run.bemin.api.review.domain.ReviewRating;
import run.bemin.api.review.entity.Review;

@Getter
@Builder
@NoArgsConstructor
public class ReviewResponseDto {
  private String userEmail;
  private String description;
  private ReviewRating reviewRating;

  public ReviewResponseDto(String userEmail, String description, ReviewRating reviewRating) {
    this.userEmail = userEmail;
    this.description = description;
    this.reviewRating = reviewRating;
  }

  public static ReviewResponseDto from(Review review) {
    return ReviewResponseDto.builder()
        .userEmail(review.getUser().getUserEmail())
        .description(review.getDescription())
        .reviewRating(review.getRating())
        .build();
  }
}
