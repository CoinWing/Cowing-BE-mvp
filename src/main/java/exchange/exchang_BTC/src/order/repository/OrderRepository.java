package exchange.exchang_BTC.src.order.repository;

import exchange.exchang_BTC.coin.entity.CoinEntity;
import exchange.exchang_BTC.src.order.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    // OrderRepository
    @Query("SELECT o FROM OrderEntity o WHERE o.coin.cId = :coinId AND o.orderType = 'SELL' AND o.price <= :buyPrice ORDER BY o.price ASC, o.createTime ASC")
    List<OrderEntity> findActiveSellOrdersByCoinAndPrice(@Param("coinId") Long coinId, @Param("buyPrice") BigDecimal buyPrice);

    @Query("SELECT o FROM OrderEntity o WHERE o.coin.cId = :coinId AND o.orderType = 'BUY' AND o.price <= :sellPrice ORDER BY o.price DESC , o.createTime ASC")
    List<OrderEntity> findActiveBuyOrdersByCoinAndPrice(@Param("coinId") Long coinId, @Param("sellPrice") BigDecimal sellPrice);

}
