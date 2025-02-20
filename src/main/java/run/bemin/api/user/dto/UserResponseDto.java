package run.bemin.api.user.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import run.bemin.api.user.entity.User;
import run.bemin.api.user.entity.UserRoleEnum;

@Getter
@AllArgsConstructor
public class UserResponseDto {
  private String userEmail;
  private String name;
  private String nickname;
  private String phone;
  private String representativeAddress; // 대표 주소
  private UserRoleEnum role;

  public UserResponseDto(User user) {
    this.userEmail = user.getUserEmail();
    this.name = user.getName();
    this.nickname = user.getNickname();
    this.phone = user.getPhone();
    this.role = user.getRole();
    // 대표 주소를 representativeAddress에서 가져옴 (null 체크 포함)
    this.representativeAddress = user.getRepresentativeAddress() != null
        ? user.getRepresentativeAddress().getRoadAddress()
        : null;
  }
}
