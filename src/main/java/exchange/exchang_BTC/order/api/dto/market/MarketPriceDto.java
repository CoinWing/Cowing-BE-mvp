package exchange.exchang_BTC.order.api.dto.market;

import java.math.BigDecimal;

public record MarketPriceDto(
        String market,
        BigDecimal trade_price
) { }
