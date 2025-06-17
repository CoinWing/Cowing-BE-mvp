package exchange.exchang_BTC.coin.repository;

import exchange.exchang_BTC.coin.entity.CoinEntity;
import exchange.exchang_BTC.src.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CoinRepository extends JpaRepository<CoinEntity, Long> {

}
