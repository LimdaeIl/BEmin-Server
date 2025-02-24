package run.bemin.api.review.controller;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import run.bemin.api.general.response.ApiResponse;
import run.bemin.api.review.dto.PagedReviewResponseDto;
import run.bemin.api.review.dto.ReviewCreateRequestDto;
import run.bemin.api.review.dto.ReviewCreateResponseDto;
import run.bemin.api.review.dto.ReviewDeleteResponseDto;
import run.bemin.api.review.dto.ReviewUpdateRequestDto;
import run.bemin.api.review.dto.ReviewUpdateResponseDto;
import run.bemin.api.review.service.ReviewService;
import run.bemin.api.security.UserDetailsImpl;
import run.bemin.api.user.entity.User;
import run.bemin.api.user.service.UserService;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ReviewController {
  private final ReviewService reviewService;
  private final UserService userService;

  // 특정 Store의 리뷰 페이징 조회
  @GetMapping("/reviews/{storeId}")
  public ResponseEntity<ApiResponse<PagedReviewResponseDto>> getPagedReviewsByStore(
      @PathVariable UUID storeId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size,
      @RequestParam(defaultValue = "createdAt") String sortBy,
      @RequestParam(defaultValue = "desc") String direction) {

    // 정렬 조건 설정
    Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
    Pageable pageable = PageRequest.of(page, size, sort);

    // 서비스 호출
    PagedReviewResponseDto responseDto = reviewService.getPagedReviewsByStore(storeId, pageable);

    // ApiResponse 포맷으로 반환
    return ResponseEntity.ok(ApiResponse.from(HttpStatus.OK, "성공", responseDto));
  }

  // 리뷰 생성하기
  @PostMapping("/reviews")
  public ResponseEntity<ReviewCreateResponseDto> createReview(
      UserDetailsImpl userDetails,
      @RequestBody ReviewCreateRequestDto requestDto) {

    User user = userService.findByUserEmail(userDetails.getUsername());

    ReviewCreateResponseDto responseDto = reviewService.createReview(user, requestDto);

    return ResponseEntity.ok(responseDto);
  }

  // 리뷰 수정하기
  @PatchMapping("/reviews/{reviewId}")
  public ResponseEntity<ReviewUpdateResponseDto> updateReview(
      UserDetailsImpl userDetails,
      @PathVariable UUID reviewId,
      @RequestBody ReviewUpdateRequestDto reviewUpdateRequest
  ) {
    User user = userService.findByUserEmail(userDetails.getUsername());

    ReviewUpdateResponseDto updatedReview = reviewService.updateReview(user, reviewId, reviewUpdateRequest);

    return ResponseEntity.ok(updatedReview);
  }

  // 리뷰 삭제하기
  @DeleteMapping("/reviews/{reviewId}")
  public ResponseEntity<ReviewDeleteResponseDto> deleteReview(UserDetailsImpl userDetails,
                                                              @PathVariable UUID reviewId) {
    User user = userService.findByUserEmail(userDetails.getUsername());

    ReviewDeleteResponseDto deleteReview = reviewService.deleteReview(user, reviewId);

    return ResponseEntity.ok(deleteReview);
  }

  // 리뷰 평점
  @GetMapping("/review/rating/{storeId}")
  public double getAvgRating(@PathVariable UUID storeId) {
    double avg = reviewService.getAvgRatingByStore(storeId);
    return avg;
  }
}
