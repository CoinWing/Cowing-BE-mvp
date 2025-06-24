package exchange.exchang_BTC.order.api.dto.upbit;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record OrderbookUnitDto(
        @JsonProperty("ask_price") @NotNull @NotBlank Double askPrice,
        @JsonProperty("bid_price") @NotNull @NotBlank Double bidPrice,
        @JsonProperty("ask_size") @NotNull @NotBlank Double askSize,
        @JsonProperty("bid_size") @NotNull @NotBlank Double bidSize

) {
}
