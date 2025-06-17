package exchange.exchang_BTC.src.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer uId;

    private LocalDateTime createTime;

    @Column(precision = 20, scale = 8, nullable = false)
    private BigDecimal uHoldings;


}