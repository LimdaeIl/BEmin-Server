package run.bemin.api.user.dto.response;

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
  private Boolean isDeleted;

  public UserResponseDto(User user) {
    this.userEmail = user.getUserEmail();
    this.name = user.getName();
    this.nickname = user.getNickname();
    this.phone = user.getPhone();
    this.role = user.getRole();
    // 대표 주소 가져오기 (null 체크 포함)
    this.representativeAddress = user.getRepresentativeAddress() != null
        ? user.getRepresentativeAddress().getRoadAddress()
        : null;
    // isDeleted 값 설정 (User 엔티티에 해당 getter가 있어야 함)
    this.isDeleted = user.getIsDeleted();
  }

  public static UserResponseDto fromEntity(User user) {
    return new UserResponseDto(user);
  }
}
