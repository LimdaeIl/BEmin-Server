package run.bemin.api.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Objects;

@Schema(description = "상품 추가 요청 DTO")
public record ProductRequestDto(
    int price,
    String title,
    String comment,
    String imageUrl
) {
  public ProductRequestDto {
    Objects.requireNonNull(price);
    Objects.requireNonNull(title);
  }
}
