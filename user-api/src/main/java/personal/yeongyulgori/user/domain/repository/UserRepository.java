package personal.yeongyulgori.user.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import personal.yeongyulgori.user.constant.Role;
import personal.yeongyulgori.user.domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    List<User> findAllByRole(Role role);

}
