package run.bemin.api.review.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true) // 모든 필드에서 자동 적용
public class ReviewRatingConverter implements AttributeConverter<ReviewRating, String> {

  @Override
  public String convertToDatabaseColumn(ReviewRating attribute) {
    if (attribute == null) {
      return null;
    }
    return attribute.name(); // Enum의 이름(String)을 DB에 저장
  }

  @Override
  public ReviewRating convertToEntityAttribute(String dbData) {
    if (dbData == null) {
      return null;
    }
    try {
      return ReviewRating.valueOf(dbData); // 문자열 → Enum 변환
    } catch (IllegalArgumentException e) {
      throw new RuntimeException("Invalid ReviewRating value in database: " + dbData);
    }
  }

  // 🔥 숫자로 변환하는 메서드 추가
  public static int toNumeric(ReviewRating rating) {
    if (rating == null) {
      return 0;
    }
    return rating.getValue();
  }

}

