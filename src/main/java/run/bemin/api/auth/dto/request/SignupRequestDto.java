package run.bemin.api.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import run.bemin.api.user.dto.UserAddressDto;
import run.bemin.api.user.entity.UserRoleEnum;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SignupRequestDto {

  @NotBlank(message = "이메일을 입력해주세요.")
  @Email
  @Pattern(
      regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
      message = "이메일 형식이 올바르지 않습니다."
  )
  private String userEmail;

  @NotBlank(message = "비밀번호를 입력해주세요.")
  @Pattern(
      regexp = "^[A-Za-z\\d@$!%*?&]{8,15}$",
      message = "비밀번호는 8자 이상 15자 이하이며, 대소문자, 숫자, 특수문자를 사용할 수 있습니다."
  )
  private String password;

  @NotBlank(message = "이름을 입력해주세요.")
  private String name;

  @NotBlank(message = "닉네임을 입력해주세요.")
  @Pattern(
      regexp = "^[a-z0-9]{4,10}$",
      message = "닉네임은 4자 이상 10자 이하이며, 소문자와 숫자만 사용할 수 있습니다."
  )
  private String nickname;


  @NotBlank(message = "전화번호를 입력해주세요.")
  private String phone;

  @NotNull(message = "주소 정보를 입력해주세요.")
  private UserAddressDto address;

  @NotNull(message = "회원 역할을 입력해주세요.")
  private UserRoleEnum role;


}
