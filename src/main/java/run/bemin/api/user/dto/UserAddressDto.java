package run.bemin.api.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import run.bemin.api.user.entity.UserAddress;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserAddressDto {
  private String bcode;
  private String jibunAddress;
  private String roadAddress;
  private String detail;

  public static UserAddressDto fromEntity(UserAddress userAddress) {
    return UserAddressDto.builder()
        .bcode(userAddress.getBcode())
        .jibunAddress(userAddress.getJibunAddress())
        .roadAddress(userAddress.getRoadAddress())
        .detail(userAddress.getDetail())
        .build();
  }
}
