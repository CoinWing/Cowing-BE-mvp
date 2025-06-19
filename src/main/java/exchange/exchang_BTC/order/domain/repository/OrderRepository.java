package exchange.exchang_BTC.order.domain.repository;

import exchange.exchang_BTC.order.domain.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

}
