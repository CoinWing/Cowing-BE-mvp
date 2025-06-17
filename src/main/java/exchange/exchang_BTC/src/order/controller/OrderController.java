package exchange.exchang_BTC.src.order.controller;

import exchange.exchang_BTC.src.order.dto.LimitOrderDTO;
import exchange.exchang_BTC.src.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;

    @PostMapping("/api/order/limit/buy")
    public ResponseEntity<String> createLimitBuyOrder(@RequestBody LimitOrderDTO limitOrderRequestDTO) {
        orderService.createLimitBuyOrder(limitOrderRequestDTO);
        return ResponseEntity.ok("지정가 매수 주문 완료");
    }

    @PostMapping("/api/order/limit/sell")
    public ResponseEntity<String> createLimitSellOrder(@RequestBody LimitOrderDTO limitOrderRequestDTO) {
        orderService.createLimitSellOrder(limitOrderRequestDTO);
        return ResponseEntity.ok("지정가 매도 주문 완료");
    }
}
