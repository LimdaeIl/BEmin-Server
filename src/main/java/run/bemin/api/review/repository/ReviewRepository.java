package run.bemin.api.review.repository;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import run.bemin.api.review.dto.ReviewResponseDto;
import run.bemin.api.review.entity.Review;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
//  List<Review> findByStore_IdAndStatus(UUID storeId, ReviewStatus status);

  @Query("SELECT COALESCE(AVG(" +
      "CASE r.rating " +
      "WHEN run.bemin.api.review.domain.ReviewRating.ONE THEN 1 " +
      "WHEN run.bemin.api.review.domain.ReviewRating.TWO THEN 2 " +
      "WHEN run.bemin.api.review.domain.ReviewRating.THREE THEN 3 " +
      "WHEN run.bemin.api.review.domain.ReviewRating.FOUR THEN 4 " +
      "WHEN run.bemin.api.review.domain.ReviewRating.FIVE THEN 5 " +
      "ELSE NULL END" +
      "), 0.0) " +
      "FROM Review r " +
      "WHERE r.store.id = :storeId AND r.status = 'ACTIVE'")
  double findAverageRatingByStore(@Param("storeId") UUID storeId);

  // 특정 가게(storeId)에 대한 리뷰 페이징 처리
  Page<Review> findByStoreId(UUID storeId, Pageable pageable);

  @Query(
      "SELECT new run.bemin.api.review.dto.ReviewResponseDto(r.user.userEmail, r.description, r.rating) "
          +
          "FROM Review r " +
          "WHERE r.store.id = :storeId")
  Page<ReviewResponseDto> findReviewsByStoreId(@Param("storeId") UUID storeId, Pageable pageable);
}
