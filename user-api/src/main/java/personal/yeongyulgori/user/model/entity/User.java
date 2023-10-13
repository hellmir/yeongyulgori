package personal.yeongyulgori.user.model.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import personal.yeongyulgori.user.base.BaseEntity;
import personal.yeongyulgori.user.model.constant.Role;
import personal.yeongyulgori.user.model.dto.CrucialInformationUpdateDto;
import personal.yeongyulgori.user.model.dto.PasswordRequestDto;
import personal.yeongyulgori.user.model.entity.embedment.Address;
import personal.yeongyulgori.user.model.form.InformationUpdateForm;
import personal.yeongyulgori.user.model.form.SignUpForm;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Entity(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends BaseEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String email;

    @Column(nullable = false, unique = true, length = 20)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 10)
    private String fullName;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Column(unique = true, length = 20)
    private String phoneNumber;

    @Embedded
    private Address address;

    @Column(columnDefinition = "MEDIUMBLOB")
    private byte[] profileImage;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<Role> roles;

    @Builder
    private User(Long id, String email, String username, String password, String fullName,
                 LocalDate birthDate, String phoneNumber, Address address, byte[] profileImage,
                 List<Role> roles, LocalDateTime createdAt) {

        this.id = id;
        this.email = email.toLowerCase(Locale.ROOT);
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
        this.address = address;
        this.profileImage = profileImage;
        this.roles = roles;

        setCreatedAt(createdAt);

    }

    public static User from(SignUpForm signUpForm, String encodedPassword) {

        byte[] decodedImage = Optional.ofNullable(signUpForm.getProfileImage())
                .map(Base64.getDecoder()::decode)
                .orElse(null);

        return User.builder()
                .email(signUpForm.getEmail())
                .username(signUpForm.getUsername())
                .password(encodedPassword)
                .fullName(signUpForm.getFullName())
                .birthDate(signUpForm.getBirthDate())
                .phoneNumber(signUpForm.getPhoneNumber())
                .address(signUpForm.getAddress())
                .roles(signUpForm.getRoles())
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
                .fullName(Optional.ofNullable(informationUpdateForm.getFullName()).orElse(fullName))
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
                .roles(Optional.ofNullable(informationUpdateForm.getRoles()).orElse(roles))
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
                .fullName(fullName)
                .birthDate(birthDate)
                .phoneNumber(Optional.ofNullable(crucialInformationUpdateDto.getPhoneNumber()).orElse(phoneNumber))
                .address(address)
                .roles(roles)
                .profileImage(profileImage)
                .createdAt(getCreatedAt())
                .build();

    }

    public User withPassword(PasswordRequestDto passwordRequestDto) {

        return User.builder()
                .id(id)
                .email(email)
                .username(username)
                .password(passwordRequestDto.getPassword())
                .fullName(fullName)
                .birthDate(birthDate)
                .phoneNumber(phoneNumber)
                .address(address)
                .roles(roles)
                .profileImage(profileImage)
                .createdAt(getCreatedAt())
                .build();

    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream().map(role -> new SimpleGrantedAuthority(role.name())).collect(Collectors.toList());
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
