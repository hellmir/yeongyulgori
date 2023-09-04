package personal.yeongyulgori.user.testutil;

import personal.yeongyulgori.user.domain.Address;
import personal.yeongyulgori.user.domain.SignUpForm;

import java.time.LocalDate;

import static org.mockito.Mockito.mock;

public class TestObjectFactory {

    public static SignUpForm enterUserForm(String name, String email, String password,
                                        LocalDate birthDate, String phoneNumber) {

        return SignUpForm.builder()
                .name(name)
                .email(email)
                .password(password)
                .birthDate(birthDate)
                .phoneNumber(phoneNumber)
                .address(mock(Address.class))
                .profileImage(new byte[]{})
                .build();

    }

}
