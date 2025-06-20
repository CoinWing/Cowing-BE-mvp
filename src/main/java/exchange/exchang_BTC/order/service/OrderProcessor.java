package exchange.exchang_BTC.order.service;

import exchange.exchang_BTC.order.config.OrderQueue;
import exchange.exchang_BTC.order.domain.entity.Order;
import exchange.exchang_BTC.trade.Trade;
import exchange.exchang_BTC.trade.TradeRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
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
                // 3-1. 시장가 매매: 한 번만 조회하고 즉시 체결 간주
                Double currentPrice = realTimeOrderBook.getPrice(marketCode);
                log.info("[Market Order Executed] ID: {}, Market: {}, Price: {}", order.getId(), marketCode, currentPrice);

                // DB에 주문 기록 저장 -> 체결로 간주
                processTrade(order);
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
                        .build()
        );
        log.info("Trade converted from order Is recorded: {}", order);
    }
}