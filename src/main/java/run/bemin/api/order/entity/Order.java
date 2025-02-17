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
import run.bemin.api.user.entity.User;

@Entity
@Getter
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
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column
  private String storeId;

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

  public void changeOrderAddress(OrderAddress newAddress) {
    if (newAddress == null) {
      throw new IllegalArgumentException("Order address cannot be null");
    }
    this.orderAddress = newAddress;
  }

  public void changeOrderStatus(OrderStatus newStatus) {
    if (newStatus == null) {
      throw new IllegalArgumentException("Order status cannot be null");
    }
    this.orderStatus = newStatus;
  }

  public void changeRiderTel(String newTel) {
    this.riderTel = newTel;
  }

  public void cancelOrder() {
    this.cancelled = true;
    this.orderStatus = OrderStatus.CANCELLED;
  }
}
