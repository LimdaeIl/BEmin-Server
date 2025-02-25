package run.bemin.api.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserAddressRequestDto {
  @NotBlank(message = "도로명 주소를 입력해주세요.")
  private String roadAddress;
  @NotBlank(message = "지번 주소를 입력해주세요.")
  private String jibunAddress;
  @NotBlank(message = "법정동 코드를 입력해주세요.")
  private String bcode;
  @NotBlank(message = "상세 주소를 입력해주세요.")
  private String detail;
}
