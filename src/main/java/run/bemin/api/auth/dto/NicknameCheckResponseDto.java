package run.bemin.api.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NicknameCheckResponseDto {
  private boolean isDuplicate;
  private String message;
  private String code;
}
