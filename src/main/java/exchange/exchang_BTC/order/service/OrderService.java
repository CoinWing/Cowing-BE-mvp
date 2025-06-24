package exchange.exchang_BTC.order.service;

import exchange.exchang_BTC.order.api.dto.limit.LimitOrderDto;
import exchange.exchang_BTC.order.api.dto.market.MarketBuyOrderDto;
import exchange.exchang_BTC.order.api.dto.market.MarketSellOrderDto;
import exchange.exchang_BTC.order.domain.entity.Order;
import exchange.exchang_BTC.order.domain.entity.OrderPosition;
import exchange.exchang_BTC.order.domain.entity.OrderType;
import exchange.exchang_BTC.order.config.OrderQueue;
import exchange.exchang_BTC.order.domain.repository.OrderRepository;
import exchange.exchang_BTC.user.User;
import exchange.exchang_BTC.user.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderQueue orderQueue;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public void requestMarketBuyOrder(MarketBuyOrderDto marketBuyOrderDto) {
        Order order = toMarketBuyOrder(marketBuyOrderDto);
        insertToOrderHistory(order);
        orderQueue.addOrder(order);
    }

    public void requestMarketSellOrder(MarketSellOrderDto marketOrderSellDto) {
        Order order = toMarketSellOrder(marketOrderSellDto);
        insertToOrderHistory(order);
        orderQueue.addOrder(order);
    }

    public void requestLimitOrder(LimitOrderDto limitOrderDto) {
        Order order = toLimitOrder(limitOrderDto);
        insertToOrderHistory(order);
        orderQueue.addOrder(order);
    }

    private Order toMarketBuyOrder(MarketBuyOrderDto dto) {
        return Order.builder()
                .marketCode(dto.marketCode())
                .orderType(OrderType.MARKET)
                .orderPosition(OrderPosition.valueOf(dto.position().toUpperCase()))
                .totalQuantity(BigDecimal.ZERO)
                .orderPrice(0L)
                .totalPrice(dto.totalPrice().longValue())
                .orderRequestedAt(LocalDateTime.now())
                .build();
    }

    private Order toMarketSellOrder(MarketSellOrderDto dto) {
        return Order.builder()
                .marketCode(dto.marketCode())
                .orderType(OrderType.MARKET)
                .orderPosition(OrderPosition.valueOf(dto.position().toUpperCase()))
                .totalQuantity(dto.totalQuantity())
                .totalPrice(0L)
                .orderPrice(0L)
                .orderRequestedAt(LocalDateTime.now())
                .build();
    }

    private Order toLimitOrder(LimitOrderDto dto) {
        return Order.builder()
                .marketCode(dto.marketCode())
                .orderType(OrderType.LIMIT)
                .orderPosition(OrderPosition.valueOf(dto.position().toUpperCase()))
                .totalPrice(dto.totalOrderPrice().longValue())
                .orderPrice(dto.orderPrice().longValue())
                .totalQuantity(dto.orderQuantity())
                .orderRequestedAt(LocalDateTime.now())
                .build();
    }

    private void insertToOrderHistory(Order order) {
        orderRepository.save(order);
    }


    @PostConstruct
    void createTestUser() {
        User user = User.builder()
                .email("test@example.com")
                .password("password")
                .nickname("Test User")
                .build();
        userRepository.save(user);
    }
}