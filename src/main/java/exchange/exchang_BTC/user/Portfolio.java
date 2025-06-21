package exchange.exchang_BTC.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "username", nullable = false)
    private String username;

    @Column(name = "market_code", nullable = false)
    private String marketCode;

    @Column(name = "quantity", nullable = false)
    private BigDecimal quantity;

    @Column(name = "average_cost", nullable = false)
    private Long averageCost;
}
