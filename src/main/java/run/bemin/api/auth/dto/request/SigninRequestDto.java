package run.bemin.api.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
@Builder
public class SigninRequestDto {

  @NotBlank(message = "이메일을 입력해주세요.")
  @Email(message = "올바른 이메일 형식이어야 합니다.")
  @Pattern(
      regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$",
      message = "이메일 형식이 올바르지 않습니다."
  )
  private String userEmail;

  @NotBlank(message = "비밀번호를 입력해주세요.")
//    @Pattern(regexp = "^[A-Za-z\\d@$!%*?&]{8,15}$")
  private String password;
}
