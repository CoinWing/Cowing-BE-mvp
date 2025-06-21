package exchange.exchang_BTC.order.api.dto.market;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record MarketBuyOrderDto(
        @JsonProperty("coin_ticker") @NotNull @NotBlank String coinTicker,
        @JsonProperty("position") @NotNull @NotBlank String position,
        @JsonProperty("total_price") @NotNull Integer totalPrice,
        @JsonProperty("market_code") @NotNull @NotBlank String marketCode,
        LocalDateTime orderRequestedAt
) {
    public MarketBuyOrderDto {
        orderRequestedAt =LocalDateTime.now();
    }
}
