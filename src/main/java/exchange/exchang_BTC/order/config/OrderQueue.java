package exchange.exchang_BTC.order.config;

import exchange.exchang_BTC.order.domain.entity.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 빠른 개발을 위해 도입하였습니다. 향후 Redis나 Kafka와 같은 메시지 큐로 대체할 예정입니다.
 * 주문은 Order 객체로 표현되며, BlockingQueue를 사용하여 스레드 안전하게 구현됩니다.
 */

@Component
public class OrderQueue {
    private final BlockingQueue<Order> queue = new LinkedBlockingQueue<>();

    public void addOrder(Order order) {
        try {
            queue.put(order);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // InterruptedException 발생 시 현재 스레드의 인터럽트 상태 복원
            throw new RuntimeException("주문을 큐에 삽입하는 것을 실패하였습니다.", e);
        }
    }

    public Order takeOrder() throws InterruptedException {
        return queue.take();
    }
}