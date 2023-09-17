package personal.yeongyulgori.user.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import personal.yeongyulgori.user.constant.Role;
import personal.yeongyulgori.user.model.Address;
import personal.yeongyulgori.user.model.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;

@Getter
@AllArgsConstructor
@Builder
public class UserResponseDto {

    private Long id;
    private String email;
    private String username;
    private String name;
    private LocalDate birthDate;
    private String phoneNumber;
    private Address address;
    private Role role;
    private String profileImage;

    private LocalDateTime createdAt;
    private LocalDateTime modifiedAt;

    public static UserResponseDto of(
            String email, String username, String name,
            Role role, LocalDateTime createdAt, LocalDateTime modifiedAt
    ) {

        return UserResponseDto.builder()
                .email(email)
                .username(username)
                .name(name)
                .role(role)
                .createdAt(createdAt)
                .modifiedAt(modifiedAt)
                .build();

    }

    public static UserResponseDto from(User user) {

        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .name(user.getName())
                .birthDate(user.getBirthDate())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .role(user.getRole())
                .profileImage(user.getProfileImage() != null ?
                        Base64.getEncoder().encodeToString(user.getProfileImage()) : null)
                .createdAt(user.getCreatedAt())
                .modifiedAt(user.getModifiedAt())
                .build();

    }

}
