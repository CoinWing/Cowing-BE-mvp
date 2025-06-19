package exchange.exchang_BTC.order.service;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class RealTimeOrderBook {
    // Key: Market Code (e.g., "KRW-BTC"), Value: 현재가, 호가 등 정보 (지금은 단순화를 위해 Double로 현재가만 저장)
    private final ConcurrentHashMap<String, Double> orderBook = new ConcurrentHashMap<>();

    public Double getPrice(String marketCode) {
        // 실제 호가 정보 조회 로직 구현 필요
        // 현재는 임시로 1000.0을 반환하여 체결을 시뮬레이션
        return orderBook.getOrDefault(marketCode, 1000.0);
    }
}