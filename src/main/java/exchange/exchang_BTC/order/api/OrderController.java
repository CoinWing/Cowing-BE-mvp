package exchange.exchang_BTC.order.api;

import exchange.exchang_BTC.order.api.dto.limit.LimitOrderDto;
import exchange.exchang_BTC.order.api.dto.market.MarketOrderBuyDto;
import exchange.exchang_BTC.order.api.dto.market.MarketSellOrderDto;
import exchange.exchang_BTC.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/market/buy")
    public ResponseEntity<String> createMarketBuyOrder(@Valid @RequestBody MarketOrderBuyDto marketOrderBuyDto) {
        orderService.requestMarketBuyOrder(marketOrderBuyDto);
        return ResponseEntity.ok("Market order request accepted.");
    }

    @PostMapping("/market/sell")
    public ResponseEntity<String> createMarketSellOrder(@Valid @RequestBody MarketSellOrderDto marketOrderSellDto) {
        orderService.requestMarketSellOrder(marketOrderSellDto);
        return ResponseEntity.ok("Market order request accepted.");
    }

    @PostMapping("/limit")
    public ResponseEntity<String> createLimitOrder(@Valid @RequestBody LimitOrderDto limitOrderDto) {
        orderService.requestLimitOrder(limitOrderDto);
        return ResponseEntity.ok("Limit order request accepted.");
    }
}
