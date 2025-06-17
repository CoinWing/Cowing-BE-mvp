package exchange.exchang_BTC.coin.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Table(name = "Coin")
@NoArgsConstructor
@AllArgsConstructor
public class CoinEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer cId;

    private String coinName;

    private String coinTicker;


}
