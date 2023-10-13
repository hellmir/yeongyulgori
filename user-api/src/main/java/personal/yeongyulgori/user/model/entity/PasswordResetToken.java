package personal.yeongyulgori.user.model.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

import static personal.yeongyulgori.user.security.JwtTokenProvider.PASSWORD_RESET_TOKEN_EXPIRATION_TIME;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "password_reset_token")
public class PasswordResetToken {

    @Id
    private String token;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private LocalDateTime expirationDate;

    private PasswordResetToken(String token, User user, LocalDateTime expirationDate) {
        this.token = token;
        this.user = user;
        this.expirationDate = expirationDate;
    }

    public static PasswordResetToken of(String token, User user) {
        LocalDateTime expirationDate = LocalDateTime.now().plusMinutes(PASSWORD_RESET_TOKEN_EXPIRATION_TIME / 1_000);
        return new PasswordResetToken(token, user, expirationDate);
    }

    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
    }

}
