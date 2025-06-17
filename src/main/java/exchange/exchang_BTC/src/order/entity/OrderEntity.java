package exchange.exchang_BTC.src.order.entity;

import exchange.exchang_BTC.coin.entity.CoinEntity;
import exchange.exchang_BTC.src.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "`Order`")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity {

    public enum OrderType {
        BUY, SELL
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer oId;

    @ManyToOne
    @JoinColumn(name = "u_id", nullable = false)
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "c_id", nullable = false)
    private CoinEntity coin;

    @Column(precision = 20, scale = 8, nullable = false)
    private BigDecimal quantity;

    @Column(precision = 20, scale = 8, nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false)
    private OrderType orderType;

    private LocalDateTime createTime = LocalDateTime.now();

    public OrderEntity(UserEntity user, CoinEntity coin, BigDecimal quantity, BigDecimal price, OrderType orderType) {
        this.user = user;
        this.coin = coin;
        this.quantity = quantity;
        this.price = price;
        this.orderType = orderType;
        this.createTime = LocalDateTime.now();
    }
}

