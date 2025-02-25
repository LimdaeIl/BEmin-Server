package run.bemin.api.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import run.bemin.api.user.entity.UserRoleEnum;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RefreshResponseDto {
  private String accessToken;
  private String email;
  private String nickname;
  private UserRoleEnum role;
}
