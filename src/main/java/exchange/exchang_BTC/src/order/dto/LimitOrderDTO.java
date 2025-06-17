package exchange.exchang_BTC.src.order.dto;

import exchange.exchang_BTC.src.order.entity.OrderEntity;
import lombok.*;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class LimitOrderDTO {
    private Long userId;
    private Long coinId;
    private BigDecimal quantity;
    private BigDecimal price;
    private OrderEntity.OrderType orderType; // "BUY" 또는 "SELL"
}

