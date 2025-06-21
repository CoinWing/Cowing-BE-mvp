package exchange.exchang_BTC;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ExchangBtcApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExchangBtcApplication.class, args);
	}

}
