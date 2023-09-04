package personal.yeongyulgori.user.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import personal.yeongyulgori.user.domain.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
