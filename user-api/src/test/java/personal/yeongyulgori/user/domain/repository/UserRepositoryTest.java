package personal.yeongyulgori.user.domain.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import personal.yeongyulgori.user.constant.Role;
import personal.yeongyulgori.user.domain.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static personal.yeongyulgori.user.constant.Role.BUSINESS_USER;
import static personal.yeongyulgori.user.constant.Role.GENERAL_USER;
import static personal.yeongyulgori.user.testutil.TestObjectFactory.createUser;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @DisplayName("이메일 주소를 통해 해당 이메일 주소로 가입한 회원을 찾을 수 있다.")
    @ParameterizedTest
    @CsvSource({
            "홍길동, abcd@abc.com, 1234, 1990-01-01, 01012345678, GENERAL_USER",
            "고길동, abcd@abcd.com, 12345, 2000-02-02, 01012345679, BUSINESS_USER",
            "김길동, abcd@abcde.com, 123456, 2010-03-03, 01012345680, GENERAL_USER"
    })
    void findByEmail(String name, String email, String password, LocalDate birthDate,
                     String phoneNumber, Role role) {

        // given
        User user = createUser(name, email, password, birthDate, phoneNumber, role);

        userRepository.save(user);

        // when
        User findedUser = userRepository.findByEmail(email)
                .orElseThrow();

        // then
        assertThat(findedUser.getName()).isEqualTo(name);
        assertThat(findedUser.getEmail()).isEqualTo(email);
        assertThat(findedUser.getPassword()).isEqualTo(password);
        assertThat(findedUser.getRole()).isEqualTo(role);

    }

    @DisplayName("존재하지 않는 이메일 주소로 회원을 찾으면 회원을 반환하지 않는다.")
    @Test
    void findByEmailWithNonExistentEmail() {

        // given

        User user = createUser("홍길동", "abcd@abc.com", "1234",
                LocalDate.of(1990, 01, 01), "01012345678", GENERAL_USER);

        userRepository.save(user);

        // when
        Optional<User> foundUser = userRepository.findByEmail("ab@abcd.com");

        // then
        assertThat(foundUser).isEmpty();

    }

    @DisplayName("Role을 통해 원하는 분류의 회원 목록을 조회할 수 있다.")
    @Test
    void findAllByRole() {

        // given

        User user1 = createUser("홍길동", "abcd@abc.com", "1234",
                LocalDate.of(1990, 01, 01), "01012345678", GENERAL_USER);

        User user2 = createUser("고길동", "abcd@abcd.com", "12345",
                LocalDate.of(2000, 02, 10), "01012345679", BUSINESS_USER);

        User user3 = createUser("김길동", "abcd@abcde.com", "123456",
                LocalDate.of(2010, 03, 20), "01012345670", GENERAL_USER);

        User user4 = createUser("길동", "abcd@abcdef.com", "1234567",
                LocalDate.of(2020, 04, 30), "01012345671", BUSINESS_USER);

        userRepository.saveAll(List.of(user1, user2, user3, user4));

        // when
        List<User> selectedUsers1 = userRepository.findAllByRole(GENERAL_USER);
        List<User> selectedUsers2 = userRepository.findAllByRole(BUSINESS_USER);

        // then
        assertThat(selectedUsers1).hasSize(2)
                .extracting("name", "email", "role")
                .containsExactlyInAnyOrder(
                        tuple("홍길동", "abcd@abc.com", GENERAL_USER),
                        tuple("김길동", "abcd@abcde.com", GENERAL_USER)
                );

        assertThat(selectedUsers2).hasSize(2)
                .extracting("name", "email", "role")
                .containsExactlyInAnyOrder(
                        tuple("고길동", "abcd@abcd.com", BUSINESS_USER),
                        tuple("길동", "abcd@abcdef.com", BUSINESS_USER)
                );

    }

}
