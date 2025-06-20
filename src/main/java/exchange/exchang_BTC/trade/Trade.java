package exchange.exchang_BTC.trade;

import exchange.exchang_BTC.order.domain.entity.OrderPosition;
import exchange.exchang_BTC.order.domain.entity.OrderType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "order_uuid")
    private String orderUuid;

    private String marketCode;

    @Enumerated(EnumType.STRING)
    private OrderType orderType;

    @Enumerated(EnumType.STRING)
    private OrderPosition orderPosition;

    @Column(nullable = false, name = "trade_price")
    private Long tradePrice;

    @Column(nullable = false, name = "trade_quantity")
    private BigDecimal tradeQuantity;

    @Column(nullable = false)
    private LocalDateTime concludedAt;

    @PrePersist
    public void prePersist() {
        this.concludedAt = LocalDateTime.now();
    }

    @Builder
    public Trade(String marketCode, OrderType orderType, OrderPosition orderPosition, Long tradePrice, BigDecimal tradeQuantity) {
        this.marketCode = marketCode;
        this.orderType = orderType;
        this.orderPosition = orderPosition;
        this.tradePrice = tradePrice;
        this.tradeQuantity = tradeQuantity;
    }
}
