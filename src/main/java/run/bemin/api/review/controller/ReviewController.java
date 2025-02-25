package run.bemin.api.review.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
import run.bemin.api.review.dto.ReviewResponseDto;
import run.bemin.api.review.dto.ReviewUpdateRequestDto;
import run.bemin.api.review.dto.ReviewUpdateResponseDto;
import run.bemin.api.review.service.ReviewService;
import run.bemin.api.security.UserDetailsImpl;
import run.bemin.api.user.entity.User;
import run.bemin.api.user.service.UserService;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "리뷰", description = "ReviewController")
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

  // 가게 주인만 리뷰 전체 목록 페이징 조회
  @GetMapping("/auth/reviews/store/{storeId}")
  @PreAuthorize("hasRole('MANAGER') or hasRole('MASTER')")
  public ResponseEntity<ApiResponse<Page<ReviewResponseDto>>> getStoreReviews(
      @AuthenticationPrincipal UserDetailsImpl userDetails,
      @PathVariable UUID storeId,
      @PageableDefault(size = 10, sort = "createdAt", direction = Direction.DESC) Pageable pageable
  ) {
    User user = userService.findByUserEmail(userDetails.getUsername());

    Page<ReviewResponseDto> storeReviews = reviewService.getStoreReviews(user, storeId, pageable);

    return ResponseEntity.ok(ApiResponse.from(HttpStatus.OK, "가게 리뷰 조회 성공", storeReviews));
  }

  // 리뷰 생성하기
  @PostMapping("/reviews")
  public ResponseEntity<ApiResponse<ReviewCreateResponseDto>> createReview(
      @AuthenticationPrincipal UserDetailsImpl userDetails,
      @RequestBody ReviewCreateRequestDto requestDto) {

    User user = userService.findByUserEmail(userDetails.getUsername());

    log.info("Controller 에서 user : {}", user.getUserEmail());

    ReviewCreateResponseDto responseDto = reviewService.createReview(user, requestDto);

    return ResponseEntity.ok(ApiResponse.from(HttpStatus.OK, "가게 리뷰 등록 성공", responseDto));
  }

  // 리뷰 수정하기
  @PatchMapping("/reviews/{reviewId}")
  public ResponseEntity<ApiResponse<ReviewUpdateResponseDto>> updateReview(
      @AuthenticationPrincipal UserDetailsImpl userDetails,
      @PathVariable UUID reviewId,
      @RequestBody ReviewUpdateRequestDto reviewUpdateRequest
  ) {
    User user = userService.findByUserEmail(userDetails.getUsername());

    ReviewUpdateResponseDto updatedReview = reviewService.updateReview(user, reviewId, reviewUpdateRequest);

    return ResponseEntity.ok(ApiResponse.from(HttpStatus.OK, "가게 리뷰 수정 성공", updatedReview));
  }

  // 리뷰 삭제하기
  @DeleteMapping("/reviews/{reviewId}")
  public ResponseEntity<ApiResponse<ReviewDeleteResponseDto>> deleteReview(
      @AuthenticationPrincipal UserDetailsImpl userDetails,
      @PathVariable UUID reviewId) {
    User user = userService.findByUserEmail(userDetails.getUsername());

    ReviewDeleteResponseDto deleteReview = reviewService.deleteReview(user, reviewId);

    return ResponseEntity.ok(ApiResponse.from(HttpStatus.OK, "가게 리뷰 삭제 성공", deleteReview));
  }

  // 리뷰 평점
  @GetMapping("/review/rating/{storeId}")
  public double getAvgRating(@PathVariable UUID storeId) {
    double avg = reviewService.getAvgRatingByStore(storeId);
    return avg;
  }
}
