package exchange.exchang_BTC.order.service;

import exchange.exchang_BTC.config.OrderQueue;
import exchange.exchang_BTC.order.api.dto.upbit.OrderbookDto;
import exchange.exchang_BTC.order.api.dto.upbit.OrderbookUnitDto;
import exchange.exchang_BTC.order.domain.entity.Order;
import exchange.exchang_BTC.order.domain.entity.OrderPosition;
import exchange.exchang_BTC.order.domain.entity.Trade;
import exchange.exchang_BTC.order.domain.repository.TradeRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderProcessor {

    private final OrderQueue orderQueue;
    private final RealTimeOrderBook realTimeOrderBook;
    private final TradeRepository tradeRepository;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @PostConstruct
    public void startProcessing() {
        executor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Order order = orderQueue.takeOrder();
                    process(order);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    log.error("Order processing was interrupted.", e);
                }
            }
        });
    }

    private void executeMarketOrderProcessing(Order order, List<OrderbookUnitDto> sortedUnits) {

        boolean isBuyOrder = order.getOrderPosition() == OrderPosition.BUY;
        BigDecimal remaining = BigDecimal.ZERO;
        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal totalPrice = BigDecimal.ZERO;
        String reason;

        if (isBuyOrder) {
            remaining = BigDecimal.valueOf(order.getTotalPrice());
            reason = OrderPosition.BUY.name();
        } else {
            remaining = order.getTotalQuantity();
            reason = OrderPosition.SELL.name();
        }

        for (OrderbookUnitDto unit : sortedUnits) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal currentPrice = isBuyOrder ? BigDecimal.valueOf(unit.askPrice()) : BigDecimal.valueOf(unit.bidPrice());
            BigDecimal currentSize = isBuyOrder ? BigDecimal.valueOf(unit.askSize()) : BigDecimal.valueOf(unit.bidSize());

            BigDecimal tradeQuantity;
            if (isBuyOrder) {
                if (currentPrice.compareTo(BigDecimal.ZERO) == 0) continue; // 0으로 나누기 방지
                BigDecimal possibleQuantity = remaining.divide(currentPrice, 8, RoundingMode.DOWN);
                tradeQuantity = possibleQuantity.min(currentSize);
            } else {
                tradeQuantity = remaining.min(currentSize);
            }

            if (tradeQuantity.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal tradeAmount = tradeQuantity.multiply(currentPrice);

            remaining = isBuyOrder ? remaining.subtract(tradeAmount) : remaining.subtract(tradeQuantity);
            totalQuantity = totalQuantity.add(tradeQuantity);
            totalPrice = totalPrice.add(tradeAmount);

            processTrade(order, tradeQuantity, tradeAmount);
        }

        if (remaining.compareTo(BigDecimal.ZERO) > 0) {
            log.warn("{} 거래 물량 부족으로 부분 체결됨. 지금까지 체결된 수량: {} 지금까지 체결된 금액: {}",
                    reason, totalQuantity, totalPrice);
        } else {
            log.info("{} 거래 완료. 총 체결 수량: {}, 총 체결 금액: {}", reason, totalQuantity, totalPrice);
        }
    }

    //지정가 매매는 주문 요청 내역(주문 가격, 수량)과 정확히 일치하지 않으면 채결할 수 없다.
    private void executeLimitOrderProcessing(Order order, List<OrderbookUnitDto> sortedUnits) {
        boolean isBuyOrder = order.getOrderPosition() == OrderPosition.BUY;
        BigDecimal remaining = order.getTotalQuantity();
        BigDecimal totalFilledQuantity = BigDecimal.ZERO;
        BigDecimal totalPrice = BigDecimal.ZERO;
        BigDecimal lastPrice = BigDecimal.ZERO;

        for (OrderbookUnitDto unit : sortedUnits) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
            BigDecimal price = isBuyOrder
                    ? BigDecimal.valueOf(unit.askPrice())
                    : BigDecimal.valueOf(unit.bidPrice());
            if (isBuyOrder && price.compareTo(BigDecimal.valueOf(order.getOrderPrice())) > 0) break;
            if (!isBuyOrder && price.compareTo(BigDecimal.valueOf(order.getOrderPrice())) < 0) break;

            BigDecimal size = isBuyOrder
                    ? BigDecimal.valueOf(unit.askSize())
                    : BigDecimal.valueOf(unit.bidSize());
            BigDecimal tradeQuantity = remaining.min(size);
            BigDecimal tradeAmount = tradeQuantity.multiply(price);

            remaining = remaining.subtract(tradeQuantity);
            totalFilledQuantity = totalFilledQuantity.add(tradeQuantity);
            totalPrice = totalPrice.add(tradeAmount);
            lastPrice = price;

            processTrade(order, tradeQuantity, tradeAmount);
        }

        if (remaining.compareTo(BigDecimal.ZERO) > 0 && lastPrice.compareTo(BigDecimal.ZERO) > 0) {
            // 남은 수량을 마지막 체결 가격으로 가정 체결
            BigDecimal assumedAmount = remaining.multiply(lastPrice);
            totalFilledQuantity = totalFilledQuantity.add(remaining);
            totalPrice = totalPrice.add(assumedAmount);
            processTrade(order, remaining, assumedAmount);
            remaining = BigDecimal.ZERO;
            log.info("잔여 수량 {}를 마지막 가격 {}으로 가정 체결함", remaining, lastPrice);
        }

        log.info("지정가 주문 체결 완료. OrderId={}, 체결수량={}, 체결금액={}",
                order.getId(), totalFilledQuantity, totalPrice);
    }

    private void process (Order order){
        log.info("Processing order: {}", order);
        String marketCode = order.getMarketCode();

        OrderbookDto orderbookDto = realTimeOrderBook.getOrderBook(order.getMarketCode());
        List<OrderbookUnitDto> units = orderbookDto.orderbookUnitDtoList();

        switch (order.getOrderType()) {
            case MARKET:
                if (order.getOrderPosition() == OrderPosition.BUY) {
                    // 매도 호가 기준 오름차순
                    List<OrderbookUnitDto> sortedMarketAskUnits = units.stream().sorted(Comparator.comparingDouble(OrderbookUnitDto::askPrice)).toList();
                    executeMarketOrderProcessing(order, sortedMarketAskUnits);
                } else {
                    // 매수 호가 기준 내림차순
                    List<OrderbookUnitDto> sortedMarketBidUnits = units.stream()
                            .sorted(Comparator.comparingDouble(OrderbookUnitDto::bidPrice).reversed())
                            .toList();
                    executeMarketOrderProcessing(order, sortedMarketBidUnits);
                }
                log.info("[Market Order Executed] ID: {}, Market: {}", order.getId(), marketCode);
                break;

            case LIMIT:

                if (order.getOrderPosition() == OrderPosition.BUY) {
                    // 매도 호가 중 사용자가 입력한 것보다 저렴한 것만 오름차순
                    List<OrderbookUnitDto> sortedLimitAskUnits = units.stream()
                            .filter(unit -> BigDecimal.valueOf(unit.askPrice()).compareTo(BigDecimal.valueOf(order.getOrderPrice())) <= 0) // 지정가 이하만
                            .sorted(Comparator.comparingDouble(OrderbookUnitDto::askPrice)) // 오름차순 정렬
                            .toList();
                    executeLimitOrderProcessing(order, sortedLimitAskUnits);


                } else {
                    // 매수 호가 중 사용자가 입력한 것보다 비싼 것만 내림차순
                    List<OrderbookUnitDto> sortedLimitBidUnits = units.stream()
                            .filter(unit -> BigDecimal.valueOf(unit.bidPrice()).compareTo(BigDecimal.valueOf(order.getOrderPrice())) >= 0) // 지정가 이상만
                            .sorted(Comparator.comparingDouble(OrderbookUnitDto::bidPrice).reversed()) // 높은 가격부터 정렬
                            .toList();
                    executeLimitOrderProcessing(order, sortedLimitBidUnits);

                }
                log.info("[Limit Order Executed] ID: {}, Limit: {}", order.getId(), marketCode);
                break;

        }
    }


    private void processTrade (Order order, BigDecimal tradeQuantity, BigDecimal tradePrice){
        tradeRepository.save(
                Trade.builder()
                        .orderUuid(order.getUuid())
                        .marketCode(order.getMarketCode())
                        .orderType(order.getOrderType())
                        .orderPosition(order.getOrderPosition())
                        .tradeQuantity(tradeQuantity)
                        .tradePrice(tradePrice.longValue())
                        .build()
        );
        log.info("Trade converted from order Is recorded: {}", order);
    }

}
