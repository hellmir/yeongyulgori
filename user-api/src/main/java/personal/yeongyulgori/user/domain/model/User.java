package personal.yeongyulgori.user.domain.model;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import personal.yeongyulgori.user.base.BaseEntity;
import personal.yeongyulgori.user.constant.Role;
import personal.yeongyulgori.user.domain.Address;
import personal.yeongyulgori.user.domain.SignUpForm;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;

import static javax.persistence.EnumType.STRING;

@Entity(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String email;

    @Column(nullable = false, unique = true, length = 20)
    private String username;

    @Column(nullable = false, length = 30)
    private String password;

    @Column(nullable = false, length = 10)
    private String name;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Column(unique = true, length = 20)
    private String phoneNumber;

    @Embedded
    private Address address;

    @Lob
    private byte[] profileImage;

    @Column(nullable = false)
    @Enumerated(STRING)
    private Role role;

    private LocalDateTime verifyExpiredAt;
    private String verificationCode;
    private boolean verify;

    @Builder
    private User(String email, String username, String password, String name,
                 LocalDate birthDate, String phoneNumber, Address address, byte[] profileImage,
                 Role role, LocalDateTime createdAt, LocalDateTime modifiedAt) {

        this.email = email;
        this.username = username;
        this.password = password;
        this.name = name;
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.role = role;
        this.profileImage = profileImage;

        setCreatedAt(createdAt);

        setModifiedAt(modifiedAt);

    }

    public static User from(SignUpForm signUpForm) {

        return User.builder()
                .email(signUpForm.getEmail().toLowerCase(Locale.ROOT))
                .username(signUpForm.getUsername())
                .password(signUpForm.getPassword())
                .name(signUpForm.getName())
                .birthDate(signUpForm.getBirthDate())
                .phoneNumber(signUpForm.getPhoneNumber())
                .address(signUpForm.getAddress())
                .role(signUpForm.getRole())
                .profileImage(signUpForm.getProfileImage())
                .build();

    }

}
