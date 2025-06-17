package exchange.exchang_BTC.src.user.repository;

import exchange.exchang_BTC.src.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

}
