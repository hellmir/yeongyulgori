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
            "abcd@abc.com, person1, 1234, 홍길동, 1990-01-01, 01012345678, GENERAL_USER",
            "abcd@abcd.com, person2, 12345, 고길동, 2000-02-02, 01012345679, BUSINESS_USER",
            "abcd@abcde.com, person3, 123456, 김길동, 2010-03-03, 01012345680, GENERAL_USER"
    })
    void findByEmail(String username, String name, String email, String password,
                     LocalDate birthDate, String phoneNumber, Role role) {

        // given
        User user = createUser(email, username, password, name, birthDate, phoneNumber, role);

        userRepository.save(user);

        // when
        User foundUser = userRepository.findByEmail(email)
                .orElseThrow();

        // then
        assertThat(foundUser.getEmail()).isEqualTo(email);
        assertThat(foundUser.getUsername()).isEqualTo(username);
        assertThat(foundUser.getPassword()).isEqualTo(password);
        assertThat(foundUser.getName()).isEqualTo(name);
        assertThat(foundUser.getRole()).isEqualTo(role);

    }

    @DisplayName("존재하지 않는 이메일 주소로 회원을 찾으면 회원을 반환하지 않는다.")
    @Test
    void findByEmailWithNonExistentEmail() {

        // given
        User user = createUser("abcd@abc.com", "person1", "1234", "홍길동",
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
        User user1 = createUser("abcd@abc.com", "person1", "1234", "홍길동",
                LocalDate.of(1990, 01, 01), "01012345678", GENERAL_USER);

        User user2 = createUser("abcd@abcd.com", "person2", "12345", "고길동",
                LocalDate.of(2000, 02, 10), "01012345679", BUSINESS_USER);

        User user3 = createUser("abcd@abcde.com", "person3", "123456", "김길동",
                LocalDate.of(2010, 03, 20), "01012345670", GENERAL_USER);

        User user4 = createUser("abcd@abcdef.com", "person4", "1234567", "길동",
                LocalDate.of(2020, 04, 30), "01012345671", BUSINESS_USER);

        userRepository.saveAll(List.of(user1, user2, user3, user4));

        // when
        List<User> selectedGeneralUsers = userRepository.findAllByRole(GENERAL_USER);
        List<User> selectedBusinessUsers = userRepository.findAllByRole(BUSINESS_USER);

        // then
        assertThat(selectedGeneralUsers).hasSize(2)
                .extracting("email", "username", "name", "role")
                .containsExactlyInAnyOrder(
                        tuple("abcd@abc.com", "person1", "홍길동", GENERAL_USER),
                        tuple("abcd@abcde.com", "person3", "김길동", GENERAL_USER)
                );

        assertThat(selectedBusinessUsers).hasSize(2)
                .extracting("email", "username", "name", "role")
                .containsExactlyInAnyOrder(
                        tuple("abcd@abcd.com", "person2", "고길동", BUSINESS_USER),
                        tuple("abcd@abcdef.com", "person4", "길동", BUSINESS_USER)
                );

    }

}
