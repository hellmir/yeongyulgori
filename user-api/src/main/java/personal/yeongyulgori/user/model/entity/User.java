package personal.yeongyulgori.user.model.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import personal.yeongyulgori.user.base.BaseEntity;
import personal.yeongyulgori.user.constant.Role;
import personal.yeongyulgori.user.model.Address;
import personal.yeongyulgori.user.model.dto.CrucialInformationUpdateDto;
import personal.yeongyulgori.user.model.form.InformationUpdateForm;
import personal.yeongyulgori.user.model.form.SignUpForm;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;

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

    @Column(columnDefinition = "MEDIUMBLOB")
    private byte[] profileImage;

    @Column(nullable = false)
    @Enumerated(STRING)
    private Role role;

    private boolean isVerified;
    private String verificationCode;
    private LocalDateTime verificationExpiredAt;

    @Builder
    private User(Long id, String email, String username, String password, String name,
                 LocalDate birthDate, String phoneNumber, Address address, byte[] profileImage,
                 Role role, LocalDateTime createdAt) {

        this.id = id;
        this.email = email.toLowerCase(Locale.ROOT);
        this.username = username;
        this.password = password;
        this.name = name;
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.role = role;
        this.profileImage = profileImage;

        setCreatedAt(createdAt);

    }

    public static User from(SignUpForm signUpForm) {

        byte[] decodedImage = Optional.ofNullable(signUpForm.getProfileImage())
                .map(Base64.getDecoder()::decode)
                .orElse(null);

        return User.builder()
                .email(signUpForm.getEmail())
                .username(signUpForm.getUsername())
                .password(signUpForm.getPassword())
                .name(signUpForm.getName())
                .birthDate(signUpForm.getBirthDate())
                .phoneNumber(signUpForm.getPhoneNumber())
                .address(signUpForm.getAddress())
                .role(signUpForm.getRole())
                .profileImage(decodedImage)
                .build();

    }

    public User withForm(String username, InformationUpdateForm informationUpdateForm) {

        byte[] decodedImage = Optional.ofNullable(informationUpdateForm.getProfileImage())
                .map(Base64.getDecoder()::decode)
                .orElse(null);

        return User.builder()
                .id(id)
                .email(email)
                .username(Optional.ofNullable(username).orElse(this.username))
                .password(password)
                .name(Optional.ofNullable(informationUpdateForm.getName()).orElse(name))
                .birthDate(birthDate)
                .phoneNumber(phoneNumber)
                .address(Optional.ofNullable(informationUpdateForm.getAddress()).isPresent()
                        ? Address.builder()
                        .city(Optional.ofNullable(informationUpdateForm.getAddress().getCity())
                                .orElse(address.getCity()))
                        .street(Optional.ofNullable(informationUpdateForm.getAddress().getStreet())
                                .orElse(address.getStreet()))
                        .zipcode(Optional.ofNullable(informationUpdateForm.getAddress().getZipcode())
                                .orElse(address.getZipcode()))
                        .detailedAddress(Optional.ofNullable(informationUpdateForm
                                        .getAddress().getDetailedAddress())
                                .orElse(address.getDetailedAddress()))
                        .build()
                        : address)
                .role(Optional.ofNullable(informationUpdateForm.getRole()).orElse(role))
                .profileImage(Optional.ofNullable(decodedImage).orElse(profileImage))
                .createdAt(getCreatedAt())
                .build();

    }

    public User withCrucialData(CrucialInformationUpdateDto crucialInformationUpdateDto) {

        return User.builder()
                .id(id)
                .email(Optional.ofNullable(crucialInformationUpdateDto.getEmail()).orElse(email))
                .username(username)
                .password(Optional.ofNullable(crucialInformationUpdateDto.getPassword()).orElse(password))
                .name(name)
                .birthDate(birthDate)
                .phoneNumber(Optional.ofNullable(crucialInformationUpdateDto.getPhoneNumber()).orElse(phoneNumber))
                .address(address)
                .role(role)
                .profileImage(profileImage)
                .createdAt(getCreatedAt())
                .build();

    }

}
