package exchange.exchang_BTC.src.trade.repository;

import exchange.exchang_BTC.src.trade.entity.TradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TradeRepository extends JpaRepository<TradeEntity, Integer> {
}