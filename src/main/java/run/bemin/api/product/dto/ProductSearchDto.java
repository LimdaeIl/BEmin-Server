package run.bemin.api.product.dto;


import java.util.Objects;
import java.util.UUID;

public record ProductSearchDto(
    UUID id,
    int price,
    String title,
    String comment,
    String imageUrl,
    boolean is_hidden
) {
  public ProductSearchDto {
    Objects.requireNonNull(id);
    Objects.requireNonNull(price);
    Objects.requireNonNull(title);
    Objects.requireNonNull(is_hidden);
  }
}