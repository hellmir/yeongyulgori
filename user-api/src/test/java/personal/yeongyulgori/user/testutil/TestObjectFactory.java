package personal.yeongyulgori.user.testutil;

import personal.yeongyulgori.user.constant.Role;
import personal.yeongyulgori.user.model.Address;
import personal.yeongyulgori.user.model.entity.User;
import personal.yeongyulgori.user.model.form.SignUpForm;

import java.time.LocalDate;

import static org.mockito.Mockito.mock;

public class TestObjectFactory {

    private static String originalTestImage = "data:image/jpeg;base64,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";
    public static final String TEST_IMAGE = originalTestImage.replaceAll("[^A-Za-z0-9+/=]", "");

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
                .profileImage(TEST_IMAGE)
                .build();

    }

    public static SignUpForm enterUserFormWithAddress(
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
                .address(Address.builder()
                        .city("서울")
                        .street("테헤란로 2길 5")
                        .zipcode("12345")
                        .detailedAddress("101동 102호")
                        .build())
                .role(role)
                .profileImage(TEST_IMAGE)
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
                .profileImage(TEST_IMAGE.getBytes())
                .role(role)
                .build();

    }

    public static User createUserWithAddress(String email, String username, String password, String name,
                                             LocalDate birthDate, String phoneNumber, Role role) {

        return User.builder()
                .email(email)
                .username(username)
                .password(password)
                .name(name)
                .birthDate(birthDate)
                .phoneNumber(phoneNumber)
                .address(Address.builder()
                        .city("서울")
                        .street("테헤란로 2길 5")
                        .zipcode("12345")
                        .detailedAddress("101동 102호")
                        .build())
                .profileImage(TEST_IMAGE.getBytes())
                .role(role)
                .build();

    }

}
