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

    private void processBuyOrder(Order order, List<OrderbookUnitDto> sortedUnits) {
        BigDecimal remainingMoney = BigDecimal.valueOf(order.getTotalPrice());
        BigDecimal totalQuantity = BigDecimal.ZERO;

        // 나중에 포트폴리오에 총 구매액을 넣을 수 있게 추가
        BigDecimal weightedTradePriceSum = BigDecimal.ZERO;
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


            order.setTotalQuantity(tradeQuantity);
            order.setTotalPrice(tradeAmount.longValue());

            processTrade(order);
        }
        if (remainingMoney.compareTo(BigDecimal.ZERO) > 0) {
            log.warn("거래 물량 부족으로 부분 체결됨. 지금까지 체결된 수량: {} 지금까지 체결된 금액: {}",totalQuantity, weightedTradePriceSum);
        }
    }

    private void processSellOrder(Order order, List<OrderbookUnitDto> sortedUnits) {
        BigDecimal remainingQuantity = order.getTotalQuantity(); // 남은 매도 수량
        // 나중에 포트폴리오에 총 판매 수량을 넣을 수 있게 추가
        BigDecimal totalQuantity = BigDecimal.ZERO;
        BigDecimal weightedTradePriceSum = BigDecimal.ZERO;

        for (OrderbookUnitDto unit : sortedUnits) {
            // 매도 수량만큼 주문 체결됐으면 끝내기
            if (remainingQuantity.compareTo(BigDecimal.ZERO) <= 0) break;

            BigDecimal currentPrice = BigDecimal.valueOf(unit.bidPrice());
            BigDecimal currentSize = BigDecimal.valueOf(unit.bidSize());

            BigDecimal tradeQuantity = remainingQuantity.min(currentSize);

            // 이것 역시 소수점 때문에 주문 가능 수량 0되면 끝내기
            // 매도 주문은 자료형 변환 심하지 않아서 필요 없을 거 같기도 함. 혹시 모르니 추가
            if (tradeQuantity.compareTo(BigDecimal.ZERO) <= 0) break;

            //호가와 거래 수량 곱해서 거래된 금액 계산
            BigDecimal tradeAmount = tradeQuantity.multiply(currentPrice);

            //체결된만큼 remaining에서 빼기
            remainingQuantity = remainingQuantity.subtract(tradeQuantity);

            // 부분 체결된 양을 계속 저장해서 총 체결 수량을 계산한다
            totalQuantity = totalQuantity.add(tradeQuantity);

            //부분 체결된 가격을 계속 저장해서 총 구매액을 계산한다
            weightedTradePriceSum = weightedTradePriceSum.add(tradeAmount);

            // 체결 정보 기록. 부분 체결될시 체결 될때마다 기록
            order.setTotalQuantity(tradeQuantity);
            order.setTotalPrice(tradeAmount.longValue());

            processTrade(order);
        }
        if (remainingQuantity.compareTo(BigDecimal.ZERO) > 0) {
            log.warn("거래 물량 부족으로 부분 체결됨. 지금까지 체결된 수량: {} 지금까지 체결된 금액: {}",totalQuantity, weightedTradePriceSum);
        }

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
                        processBuyOrder(order, sortedMarketAskUnits);
                    } else {
                        // 매수 호가 기준 내림차순
                        List<OrderbookUnitDto> sortedMarketBidUnits = units.stream()
                                .sorted(Comparator.comparingDouble(OrderbookUnitDto::bidPrice).reversed())
                                .toList();
                        processSellOrder(order, sortedMarketBidUnits);

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
                        processBuyOrder(order, sortedLimitAskUnits);


                    } else {
                        // 매수 호가 중 사용자가 입력한 것보다 비싼 것만 내림차순
                        List<OrderbookUnitDto> sortedLimitBidUnits = units.stream()
                                .filter(unit -> BigDecimal.valueOf(unit.bidPrice()).compareTo(BigDecimal.valueOf(order.getOrderPrice())) >= 0) // 지정가 이상만
                                .sorted(Comparator.comparingDouble(OrderbookUnitDto::bidPrice).reversed()) // 높은 가격부터 정렬
                                .toList();
                        processSellOrder(order, sortedLimitBidUnits);

                    }
                    log.info("[Market Order Executed] ID: {}, Market: {}", order.getId(), marketCode);
                    break;

            }
        }


        private void processTrade (Order order){
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
