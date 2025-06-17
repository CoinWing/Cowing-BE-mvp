package exchange.exchang_BTC.src.order.service;

import exchange.exchang_BTC.coin.entity.CoinEntity;
import exchange.exchang_BTC.coin.repository.CoinRepository;
import exchange.exchang_BTC.src.order.dto.LimitOrderDTO;
import exchange.exchang_BTC.src.order.entity.OrderEntity;
import exchange.exchang_BTC.src.order.repository.OrderRepository;
import exchange.exchang_BTC.src.user.entity.UserEntity;
import exchange.exchang_BTC.src.user.repository.UserRepository;
import exchange.exchang_BTC.src.trade.entity.TradeEntity;
import exchange.exchang_BTC.src.trade.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {


    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CoinRepository coinRepository;
    private final TradeRepository tradeRepository;

    public void createLimitBuyOrder(LimitOrderDTO limitDTO) {
        OrderEntity.OrderType orderType = limitDTO.getOrderType();
        UserEntity user = userRepository.findById(limitDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        CoinEntity coin = coinRepository.findById(limitDTO.getCoinId())
                .orElseThrow(() -> new RuntimeException("Coin not found"));

        OrderEntity limitOrder = new OrderEntity(
                user, coin, limitDTO.getQuantity(), limitDTO.getPrice(), orderType
        );
        orderRepository.save(limitOrder);

        if (orderType == OrderEntity.OrderType.BUY) {
            List<OrderEntity> sellOrders = orderRepository.findActiveSellOrdersByCoinAndPrice(limitDTO.getCoinId(), limitOrder.getPrice());
            BigDecimal remainingQuantity = limitOrder.getQuantity();
            for (OrderEntity sellOrder : sellOrders) {
                if (remainingQuantity.compareTo(BigDecimal.ZERO) < 0) {
                    break;
                }

                BigDecimal sellQuantity = sellOrder.getQuantity();
                BigDecimal matchedQuantity = remainingQuantity.min(sellQuantity);

                if(matchedQuantity.compareTo(BigDecimal.ZERO) > 0) {
                    TradeEntity trade = new TradeEntity(
                            limitOrder, sellOrder, matchedQuantity, sellOrder.getPrice()
                    );
                    tradeRepository.save(trade);

                    sellOrder.setQuantity(sellQuantity.subtract(matchedQuantity));
                    orderRepository.save(sellOrder);

                    remainingQuantity = remainingQuantity.subtract(matchedQuantity);
                }
            }

            limitOrder.setQuantity(remainingQuantity);
            orderRepository.save(limitOrder);
        }

    }

    public void createLimitSellOrder(LimitOrderDTO limitDTO) {
        OrderEntity.OrderType orderType = limitDTO.getOrderType();
        UserEntity user = userRepository.findById(limitDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        CoinEntity coin = coinRepository.findById(limitDTO.getCoinId())
                .orElseThrow(() -> new RuntimeException("Coin not found"));

        OrderEntity limitOrder = new OrderEntity(
                user, coin, limitDTO.getQuantity(), limitDTO.getPrice(), orderType
        );
        orderRepository.save(limitOrder);

        if (orderType == OrderEntity.OrderType.SELL) {
            List<OrderEntity> buyOrders = orderRepository.findActiveBuyOrdersByCoinAndPrice(limitDTO.getCoinId(), limitOrder.getPrice());

            BigDecimal remainingQuantity = limitOrder.getQuantity(); //남은 수량 가져옴

            for (OrderEntity buyOrder : buyOrders) {
                if (remainingQuantity.compareTo(BigDecimal.ZERO) < 0) {
                    break;
                }

                BigDecimal sellQuantity = buyOrder.getQuantity();
                BigDecimal matchedQuantity = remainingQuantity.min(sellQuantity);

                if(matchedQuantity.compareTo(BigDecimal.ZERO) > 0) {
                    TradeEntity trade = new TradeEntity(
                            limitOrder, buyOrder, matchedQuantity, buyOrder.getPrice()
                    );
                    tradeRepository.save(trade);

                    buyOrder.setQuantity(sellQuantity.subtract(matchedQuantity));
                    orderRepository.save(buyOrder);

                    remainingQuantity = remainingQuantity.subtract(matchedQuantity);
                }
            }

            limitOrder.setQuantity(remainingQuantity);
            orderRepository.save(limitOrder);
        }

    }


}

