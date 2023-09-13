package personal.yeongyulgori.user.testutil;

import org.springframework.mock.web.MockMultipartFile;
import personal.yeongyulgori.user.constant.Role;
import personal.yeongyulgori.user.domain.Address;
import personal.yeongyulgori.user.domain.SignUpForm;
import personal.yeongyulgori.user.domain.model.User;

import java.time.LocalDate;

import static org.mockito.Mockito.mock;

public class TestObjectFactory {

    public static SignUpForm enterUserForm(String email, String username, String password, String name,
                                           LocalDate birthDate, String phoneNumber, Role role) {

        return SignUpForm.builder()
                .email(email)
                .username(username)
                .password(password)
                .name(name)
                .birthDate(birthDate)
                .phoneNumber(phoneNumber)
                .address(mock(Address.class))
                .role(role)
                .profileImage(
                        new MockMultipartFile
                                ("image", "image.png", "image/png", new byte[0])
                )
                .build();

    }

    public static SignUpForm enterUserFormWithAddressAndWithoutProfileImage(
            String email, String username, String password, String name,
            LocalDate birthDate, String phoneNumber, Role role
    ) {

        return SignUpForm.builder()
                .email(email)
                .username(username)
                .password(password)
                .name(name)
                .birthDate(birthDate)
                .phoneNumber(phoneNumber)
                .address(new Address(
                        "서울", "테헤란로 2길 5", "123-456-7890", "101동 102호"
                ))
                .role(role)
                .build();

    }

    public static User createUser(String email, String username, String password, String name,
                                  LocalDate birthDate, String phoneNumber, Role role) {

        return User.builder()
                .email(email)
                .username(username)
                .password(password)
                .name(name)
                .birthDate(birthDate)
                .phoneNumber(phoneNumber)
                .address(mock(Address.class))
                .profileImage(new byte[]{})
                .role(role)
                .build();

    }

}
