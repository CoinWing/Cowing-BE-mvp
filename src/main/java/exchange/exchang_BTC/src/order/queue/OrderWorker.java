package exchange.exchang_BTC.src.order.queue;

import exchange.exchang_BTC.coin.entity.CoinEntity;
import exchange.exchang_BTC.coin.repository.CoinRepository;
import exchange.exchang_BTC.src.order.dto.LimitOrderDTO;
import exchange.exchang_BTC.src.order.entity.OrderEntity;
import exchange.exchang_BTC.src.order.repository.OrderRepository;
import exchange.exchang_BTC.src.user.entity.UserEntity;
import exchange.exchang_BTC.src.user.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderWorker {

    private final UserRepository userRepository;
    private final CoinRepository coinRepository;
    private final OrderRepository orderRepository;
    private final OrderQueue orderQueue; // 💡 생성자 주입

    @PostConstruct
    public void startWorker() {
        Thread worker = new Thread(() -> {
            while (true) {
                try {
                    LimitOrderDTO dto = orderQueue.dequeue(); // 여기서 NPE 발생했던 것
                    recordOrder(dto);// 저장 처리

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        worker.setDaemon(true);
        worker.start();
    }
    @Transactional
    public void recordOrder(LimitOrderDTO dto) {
        // 1. 사용자 조회
        UserEntity user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. 코인 조회
        CoinEntity coin = coinRepository.findById(dto.getCoinId())
                .orElseThrow(() -> new RuntimeException("Coin not found"));

        // 3. 주문 엔티티 생성
        OrderEntity order = new OrderEntity(
                user,
                coin,
                dto.getQuantity(),
                dto.getPrice(),
                dto.getOrderType()
        );

        // 4. 저장
        orderRepository.save(order);
    }
}