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
            "person1, 홍길동, abcd@abc.com, 1234, 1990-01-01, 01012345678, GENERAL_USER",
            "person2, 고길동, abcd@abcd.com, 12345, 2000-02-02, 01012345679, BUSINESS_USER",
            "person3, 김길동, abcd@abcde.com, 123456, 2010-03-03, 01012345680, GENERAL_USER"
    })
    void findByEmail(String userName, String name, String email, String password,
                     LocalDate birthDate, String phoneNumber, Role role) {

        // given
        User user = createUser(userName, name, email, password, birthDate, phoneNumber, role);

        userRepository.save(user);

        // when
        User foundUser = userRepository.findByEmail(email)
                .orElseThrow();

        // then
        assertThat(foundUser.getUserName()).isEqualTo(userName);
        assertThat(foundUser.getName()).isEqualTo(name);
        assertThat(foundUser.getEmail()).isEqualTo(email);
        assertThat(foundUser.getPassword()).isEqualTo(password);
        assertThat(foundUser.getRole()).isEqualTo(role);

    }

    @DisplayName("존재하지 않는 이메일 주소로 회원을 찾으면 회원을 반환하지 않는다.")
    @Test
    void findByEmailWithNonExistentEmail() {

        // given
        User user = createUser("person1", "홍길동", "abcd@abc.com", "1234",
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
        User user1 = createUser("person1", "홍길동", "abcd@abc.com", "1234",
                LocalDate.of(1990, 01, 01), "01012345678", GENERAL_USER);

        User user2 = createUser("person2", "고길동", "abcd@abcd.com", "12345",
                LocalDate.of(2000, 02, 10), "01012345679", BUSINESS_USER);

        User user3 = createUser("person3", "김길동", "abcd@abcde.com", "123456",
                LocalDate.of(2010, 03, 20), "01012345670", GENERAL_USER);

        User user4 = createUser("person4", "길동", "abcd@abcdef.com", "1234567",
                LocalDate.of(2020, 04, 30), "01012345671", BUSINESS_USER);

        userRepository.saveAll(List.of(user1, user2, user3, user4));

        // when
        List<User> selectedGeneralUsers = userRepository.findAllByRole(GENERAL_USER);
        List<User> selectedBusinessUsers = userRepository.findAllByRole(BUSINESS_USER);

        // then
        assertThat(selectedGeneralUsers).hasSize(2)
                .extracting("userName", "name", "email", "role")
                .containsExactlyInAnyOrder(
                        tuple("person1", "홍길동", "abcd@abc.com", GENERAL_USER),
                        tuple("person3", "김길동", "abcd@abcde.com", GENERAL_USER)
                );

        assertThat(selectedBusinessUsers).hasSize(2)
                .extracting("userName", "name", "email", "role")
                .containsExactlyInAnyOrder(
                        tuple("person2", "고길동", "abcd@abcd.com", BUSINESS_USER),
                        tuple("person4", "길동", "abcd@abcdef.com", BUSINESS_USER)
                );

    }

}
