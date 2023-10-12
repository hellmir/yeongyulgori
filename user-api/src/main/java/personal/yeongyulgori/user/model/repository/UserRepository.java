package personal.yeongyulgori.user.model.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import personal.yeongyulgori.user.model.constant.Role;
import personal.yeongyulgori.user.model.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    List<User> findAllByRoles(Role role);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByFullName(String fullName);

    Page<User> findByFullNameContaining(String keyword, Pageable pageable);

}
