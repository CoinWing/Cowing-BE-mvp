package exchange.exchang_BTC.trade;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name = "order_uuid")
    private String orderUuid;

    @Column(nullable = false)
    private LocalDateTime concludedAt;

    @PrePersist
    public void prePersist() {
        this.concludedAt = LocalDateTime.now();
    }

    @Builder
    public Trade(String orderUuid, String marketCode, OrderType orderType, OrderPosition orderPosition, Long tradePrice, BigDecimal tradeQuantity) {
        this.orderUuid = orderUuid;
        this.marketCode = marketCode;
        this.orderType = orderType;
        this.orderPosition = orderPosition;
        this.tradePrice = tradePrice;
        this.tradeQuantity = tradeQuantity;

    }
}
