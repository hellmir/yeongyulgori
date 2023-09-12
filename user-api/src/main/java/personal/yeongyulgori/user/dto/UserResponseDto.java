package personal.yeongyulgori.user.dto;

import lombok.Data;
import personal.yeongyulgori.user.constant.Role;
import personal.yeongyulgori.user.domain.Address;

import java.time.LocalDate;

@Data
public class UserResponseDto {

    private Long id;
    private String email;
    private String username;
    private String name;
    private LocalDate birthDate;
    private String phoneNumber;
    private Address address;
    private Role role;
    private byte[] profileImage;

}
