package exchange.exchang_BTC.order.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Table(name = "order_history")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "uuid", nullable = false, updatable = false)
    private String uuid;

    @Column(nullable = false)
    private String marketCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderPosition orderPosition;

    // 지정가 주문용 주문 금액 변수
    @Column(nullable = false, updatable = false)
    private Long orderPrice;

    // 시장가 주문용 총 금액 변수
    @Column(nullable = false, updatable = false)
    private Long totalPrice;

    @Column(nullable = false, updatable = false)
    private BigDecimal totalQuantity;

    @Column(nullable = false, updatable = false)
    private LocalDateTime orderRequestedAt;

    @PrePersist
    public void prePersist() {
        this.uuid = this.uuid == null ? UUID.randomUUID().toString() : this.uuid;
    }

    @Builder
    public Order(String marketCode, OrderType orderType, OrderPosition orderPosition, Long orderPrice, BigDecimal totalQuantity, Long totalPrice, LocalDateTime orderRequestedAt) {
        this.marketCode = marketCode;
        this.orderType = orderType;
        this.orderPosition = orderPosition;
        this.orderPrice = orderPrice;
        this.totalQuantity = totalQuantity;
        this.totalPrice = totalPrice;
        this.orderRequestedAt = orderRequestedAt;
    }
}
