package personal.yeongyulgori.user.domain.model;

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
import static personal.yeongyulgori.user.constant.Role.GENERAL_USER;

@Entity(name = "users")
@Getter
@NoArgsConstructor
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 10)
    private String name;

    @Column(nullable = false, unique = true, length = 30)
    private String email;

    @Column(nullable = false, length = 20)
    private String password;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Column(length = 20, unique = true)
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
    private User(Long id, String name, String email, String password, LocalDate birthDate,
                 String phoneNumber, Address address, byte[] profileImage,
                 LocalDateTime createdAt, LocalDateTime modifiedAt) {

        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.role = GENERAL_USER;

        setCreatedAt(createdAt);

        setModifiedAt(modifiedAt);

    }

    public static User from(SignUpForm signUpForm) {

        return User.builder()
                .name(signUpForm.getName())
                .email(signUpForm.getEmail().toLowerCase(Locale.ROOT))
                .password(signUpForm.getPassword())
                .birthDate(signUpForm.getBirthDate())
                .phoneNumber(signUpForm.getPhoneNumber())
                .address(signUpForm.getAddress())
                .profileImage(signUpForm.getProfileImage())
                .build();

    }

}
