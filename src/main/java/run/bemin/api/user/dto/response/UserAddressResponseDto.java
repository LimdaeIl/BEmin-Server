package run.bemin.api.user.dto.response;

import java.util.UUID;
import lombok.Getter;
import run.bemin.api.user.entity.UserAddress;

@Getter
public class UserAddressResponseDto {
  private UUID userAddressId;
  private String bcode;
  private String jibunAddress;
  private String roadAddress;
  private String detail;
  private boolean isRepresentative;

  public UserAddressResponseDto(UserAddress address) {
    this.userAddressId = address.getId();
    this.bcode = address.getBcode();
    this.jibunAddress = address.getJibunAddress();
    this.roadAddress = address.getRoadAddress();
    this.detail = address.getDetail();
    this.isRepresentative = address.getIsRepresentative();
  }

  public static UserAddressResponseDto fromEntity(UserAddress address) {
    return new UserAddressResponseDto(address);
  }
}
