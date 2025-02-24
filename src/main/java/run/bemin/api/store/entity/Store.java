package run.bemin.api.store.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import run.bemin.api.category.entity.Category;
import run.bemin.api.general.auditing.AuditableEntity;
import run.bemin.api.product.entity.Product;
import run.bemin.api.review.entity.Review;
import run.bemin.api.user.entity.User;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "p_store")
public class Store extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "store_id", nullable = false, updatable = false, unique = true)
  private UUID id;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "phone")
  private String phone;

  @Column(name = "minimum_price")
  private Integer minimumPrice;

  @Column(name = "is_active", nullable = false)
  @ColumnDefault("true")
  private Boolean isActive = true;

  @Column(name = "rating")
  private Float rating = 0.0F;

  // 가게 주소와 연관 (Cascade 옵션을 통해 주소 생명주기를 함께 관리)
  @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @JoinColumn(name = "store_address_id")
  private StoreAddress storeAddress;

  // 소유자(User)와의 명시적 연관관계; User 엔티티의 기본키(예: userEmail)를 기준으로 매핑
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_email", nullable = false)
  private User owner;

  // 가게와 카테고리 간의 연결 (StoreCategory 엔티티를 통해 다대다 관계 관리)
  @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = false)
  private final List<StoreCategory> storeCategories = new ArrayList<>();

  @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = false)
  private final List<Review> reviews = new ArrayList<>();

  @OneToMany(mappedBy = "store", cascade = CascadeType.ALL, orphanRemoval = false)
  private final List<Product> products = new ArrayList<>();

  @Column(name = "is_deleted", nullable = false)
  @ColumnDefault("false")
  private Boolean isDeleted = false;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Column(name = "deleted_by")
  private String deletedBy;

  @Builder
  public Store(String name,
               String phone,
               Integer minimumPrice,
               boolean isActive,
               StoreAddress storeAddress,
               User owner) {
    this.name = name;
    this.phone = phone;
    this.minimumPrice = minimumPrice;
    this.isActive = isActive;
    this.storeAddress = storeAddress;
    this.owner = owner;
  }

  public static Store create(String name,
                             String phone,
                             Integer minimumPrice,
                             boolean isActive,
                             StoreAddress storeAddress,
                             User owner) {
    return Store.builder()
        .name(name)
        .phone(phone)
        .minimumPrice(minimumPrice)
        .isActive(isActive)
        .storeAddress(storeAddress)
        .owner(owner)
        .build();
  }


  // 카테고리 연결 헬퍼 메서드. 첫 번째 추가되는 카테고리를 primary 로 설정
  public void addCategory(Category category) {
    boolean isPrimary = this.storeCategories.isEmpty();
    StoreCategory storeCategory = StoreCategory.create(this, category, isPrimary);
    this.storeCategories.add(storeCategory);
  }


  // 가게 정보를 업데이트
  public void update(String name, String phone, Integer minimumPrice, Boolean isActive, User owner) {
    this.name = name;
    this.phone = phone;
    this.minimumPrice = minimumPrice;
    this.isActive = isActive;
    this.owner = owner;
  }

  // 가게 활성화 여부 업데이트
  public void updateIsActive(Boolean isActive) {
    this.isActive = isActive;
  }

  // 가게 최소금액 업데이트
  public void updateMinimumPrice(Integer minimumPrice) {
    this.minimumPrice = minimumPrice;
  }

  // 가게 이름 업데이트
  public void updateName(String name) {
    this.name = name;
  }

  // 가게 주인 업데이트
  public void updateOwner(User newOwner) {
    this.owner = newOwner;
  }

  // 가게 전화번호 업데이트
  public void updatePhone(String newPhone) {
    this.phone = newPhone;
  }

  // 가게 평점 업데이트
  public void updateRating(float rating) {
    this.rating = rating;
  }

  // 소프트 삭제 처리: 삭제 플래그 및 삭제 시간, 삭제자 정보를 기록
  public void softDelete(String deletedBy) {
    this.isDeleted = true;
    this.deletedAt = LocalDateTime.now();
    this.deletedBy = deletedBy;
  }


  // 카테고리 업데이트: 새로운 카테고리 목록에 따라 기존 연결을 소프트 삭제, 업데이트 또는 신규 추가
  public void updateCategories(List<Category> newCategories, String currentUser) {
    var newCategoryMap = newCategories.stream()
        .collect(Collectors.toMap(Category::getId, category -> category));

    // 기존 연결 중 새 목록에 없는 항목은 소프트 삭제 처리
    for (StoreCategory storeCategory : storeCategories) {
      if (!newCategoryMap.containsKey(storeCategory.getCategory().getId()) && !storeCategory.isDeleted()) {
        storeCategory.softDelete(currentUser);
      }
    }

    // 새 목록에 대해, 기존 연결이 있으면 업데이트(또는 복원), 없으면 신규 연결 추가
    for (int i = 0; i < newCategories.size(); i++) {
      Category newCategory = newCategories.get(i);
      boolean isPrimary = (i == 0);
      var existingOpt = storeCategories.stream()
          .filter(sc -> sc.getCategory().getId().equals(newCategory.getId()))
          .findFirst();

      if (existingOpt.isPresent()) {
        StoreCategory sc = existingOpt.get();
        if (sc.isDeleted()) {
          sc.restore(currentUser, isPrimary);
        } else {
          sc.update(isPrimary);
        }
      } else {
        StoreCategory newSC = StoreCategory.create(this, newCategory, isPrimary);
        storeCategories.add(newSC);
      }
    }
  }
}
