package exchange.exchang_BTC.src.user.entity;


import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAssetLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer ualId;

    @ManyToOne
    @JoinColumn(name = "u_id", nullable = false)
    private UserEntity user;

    @Column(precision = 20, scale = 8, nullable = false)
    private BigDecimal asset;

    private LocalDateTime createTime;
}