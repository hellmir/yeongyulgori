package personal.yeongyulgori.user.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import personal.yeongyulgori.user.constant.Role;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
@Builder
public class SignUpForm {

    private String username;
    private String name;
    private String email;
    private String password;
    private LocalDate birthDate;
    private String phoneNumber;
    private Address address;
    private Role role;
    private byte[] profileImage;

}
