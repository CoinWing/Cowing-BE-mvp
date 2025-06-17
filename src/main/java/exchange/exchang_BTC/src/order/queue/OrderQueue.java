package exchange.exchang_BTC.src.order.queue;

import exchange.exchang_BTC.src.order.dto.LimitOrderDTO;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class OrderQueue {
    private final BlockingQueue<LimitOrderDTO> queue = new LinkedBlockingQueue<>();

    public void enqueue(LimitOrderDTO order) {
        queue.offer(order);
    }

    public LimitOrderDTO dequeue() throws InterruptedException {
        return queue.take();
    }
}
