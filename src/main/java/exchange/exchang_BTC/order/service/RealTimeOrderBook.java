package exchange.exchang_BTC.order.service;

import exchange.exchang_BTC.order.api.dto.market.MarketPriceDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RealTimeOrderBook {
    // Key: Market Code (e.g., "KRW-BTC"), Value: 현재가, 호가 등 정보 (지금은 단순화를 위해 Double로 현재가만 저장)
    private final ConcurrentHashMap<String, Double> orderBook = new ConcurrentHashMap<>();
    private final RestTemplate restTemplate = new RestTemplate();
    private final List<String> marketCodes = List.of(
            "KRW-XRP", "KRW-ETH", "KRW-BTC", "KRW-SNT", "KRW-ALT", "KRW-USDT", "KRW-SUI", "KRW-SOL",
            "KRW-DOGE", "KRW-POKT", "KRW-BORA", "KRW-RVN", "KRW-ORBS", "KRW-UNI", "KRW-ONDO", "KRW-VIRTUAL", "KRW-SOPH", "KRW-ADA", "KRW-NXPC", "KRW-ANIME",
            "KRW-PEPE", "KRW-TRUMP", "KRW-AGLD", "KRW-BCH", "KRW-ENS", "KRW-SEI", "KRW-SHIB", "KRW-STMX", "KRW-AAVE", "KRW-WCT", "KRW-STRAX", "KRW-LAYER",
            "KRW-TFUEL", "KRW-LINK", "KRW-MOVE", "KRW-TRX", "KRW-KAITO", "KRW-NEAR", "KRW-ARB", "KRW-STX", "KRW-HBAR", "KRW-XLM", "KRW-UXLINK", "KRW-ZRO", "KRW-AVAX", "KRW-SAND", "KRW-MASK", "KRW-T",
            "KRW-DOT", "KRW-POL", "KRW-AXL", "KRW-ME", "KRW-MEW", "KRW-ETC", "KRW-VANA", "KRW-LPT", "KRW-JTO", "KRW-ALGO", "KRW-BONK", "KRW-DRIFT", "KRW-SONIC", "KRW-PYTH",
            "KRW-BERA", "KRW-A", "KRW-TAIKO", "KRW-BSV", "KRW-BTT", "KRW-BLUR", "KRW-AERGO", "KRW-IMX", "KRW-PENDLE", "KRW-ICX", "KRW-PENGU", "KRW-OM", "KRW-GRT",
            "KRW-COMP", "KRW-ATH", "KRW-XEM", "KRW-NEO", "KRW-INJ", "KRW-JUP", "KRW-BIGTIME", "KRW-APT", "KRW-BEAM", "KRW-SIGN", "KRW-ZETA", "KRW-AKT", "KRW-CRO", "KRW-ARDR", "KRW-VET", "KRW-GAS",
            "KRW-W", "KRW-ATOM", "KRW-GMT", "KRW-AXS", "KRW-TT", "KRW-CTC", "KRW-FLOW", "KRW-AUCTION", "KRW-CARV", "KRW-MOCA", "KRW-MLK", "KRW-MANA", "KRW-PUNDIX",
            "KRW-FIL", "KRW-IOST", "KRW-WAVES", "KRW-DEEP", "KRW-MNT", "KRW-XEC", "KRW-RENDER", "KRW-CHZ", "KRW-SXP", "KRW-ORCA", "KRW-QTUM", "KRW-IOTA", "KRW-HIVE", "KRW-CVC",
            "KRW-TOKAMAK", "KRW-BLAST", "KRW-THETA", "KRW-HUNT", "KRW-MINA", "KRW-G", "KRW-POLYX", "KRW-SAFE", "KRW-TIA", "KRW-ARKM", "KRW-AWE", "KRW-WAXP", "KRW-STRIKE", "KRW-CELO", "KRW-ARK",
            "KRW-ID", "KRW-ZRX", "KRW-HP", "KRW-ZIL", "KRW-GLM", "KRW-GAME2", "KRW-CKB", "KRW-VTHO", "KRW-STG", "KRW-COW", "KRW-USDC", "KRW-EGLD", "KRW-MED", "KRW-KAVA", "KRW-XTZ",
            "KRW-ASTR", "KRW-AQT", "KRW-IQ", "KRW-ONT", "KRW-WAL", "KRW-MVL", "KRW-ELF", "KRW-LSK", "KRW-ONG", "KRW-DKA", "KRW-ANKR", "KRW-MTL", "KRW-JST", "KRW-MBL", "KRW-BOUNTY",
            "KRW-SC", "KRW-QKC", "KRW-1INCH", "KRW-STEEM", "KRW-STORJ", "KRW-MOC", "KRW-META", "KRW-BAT", "KRW-POWR", "KRW-AHT", "KRW-KNC", "KRW-CBK", "KRW-GRS", "KRW-FCT2"
            );
    public Double getPrice(String marketCode) {
        return orderBook.get(marketCode);
    }
    String markets = String.join(",", marketCodes);

    @Scheduled(fixedRate = 1000)
    public void getAPI() {
        try {
            String url = "https://api.upbit.com/v1/ticker?markets=" + markets;
            MarketPriceDto[] response = restTemplate.getForObject(url, MarketPriceDto[].class);

            if (response != null && response.length > 0) {
                for (MarketPriceDto dto : response) {
                    String market = dto.market();
                    Double price = dto.trade_price().doubleValue();
                    orderBook.put(market, price);
                }
            } else {
                log.warn("현재가 응답 없음");
            }
        } catch (Exception e) {
            log.error("현재가 조회 실패");
        }

    }
}

