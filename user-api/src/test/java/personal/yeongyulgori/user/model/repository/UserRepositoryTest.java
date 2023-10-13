package personal.yeongyulgori.user.model.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import personal.yeongyulgori.user.model.constant.Role;
import personal.yeongyulgori.user.model.entity.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static personal.yeongyulgori.user.model.constant.Role.*;
import static personal.yeongyulgori.user.testutil.TestConstant.*;
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
            "abcd@abc.com, person1, 1234, 홍길동, 1990-01-01, 01012345678, ROLE_GENERAL_USER",
            "abcd@abcd.com, person2, 12345, 고길동, 2000-02-02, 01012345679, ROLE_BUSINESS_USER",
            "abcd@abcde.com, person3, 123456, 김길동, 2010-03-03, 01012345680, ROLE_GENERAL_USER"
    })
    void findByEmail(String username, String fullName, String email, String password,
                     LocalDate birthDate, String phoneNumber, Role role) {

        // given
        User user = createUser(email, username, password, fullName, birthDate, phoneNumber, List.of(role));

        userRepository.save(user);

        // when
        User foundUser = userRepository.findByEmail(email)
                .orElseThrow();

        // then
        assertThat(foundUser.getEmail()).isEqualTo(email);
        assertThat(foundUser.getUsername()).isEqualTo(username);
        assertThat(foundUser.getPassword()).isEqualTo(password);
        assertThat(foundUser.getFullName()).isEqualTo(fullName);
        assertThat(foundUser.getRoles()).isEqualTo(user.getRoles());

    }

    @DisplayName("존재하지 않는 이메일 주소로 회원을 찾으면 회원을 반환하지 않는다.")
    @Test
    void findByEmailWithNonExistentEmail() {

        // given
        User user = createUser(EMAIL1, USERNAME1, PASSWORD1, FULL_NAME1,
                BIRTH_DATE1, PHONE_NUMBER1, List.of(ROLE_GENERAL_USER));

        userRepository.save(user);

        // when
        Optional<User> foundUser = userRepository.findByEmail(EMAIL2);

        // then
        assertThat(foundUser).isEmpty();

    }

    @DisplayName("사용자 이름을 통해 해당하는 회원을 찾을 수 있다.")
    @ParameterizedTest
    @CsvSource({
            "abcd@abc.com, person1, 1234, 홍길동, 1990-01-01, 01012345678, ROLE_GENERAL_USER",
            "abcd@abcd.com, person2, 12345, 고길동, 2000-02-02, 01012345679, ROLE_BUSINESS_USER",
            "abcd@abcde.com, person3, 123456, 김길동, 2010-03-03, 01012345680, ROLE_GENERAL_USER"
    })
    void findByUsername(String username, String fullName, String email, String password,
                        LocalDate birthDate, String phoneNumber, Role role) {

        // given
        User user = createUser(email, username, password, fullName, birthDate, phoneNumber, List.of(role));

        userRepository.save(user);

        // when
        User foundUser = userRepository.findByUsername(username).get();

        // then
        assertThat(foundUser.getUsername()).isEqualTo(username);
        assertThat(foundUser.getEmail()).isEqualTo(email);
        assertThat(foundUser.getPassword()).isEqualTo(password);
        assertThat(foundUser.getFullName()).isEqualTo(fullName);
        assertThat(foundUser.getRoles()).isEqualTo(user.getRoles());

    }

    @DisplayName("입력한 권한에 해당하는 모든 회원 목록을 조회할 수 있다.")
    @Test
    void findAllByRoles() {

        // given
        User user1 = createUser(EMAIL1, USERNAME1, PASSWORD1, FULL_NAME1,
                LocalDate.of(1990, 01, 01),
                PHONE_NUMBER1, List.of(ROLE_GENERAL_USER));

        User user2 = createUser(EMAIL2, USERNAME2, PASSWORD2, FULL_NAME2,
                LocalDate.of(2000, 02, 10),
                PHONE_NUMBER2, List.of(ROLE_BUSINESS_USER));

        User user3 = createUser(EMAIL3, USERNAME3, PASSWORD3, FULL_NAME3,
                LocalDate.of(2010, 03, 20),
                PHONE_NUMBER3, List.of(ROLE_GENERAL_USER, ROLE_ADMIN));

        User user4 = createUser(EMAIL4, USERNAME4, PASSWORD4, FULL_NAME4,
                LocalDate.of(2020, 04, 30),
                PHONE_NUMBER4, List.of(ROLE_BUSINESS_USER, ROLE_GENERAL_USER, ROLE_ADMIN));

        userRepository.saveAll(List.of(user1, user2, user3, user4));

        // when
        List<User> selectedGeneralUsers = userRepository.findAllByRoles(ROLE_GENERAL_USER);
        List<User> selectedBusinessUsers = userRepository.findAllByRoles(ROLE_BUSINESS_USER);
        List<User> selectedAdmins = userRepository.findAllByRoles(ROLE_ADMIN);

        // then
        assertThat(selectedGeneralUsers).hasSize(3)
                .extracting("email", "username", "fullName", "roles")
                .containsExactlyInAnyOrder(
                        tuple(EMAIL1, USERNAME1, FULL_NAME1, user1.getRoles()),
                        tuple(EMAIL3, USERNAME3, FULL_NAME3, user3.getRoles()),
                        tuple(EMAIL4, USERNAME4, FULL_NAME4, user4.getRoles())
                );

        assertThat(selectedBusinessUsers).hasSize(2)
                .extracting("email", "username", "fullName", "roles")
                .containsExactlyInAnyOrder(
                        tuple(EMAIL2, USERNAME2, FULL_NAME2, user2.getRoles()),
                        tuple(EMAIL4, USERNAME4, FULL_NAME4, user4.getRoles())
                );

        assertThat(selectedAdmins).hasSize(2)
                .extracting("email", "username", "fullName", "roles")
                .containsExactlyInAnyOrder(
                        tuple(EMAIL3, USERNAME3, FULL_NAME3, user3.getRoles()),
                        tuple(EMAIL4, USERNAME4, FULL_NAME4, user4.getRoles())
                );

    }

    @DisplayName("이메일을 통해 해당 회원의 가입 여부를 확인할 수 있다.")
    @ParameterizedTest
    @CsvSource({
            "abcd@abc.com, person1, 1234, 홍길동, 1990-01-01, 01012345678, ROLE_GENERAL_USER",
            "abcd@abcd.com, person2, 12345, 고길동, 2000-02-02, 01012345679, ROLE_BUSINESS_USER",
            "abcd@abcde.com, person3, 123456, 김길동, 2010-03-03, 01012345680, ROLE_GENERAL_USER"
    })
    void existsByEmail(String username, String fullName, String email, String password,
                       LocalDate birthDate, String phoneNumber, Role role) {

        // given
        User user = createUser(email, username, password, fullName, birthDate, phoneNumber, List.of(role));

        userRepository.save(user);

        // when
        boolean isEmailExists1 = userRepository.existsByEmail(email);
        boolean isEmailExists2 = userRepository.existsByEmail(EMAIL4);

        // then
        assertThat(isEmailExists1).isTrue();
        assertThat(isEmailExists2).isFalse();

    }

    @DisplayName("사용자 이름을 통해 해당 회원의 가입 여부를 확인할 수 있다.")
    @ParameterizedTest
    @CsvSource({
            "abcd@abc.com, person1, 1234, 홍길동, 1990-01-01, 01012345678, ROLE_GENERAL_USER",
            "abcd@abcd.com, person2, 12345, 고길동, 2000-02-02, 01012345679, ROLE_BUSINESS_USER",
            "abcd@abcde.com, person3, 123456, 김길동, 2010-03-03, 01012345680, ROLE_GENERAL_USER"
    })
    void existsByUsername(String username, String fullName, String email, String password,
                          LocalDate birthDate, String phoneNumber, Role role) {

        // given
        User user = createUser(email, username, password, fullName, birthDate, phoneNumber, List.of(role));

        userRepository.save(user);

        // when
        boolean isUsernameExists1 = userRepository.existsByUsername(username);
        boolean isUsernameExists2 = userRepository.existsByUsername(USERNAME4);

        // then
        assertThat(isUsernameExists1).isTrue();
        assertThat(isUsernameExists2).isFalse();

    }

    @DisplayName("휴대폰 번호를 통해 해당 회원의 가입 여부를 확인할 수 있다.")
    @ParameterizedTest
    @CsvSource({
            "abcd@abc.com, person1, 1234, 홍길동, 1990-01-01, 01012345678, ROLE_GENERAL_USER",
            "abcd@abcd.com, person2, 12345, 고길동, 2000-02-02, 01012345679, ROLE_BUSINESS_USER",
            "abcd@abcde.com, person3, 123456, 김길동, 2010-03-03, 01012345680, ROLE_GENERAL_USER"
    })
    void existsByPhoneNumber(String username, String fullName, String email, String password,
                             LocalDate birthDate, String phoneNumber, Role role) {

        // given
        User user = createUser(email, username, password, fullName, birthDate, phoneNumber, List.of(role));

        userRepository.save(user);

        // when
        boolean isPhoneNumberExists1 = userRepository.existsByPhoneNumber(phoneNumber);
        boolean isPhoneNumberExists2 = userRepository.existsByPhoneNumber(PHONE_NUMBER4);

        // then
        assertThat(isPhoneNumberExists1).isTrue();
        assertThat(isPhoneNumberExists2).isFalse();

    }

    @DisplayName("성명을 통해 해당 회원의 가입 여부를 확인할 수 있다.")
    @ParameterizedTest
    @CsvSource({
            "abcd@abc.com, person1, 1234, 홍길동, 1990-01-01, 01012345678, ROLE_GENERAL_USER",
            "abcd@abcd.com, person2, 12345, 고길동, 2000-02-02, 01012345679, ROLE_BUSINESS_USER",
            "abcd@abcde.com, person3, 123456, 김길동, 2010-03-03, 01012345680, ROLE_GENERAL_USER"
    })
    void existsByFullName(String username, String fullName, String email, String password,
                          LocalDate birthDate, String phoneNumber, Role role) {

        // given
        User user = createUser(email, username, password, fullName, birthDate, phoneNumber, List.of(role));

        userRepository.save(user);

        // when
        boolean isFullNameExists1 = userRepository.existsByFullName(fullName);
        boolean isFullNameExists2 = userRepository.existsByFullName(FULL_NAME4);

        // then
        assertThat(isFullNameExists1).isTrue();
        assertThat(isFullNameExists2).isFalse();

    }

    @DisplayName("일부 일치하는 키워드를 통해 해당 키워드를 포함하는 이름을 가진 회원을 찾을 수 있다.")
    @Test
    void findByNameContaining() {

        // given
        User user1 = createUser(EMAIL1, USERNAME1, PASSWORD1, FULL_NAME1,
                LocalDate.of(1990, 01, 01), PHONE_NUMBER1, List.of(ROLE_GENERAL_USER));

        User user2 = createUser(EMAIL2, USERNAME2, PASSWORD2, FULL_NAME2,
                LocalDate.of(2000, 02, 10), PHONE_NUMBER2, List.of(ROLE_BUSINESS_USER));

        User user3 = createUser(EMAIL3, USERNAME3, PASSWORD3, FULL_NAME3,
                LocalDate.of(2010, 03, 20), PHONE_NUMBER3, List.of(ROLE_GENERAL_USER));

        User user4 = createUser(EMAIL4, USERNAME4, PASSWORD4, FULL_NAME5,
                LocalDate.of(2020, 04, 30), PHONE_NUMBER4, List.of(ROLE_BUSINESS_USER));

        userRepository.saveAll(List.of(user1, user2, user3, user4));

        // when
        Page<User> foundUsers = userRepository.findByFullNameContaining(FIRST_NAME, Pageable.unpaged());

        // then
        assertThat(foundUsers).hasSize(3)
                .extracting("email", "username", "fullName", "roles")
                .containsExactlyInAnyOrder(
                        tuple(EMAIL1, USERNAME1, FULL_NAME1, user1.getRoles()),
                        tuple(EMAIL2, USERNAME2, FULL_NAME2, user2.getRoles()),
                        tuple(EMAIL3, USERNAME3, FULL_NAME3, user3.getRoles())
                );

    }

    @DisplayName("일부 일치하는 키워드를 통해 해당 키워드를 포함하는 이름을 가진 회원을 페이징 처리해 찾을 수 있다.")
    @Test
    void findByFullNameContainingWithPaging() {

        // given
        User user1 = createUser(EMAIL1, USERNAME1, PASSWORD1, FULL_NAME1,
                LocalDate.of(1990, 01, 01), PHONE_NUMBER1, List.of(ROLE_GENERAL_USER));

        User user2 = createUser(EMAIL2, USERNAME2, PASSWORD2, FULL_NAME2,
                LocalDate.of(2000, 02, 10), PHONE_NUMBER2, List.of(ROLE_BUSINESS_USER));

        User user3 = createUser(EMAIL3, USERNAME3, PASSWORD3, FULL_NAME5,
                LocalDate.of(2010, 03, 20), PHONE_NUMBER3, List.of(ROLE_BUSINESS_USER));

        User user4 = createUser(EMAIL4, USERNAME4, PASSWORD4, FULL_NAME6,
                LocalDate.of(2020, 04, 30), PHONE_NUMBER4, List.of(ROLE_GENERAL_USER));

        userRepository.saveAll(List.of(user1, user2, user3, user4));

        Pageable pageable = PageRequest.of(0, 2);

        // when
        Page<User> foundUsers = userRepository.findByFullNameContaining(FRONT_PART_OF_NAME, pageable);

        // then
        assertThat(foundUsers).hasSize(2)
                .extracting("email", "username", "fullName", "roles")
                .containsExactlyInAnyOrder(
                        tuple(EMAIL1, USERNAME1, FULL_NAME1, user1.getRoles()),
                        tuple(EMAIL3, USERNAME3, FULL_NAME5, user3.getRoles())
                );

    }

    @DisplayName("키워드를 포함하지 않으면 모든 회원을 페이징 처리해 찾을 수 있다.")
    @Test
    void findAllWithPaging() {

        // given
        User user1 = createUser(EMAIL1, USERNAME1, PASSWORD1, FULL_NAME1,
                LocalDate.of(1990, 01, 01), PHONE_NUMBER1, List.of(ROLE_GENERAL_USER));

        User user2 = createUser(EMAIL2, USERNAME2, PASSWORD2, FULL_NAME2,
                LocalDate.of(2000, 02, 10), PHONE_NUMBER2, List.of(ROLE_BUSINESS_USER));

        User user3 = createUser(EMAIL3, USERNAME3, PASSWORD3, FULL_NAME5,
                LocalDate.of(2010, 03, 20), PHONE_NUMBER3, List.of(ROLE_BUSINESS_USER));

        User user4 = createUser(EMAIL4, USERNAME4, PASSWORD4, FULL_NAME6,
                LocalDate.of(2020, 04, 30), PHONE_NUMBER4, List.of(ROLE_GENERAL_USER));

        userRepository.saveAll(List.of(user1, user2, user3, user4));

        Pageable pageable = PageRequest.of(0, 2);

        // when
        Page<User> foundUsers = userRepository.findByFullNameContaining("", pageable);

        // then
        assertThat(foundUsers).hasSize(2)
                .extracting("email", "username", "fullName", "roles")
                .containsExactlyInAnyOrder(
                        tuple(EMAIL1, USERNAME1, FULL_NAME1, user1.getRoles()),
                        tuple(EMAIL2, USERNAME2, FULL_NAME2, user2.getRoles())
                );

    }

}
