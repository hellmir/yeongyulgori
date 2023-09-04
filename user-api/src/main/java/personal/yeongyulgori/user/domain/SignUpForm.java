package personal.yeongyulgori.user.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class SignUpForm {

    private String name;
    private String email;
    private String password;
    private LocalDate birthDate;
    private String phoneNumber;
    private Address address;
    private byte[] profileImage;

}
