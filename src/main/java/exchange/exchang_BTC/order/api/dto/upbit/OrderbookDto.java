package exchange.exchang_BTC.order.api.dto.upbit;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderbookDto(
        @JsonProperty("market") @NotNull @NotBlank String market,
        @JsonProperty("total_ask_size") @NotNull @NotBlank Double totalAskSize,
        @JsonProperty("total_bid_size") @NotNull @NotBlank Double totalBidSize,
        @JsonProperty("orderbook_units") @NotNull @NotBlank List<OrderbookUnitDto> orderbookUnitDtoList

) {
}
