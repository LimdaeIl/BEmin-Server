package run.bemin.api.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SignoutResponseDto {
  private String userEmail;
  private String message;
}

