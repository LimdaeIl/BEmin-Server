package run.bemin.api.user.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import run.bemin.api.general.auditing.AuditableEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity(name = "p_user_address")
public class UserAddress extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "user_address_id", unique = true, nullable = false)
  private UUID id;

  @Column(name = "bcode", nullable = false)
  private String bcode; // 법정동/법정리 코드

  @Column(name = "jibun_address", nullable = false)
  private String jibunAddress; // 지번 주소

  @Column(name = "road_address", nullable = false)
  private String roadAddress; // 도로명 주소

  @Column(name = "detail", nullable = false)
  private String detail;

  // 대표 주소 여부 플래그
  @Builder.Default
  @Column(name = "is_representative", nullable = false)
  private Boolean isRepresentative = false;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_email")
  @JsonBackReference // User의 userAddressList에 대한 역참조를 무시
  private User user;

  @Builder.Default
  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted = false;

  // soft delete 필드 추가
  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Column(name = "deleted_by")
  private String deletedBy;

  @Builder
  public UserAddress(String bcode, String jibunAddress, String roadAddress,
                     String detail, Boolean isRepresentative, User user) {
    this.bcode = bcode;
    this.jibunAddress = jibunAddress;
    this.roadAddress = roadAddress;
    this.detail = detail;
    this.isRepresentative = isRepresentative;
    this.user = user;
  }

  public void setRepresentative(Boolean isRepresentative) {
    this.isRepresentative = isRepresentative != null ? isRepresentative : Boolean.FALSE;
  }

  public void softDelete(String deletedBy) {
    this.isDeleted = true;
    this.deletedAt = LocalDateTime.now();
    this.deletedBy = deletedBy;
  }
}
