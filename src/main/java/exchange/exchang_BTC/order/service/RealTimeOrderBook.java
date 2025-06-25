package exchange.exchang_BTC.order.service;

import exchange.exchang_BTC.order.api.dto.upbit.OrderbookDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RealTimeOrderBook {
    private final ConcurrentHashMap<String, OrderbookDto> orderBook = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate = new RestTemplate();

    private final String markets = "KRW-BTC,KRW-ETH,KRW-XRP,KRW-SOL,KRW-ADA,KRW-DOGE,KRW-AVAX,KRW-LINK,KRW-TRX,KRW-DOT,KRW-ATOM,KRW-SUI,KRW-UNI,KRW-SEI,KRW-AAVE,KRW-ARB,KRW-STX,KRW-NEAR,KRW-APT,KRW-IMX,KRW-POL,KRW-INJ,KRW-TIA,KRW-SHIB,KRW-PEPE,KRW-SAND,KRW-MANA,KRW-AXS,KRW-BONK,KRW-USDT,KRW-USDC,KRW-BCH,KRW-ETC,KRW-XLM,KRW-HBAR,KRW-VET,KRW-FIL,KRW-THETA,KRW-CHZ,KRW-GRT,KRW-ZRX,KRW-KNC,KRW-BAT,KRW-WAVES,KRW-XTZ,KRW-NEO,KRW-QTUM,KRW-ICX,KRW-1INCH,KRW-STMX";

    public OrderbookDto getOrderBook(String marketCode) {
        return orderBook.get(marketCode);
    }

    @Scheduled(fixedRate = 1000)
    public void getAPIOrderBook() {
        try {
            // orderBook에 이전 정보가 있다면 비우고 새로운 데이터를 가져온다.
            if (!orderBook.isEmpty()){
                orderBook.clear();
            }

            String url = "https://api.upbit.com/v1/orderbook?markets=" + markets;
            OrderbookDto[] response = restTemplate.getForObject(url, OrderbookDto[].class);

            if (response != null && response.length > 0) {
                for (OrderbookDto dto : response) {
                    String market = dto.market();
                    orderBook.put(market, dto);
                }
            } else {
                log.warn("호가 응답 없음");
            }

        } catch (Exception e) {
            log.error("호가 조회 실패");

        }

    }
}