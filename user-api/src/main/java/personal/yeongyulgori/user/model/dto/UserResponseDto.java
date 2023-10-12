package personal.yeongyulgori.user.model.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import personal.yeongyulgori.user.model.constant.Role;
import personal.yeongyulgori.user.model.entity.User;
import personal.yeongyulgori.user.model.entity.embedment.Address;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
public class UserResponseDto {

    private Long id;
    private String email;
    private String username;
    private String fullName;
    private LocalDate birthDate;
    private String phoneNumber;
    private Address address;
    private List<Role> roles;
    private String profileImage;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime createdAt;

    @JsonSerialize(using = LocalDateTimeSerializer.class)
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime modifiedAt;

    public static UserResponseDto of(
            String email, String username, String fullName,
            List<Role> roles, LocalDateTime createdAt, LocalDateTime modifiedAt
    ) {

        return UserResponseDto.builder()
                .email(email)
                .username(username)
                .fullName(fullName)
                .roles(roles)
                .createdAt(createdAt)
                .modifiedAt(modifiedAt)
                .build();

    }

    public static UserResponseDto from(User user) {

        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .birthDate(user.getBirthDate())
                .phoneNumber(user.getPhoneNumber())
                .address(user.getAddress())
                .roles(user.getRoles())
                .profileImage(user.getProfileImage() != null ?
                        Base64.getEncoder().encodeToString(user.getProfileImage()) : null)
                .createdAt(user.getCreatedAt())
                .modifiedAt(user.getModifiedAt())
                .build();

    }

}
