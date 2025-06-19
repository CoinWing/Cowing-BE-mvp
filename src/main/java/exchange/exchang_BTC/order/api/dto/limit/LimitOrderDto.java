package exchange.exchang_BTC.order.api.dto.limit;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;


/*
 * 시장가 (market) 주문과 다르게 지정가 주문은 매수와 매도 시 동일한 속성을 사용한다. 따라서 dto를 분리하지 않았다.
 */
public record LimitOrderDto(
        @JsonProperty("market_code") @NotNull @NotBlank String marketCode,
        @JsonProperty("coin_ticker") @NotNull @NotBlank String coinTicker,
        @JsonProperty("order_price") @NotNull @Positive BigDecimal orderPrice,
        @JsonProperty("position") @NotNull @NotBlank String position,
        @JsonProperty("order_quantity") @NotNull @Positive BigDecimal orderQuantity,
        @JsonProperty("total_order_price") @NotNull @Positive BigDecimal totalOrderPrice,
        LocalDateTime orderRequestedAt
) {
    public LimitOrderDto {
        orderRequestedAt = LocalDateTime.now();
    }
}
