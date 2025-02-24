package run.bemin.api.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import run.bemin.api.user.entity.UserRoleEnum;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SigninResponseDto {
  private String accessToken;
  private String email;
  private String nickname;
  private UserRoleEnum role;
}
