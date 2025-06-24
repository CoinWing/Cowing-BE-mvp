package exchange.exchang_BTC.order.service;

import exchange.exchang_BTC.order.api.dto.upbit.OrderbookDto;
import exchange.exchang_BTC.order.api.dto.upbit.OrderbookUnitDto;
import exchange.exchang_BTC.order.config.OrderQueue;
import exchange.exchang_BTC.order.domain.entity.Order;
import exchange.exchang_BTC.order.domain.entity.OrderPosition;
import exchange.exchang_BTC.trade.Trade;
import exchange.exchang_BTC.trade.TradeRepository;
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
import java.util.concurrent.TimeUnit;

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

    private void process(Order order) {
        log.info("Processing order: {}", order);
        String marketCode = order.getMarketCode();

        switch (order.getOrderType()) {
            case MARKET:
                OrderbookDto orderbookDto = realTimeOrderBook.getOrderBook(order.getMarketCode());
                List<OrderbookUnitDto> units = orderbookDto.orderbookUnitDtoList();


                if (order.getOrderPosition() == OrderPosition.BUY) {

                    BigDecimal remainingMoney = BigDecimal.valueOf(order.getTotalPrice());
                    BigDecimal totalQuantity = BigDecimal.ZERO;

                    // 나중에 포트폴리오에 총 구매액을 넣을 수 있게 추가
                    BigDecimal weightedTradePriceSum = BigDecimal.ZERO;

                    // 매도 호가 기준 오름차순
                    List<OrderbookUnitDto> sortedUnits = units.stream().sorted(Comparator.comparingDouble(OrderbookUnitDto::askPrice)).toList();


                    // 호가 목록 돌면서 비교
                    for (OrderbookUnitDto unit : sortedUnits) {
                        // 주문 금액 0되면 끝내기
                        if (remainingMoney.compareTo(BigDecimal.ZERO) <= 0) break;

                        BigDecimal currentPrice = BigDecimal.valueOf(unit.askPrice());
                        BigDecimal currentSize = BigDecimal.valueOf(unit.askSize());

                        // 구매 가격을 현재가로 나눠서 구매할 수 있는 수량을 계산한다.
                        BigDecimal possibleQuantity = remainingMoney.divide(currentPrice, 8, RoundingMode.DOWN);

                        // 매도 물량과, 구매 가능 수량 중 더 작은 쪽이 거래 수량이 된다.
                        BigDecimal tradeQuantity = possibleQuantity.min(currentSize);

                        // 주문 금액이 남긴 했는데 소수점 내림으로 0되는 경우는 끝내기
                        if (tradeQuantity.compareTo(BigDecimal.ZERO) <= 0) break;

                        // 거래 수량 * 현재가를 해서 얼마에 샀는지 계산
                        BigDecimal tradeAmount = tradeQuantity.multiply(currentPrice);

                        // 구매한 가격 만큼 주문 금액에서 뺀다
                        remainingMoney = remainingMoney.subtract(tradeAmount);

                        // 부분 체결된 양을 계속 저장해서 총 체결 수량을 계산한다
                        totalQuantity = totalQuantity.add(tradeQuantity);

                        //부분 체결된 가격을 계속 저장해서 총 구매액을 계산한다
                        weightedTradePriceSum = weightedTradePriceSum.add(tradeAmount);
                        log.info("weighed Trade: {}", weightedTradePriceSum);

                        order.setTotalQuantity(tradeQuantity);
                        order.setTotalPrice(tradeAmount.longValue());

                        processTrade(order);
                    }

                }
                else {
                    BigDecimal remainingQuantity = order.getTotalQuantity(); // 남은 매도 수량
                    // 나중에 포트폴리오에 총 판매 수량을 넣을 수 있게 추가
                    BigDecimal totalEarned = BigDecimal.ZERO;

                    // 매수 호가 높은 순으로 정렬
                    List<OrderbookUnitDto> sortedBidUnits = units.stream()
                            .sorted(Comparator.comparingDouble(OrderbookUnitDto::bidPrice).reversed())
                            .toList();

                    for (OrderbookUnitDto unit : sortedBidUnits) {
                        if (remainingQuantity.compareTo(BigDecimal.ZERO) <= 0) break;

                        BigDecimal currentPrice = BigDecimal.valueOf(unit.bidPrice());
                        BigDecimal currentSize = BigDecimal.valueOf(unit.bidSize());

                        BigDecimal tradeQuantity = remainingQuantity.min(currentSize);

                        if (tradeQuantity.compareTo(BigDecimal.ZERO) <= 0) break;

                        BigDecimal tradeAmount = tradeQuantity.multiply(currentPrice);
                        remainingQuantity = remainingQuantity.subtract(tradeQuantity);
                        totalEarned = totalEarned.add(tradeAmount);
                        log.info("total Earned: {}", totalEarned);

                        // 체결 정보 기록
                        order.setTotalQuantity(tradeQuantity);
                        order.setTotalPrice(tradeAmount.longValue());

                        processTrade(order); // 체결 단위 저장

                    }
                }
                log.info("[Market Order Executed] ID: {}, Market: {}", order.getId(), marketCode);
                break;


            case LIMIT:
                // 3-2. 지정가 매매: 5초 대기 후 체결 간주
                try {
                    log.info("[Limit Order Watching] ID: {}, Market: {}, Target Price: {}", order.getId(), marketCode, order.getOrderPrice());
                    TimeUnit.SECONDS.sleep(5); // 5초 동안 watch하는 것을 시뮬레이션
                    log.info("[Limit Order Executed] ID: {}, Market: {}, After 5s", order.getId(), marketCode);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }

                processTrade(order);
                break;
        }
    }

    private void processTrade(Order order) {
        tradeRepository.save(
                Trade.builder()
                        .orderUuid(order.getUuid())
                        .marketCode(order.getMarketCode())
                        .orderType(order.getOrderType())
                        .orderPosition(order.getOrderPosition())
                        .tradeQuantity(order.getTotalQuantity())
                        .tradePrice(order.getTotalPrice())
                        .build()
        );
        log.info("Trade converted from order Is recorded: {}", order);
    }
}