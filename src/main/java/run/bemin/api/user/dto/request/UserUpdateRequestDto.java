package run.bemin.api.user.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder

public class UserUpdateRequestDto { // TODO: 추후 주소 테이블 연동 및 상세 주소 관리 기능 구현 예정
  @Pattern(
      regexp = "(^$)|(^[A-Za-z\\d@$!%*?&]{8,15}$)",
      message = "비밀번호는 8자 이상 15자 이하이며, 대소문자, 숫자, 특수문자를 사용할 수 있습니다."
  )
  private String password;

  @Pattern(
      regexp = "(^$)|(^[a-z0-9]{4,10}$)",
      message = "닉네임은 4자 이상 10자 이하이며, 소문자와 숫자만 사용할 수 있습니다."
  )
  private String nickname;

  private String phone;

  private String address;
}
