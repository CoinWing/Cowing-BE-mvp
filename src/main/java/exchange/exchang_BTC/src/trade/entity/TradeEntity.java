package exchange.exchang_BTC.src.trade.entity;

import jakarta.persistence.*;
import lombok.*;
import exchange.exchang_BTC.src.order.entity.OrderEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Trade")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TradeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "t_id")
    private Long id;

    // 매수 주문 (Buy Order)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "o_id_b", referencedColumnName = "oId", nullable = false)
    private OrderEntity buyOrder;

    // 매도 주문 (Sell Order)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "o_id_s", referencedColumnName = "oId", nullable = false)
    private OrderEntity sellOrder;

    @Column(name = "quantity", precision = 20, scale = 8, nullable = false)
    private BigDecimal quantity;

    @Column(name = "price", precision = 20, scale = 8, nullable = false)
    private BigDecimal price;

    @Column(name = "date_time", nullable = false)
    private LocalDateTime dateTime;

    // 생성자 편의용 (체결 시각 기본 현재 시간)
    public TradeEntity(OrderEntity buyOrder, OrderEntity sellOrder, BigDecimal quantity, BigDecimal price) {
        this.buyOrder = buyOrder;
        this.sellOrder = sellOrder;
        this.quantity = quantity;
        this.price = price;
        this.dateTime = LocalDateTime.now();
    }
}