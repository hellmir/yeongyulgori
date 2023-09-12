package personal.yeongyulgori.user.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import personal.yeongyulgori.user.constant.Role;
import personal.yeongyulgori.user.domain.SignUpForm;
import personal.yeongyulgori.user.dto.UserResponseDto;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static personal.yeongyulgori.user.testutil.TestObjectFactory.enterUserForm;

@ActiveProfiles("test")
@SpringBootTest
class SignUpUserServiceTest {

    @Autowired
    private SignUpUserService signUpUserService;

    @DisplayName("사용자가 올바른 양식을 입력하면 회원가입을 할 수 있다.")
    @ParameterizedTest
    @CsvSource({
            "abcd@abc.com, person1, 1234, 홍길동, 2000-01-01, 01012345678, GENERAL_USER",
            "abcd@abcd.com, person2, 12345, 고길동, 2000-02-02, 01012345679, BUSINESS_USER",
            "abcd@abcde.com, person3, 123456, 김길동, 2000-03-03, 01012345680, GENERAL_USER"
    })
    void signUpUser(String email, String username, String password, String name,
                    LocalDate birthDate, String phoneNumber, Role role) {

        // given
        SignUpForm signUpForm = enterUserForm
                (email, username, password, name, birthDate, phoneNumber, role);

        // when
        UserResponseDto userResponseDto = signUpUserService.signUpUser(signUpForm);

        // then
        assertThat(userResponseDto.getEmail()).isEqualTo(email);
        assertThat(userResponseDto.getUsername()).isEqualTo(username);
        assertThat(userResponseDto.getRole()).isEqualTo(role);

    }

}