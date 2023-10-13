package personal.yeongyulgori.user.model.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import personal.yeongyulgori.user.model.entity.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, String> {
}
