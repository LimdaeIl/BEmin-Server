package run.bemin.api.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import run.bemin.api.user.entity.UserRoleEnum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenDto {
  private String accessToken;
  private String refreshToken;
  private long accessTokenExpiresTime;
  @Getter
  private long refreshTokenExpiresTime;

  private String email;
  private String nickname;
  private UserRoleEnum role;

  public String getToken() {
    return accessToken;
  }
}
