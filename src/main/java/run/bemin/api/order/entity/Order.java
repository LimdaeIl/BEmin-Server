package run.bemin.api.order.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import run.bemin.api.general.auditing.AuditableEntity;
import run.bemin.api.order.exception.OrderNullException;
import run.bemin.api.order.exception.OrderUserNotFoundException;
import run.bemin.api.user.entity.User;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "orders")
@SQLDelete(sql = "UPDATE orders SET deleted = true, deleted_at = now(), deleted_by = ? WHERE order_id = ?")
public class Order extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(name = "order_id", columnDefinition = "UUID")
  private UUID orderId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "user_email", nullable = false)
  private String userEmail;

  @Column
  private UUID storeId;

  @Convert(converter = OrderTypeConverter.class)
  @Column(nullable = false)
  private OrderType orderType;

  @Convert(converter = OrderStatusConverter.class)
  @Column(nullable = false)
  private OrderStatus orderStatus = OrderStatus.PENDING;

  @Column(nullable = false)
  private String storeName;

  @Column
  private String riderTel;

  @Embedded
  private OrderAddress orderAddress; // 주소 관련 클래스

  @Column(columnDefinition = "BOOLEAN DEFAULT false")
  private Boolean cancelled = false;

  @OneToMany(mappedBy = "order", cascade = {CascadeType.PERSIST, CascadeType.REMOVE}, orphanRemoval = true)
  @JsonManagedReference
  private List<OrderDetail> orderDetails = new ArrayList<>();

  @Column(columnDefinition = "BOOLEAN DEFAULT false")
  private Boolean deleted = false;

  @Column(name = "deleted_at")
  private LocalDateTime deletedAt;

  @Column(name = "deleted_by")
  private String deletedBy;

  @Column(name = "total_price", nullable = false)
  private int totalPrice;

  @Builder
  public Order(User user,
               UUID storeId,
               OrderType orderType,
               String storeName,
               OrderAddress orderAddress) {
    this.setUser(user);
    this.storeId = storeId;
    this.orderType = orderType;
    this.storeName = storeName;
    this.orderAddress = orderAddress;
    this.cancelled = false;
    this.deleted = false;
  }

  protected void setUser(User user) {
    if (user == null) {
      throw new OrderUserNotFoundException("User not found");
    }
    this.user = user;
    this.userEmail = user.getUserEmail();
  }

  public void changeOrderAddress(OrderAddress newAddress) {
    if (newAddress == null) {
      throw new OrderNullException("Order address cannot be null");
    }
    this.orderAddress = newAddress;
  }

  public void changeOrderStatus(OrderStatus newStatus) {
    if (newStatus == null) {
      throw new OrderNullException("Order status cannot be null");
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

  /**
   * 주문 상세 추가
   */
  public void addOrderDetail(OrderDetail orderDetail) {
    this.orderDetails.add(orderDetail);
    this.totalPrice += orderDetail.getPrice() * orderDetail.getQuantity();
    orderDetail.setOrder(this);
  }

  /**
   * 소프트 딜리트
   */
  public void softDelete(String deletedBy) {
    this.deleted = true;
    this.deletedAt = LocalDateTime.now();
    this.deletedBy = deletedBy;
  }

  /**
   * 삭제 여부 확인
   */
  public boolean isDeleted() {
    return this.deleted;
  }
}
