package run.bemin.api.category.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import run.bemin.api.general.auditing.AuditableEntity;
import run.bemin.api.store.entity.StoreCategory;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "p_category")
public class Category extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Column(name = "category_id", nullable = false, updatable = false, unique = true)
  private UUID id;

  @Column(name = "name", nullable = false, unique = true)
  private String name;

  @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = false)
  private final List<StoreCategory> storeCategories = new ArrayList<>();

  @Column(name = "is_deleted", nullable = false)
  private Boolean isDeleted = false;

  @Column(name = "deleted_by")
  private String deletedBy;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  private Category(String name) {
    this.name = name;
  }

  public static Category create(String name) {
    return new Category(name);
  }

  public void update(String updatedBy, String name, Boolean isDeleted) {
    this.name = name;
    if (isDeleted != null) {
      this.isDeleted = isDeleted;
    }
  }

  public void softDelete(String deletedBy) {
    this.isDeleted = true;
    this.deletedBy = deletedBy;
    this.deletedAt = LocalDateTime.now();
  }
}
