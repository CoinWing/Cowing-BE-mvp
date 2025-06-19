package exchange.exchang_BTC.order.api.dto.market;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MarketSellOrderDto(
        @JsonProperty("coin_ticker") @NotNull @NotBlank String coinTicker,
        @JsonProperty("position") @NotNull @NotBlank String position,
        @JsonProperty("total_quantity") @NotNull @Positive BigDecimal totalQuantity,
        @JsonProperty("market_code") @NotNull @NotBlank String marketCode,
        LocalDateTime orderRequestedAt
) {
    public MarketSellOrderDto {
        orderRequestedAt = LocalDateTime.now();
    }
}
