package exchange.exchang_BTC.order.domain.entity;

import lombok.Getter;

@Getter
public enum OrderPosition {
    BUY("buy"),
    SELL("sell");

    private final String position;

    OrderPosition(String position){
        this.position = position;
    }
}