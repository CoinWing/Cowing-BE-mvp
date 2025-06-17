package exchange.exchang_BTC.src.test;

import exchange.exchang_BTC.src.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/test")
public class TestController {

    private final UserRepository userRepository;

    @GetMapping("/ping")
    public String checkDBConnection() {
        long count = userRepository.count();  // user 테이블에 있는 row 개수
        return "DB 연결됨! 현재 유저 수: " + count;
    }
}

