package run.bemin.api.order.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(columnDefinition = "UUID")
  private UUID orderId;

  @ManyToOne
  @JoinColumn(name = "user_id", referencedColumnName = "userId", nullable = false)
  private User user;

  @ManyToOne
  @JoinColumn(name = "store_id", referencedColumnName = "storeId", nullable = false)
  private Store store;

  @Convert(converter = OrderStatusConverter.class)
  @Column(nullable = false)
  private OrderType orderType;

  @Convert(converter = OrderStatusConverter.class)
  private OrderStatus orderStatus;

  @Column(nullable = false)
  private String storeName;

  @Column
  private String riderTel;

  @Embedded
  private OrderAddress orderAddress; // 주소 관련 클래스

  @Column(columnDefinition = "BOOLEAN DEFAULT false")
  @Builder.Default
  private Boolean cancelled = false;

  /*
   * 추후 audit 필드 및 생성자, 갱신자, 삭제자 구현.
   */

  // null 값 방지를 위한 PrePersist
  @PrePersist
  public void prePersist() {
    if (cancelled == null) {
      cancelled = false;
    }
  }

  // 빌더 클래스 커스터마이징
  // store 설정 시 storeName 자동 설정
  public static class OrderBuilder {
    public OrderBuilder store(Store store) {
      this.store = store;
      this.storeName = store != null ? store.getStoreName() : null;
      return this;
    }
  }
}
