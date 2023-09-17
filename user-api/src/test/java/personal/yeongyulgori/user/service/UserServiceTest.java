package personal.yeongyulgori.user.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import personal.yeongyulgori.user.constant.Role;
import personal.yeongyulgori.user.model.dto.UserResponseDto;
import personal.yeongyulgori.user.model.entity.User;
import personal.yeongyulgori.user.model.repository.UserRepository;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static personal.yeongyulgori.user.constant.Role.BUSINESS_USER;
import static personal.yeongyulgori.user.constant.Role.GENERAL_USER;
import static personal.yeongyulgori.user.testutil.TestObjectFactory.createUser;

@ActiveProfiles("test")
@SpringBootTest
class UserServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @AfterEach
    void tearDown() {
        userRepository.deleteAllInBatch();
    }

    @DisplayName("사용자 이름을 통해 다른 회원의 프로필을 조회할 수 있다.")
    @ParameterizedTest
    @CsvSource({
            "abcd@abc.com, person1, 1234, 홍길동, 2000-01-01, 01012345678, GENERAL_USER",
            "abcd@abcd.com, person2, 12345, 고길동, 2000-02-02, 01012345679, BUSINESS_USER",
            "abcd@abcde.com, person3, 123456, 김길동, 2000-03-03, 01012345680, GENERAL_USER"
    })
    void getUserProfile(
            String email, String username, String password,
            String name, LocalDate birthDate, String phoneNumber, Role role
    ) {

        // given
        User user = createUser(email, username, password, name, birthDate, phoneNumber, role);

        userRepository.save(user);

        // when
        User foundUser = userRepository.findByUsername(username).get();

        // then
        assertThat(foundUser.getUsername()).isEqualTo(username);
        assertThat(foundUser.getPassword()).isEqualTo(password);
        assertThat(foundUser.getName()).isEqualTo(name);
        assertThat(foundUser.getRole()).isEqualTo(role);

    }

    @DisplayName("존재하지 않는 사용자 이름으로 조회하면 EntityNotFoundException이 발생한다.")
    @Test
    void getUserProfileByWrongUsername() {

        // given
        User user = createUser(
                "abcd@abc.com", "person1", "1234", "홍길동",
                LocalDate.of(2000, 1, 1), "01012345678", Role.BUSINESS_USER
        );

        userRepository.save(user);

        // when, then
        assertThatThrownBy(() -> userService.getUserProfile("abcd@abcd.com"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당 회원이 존재하지 않습니다. username: " + "abcd@abcd.com");


    }

    @DisplayName("키워드를 통해 회원들을 검색하고 페이징 처리할 수 있다.")
    @Test
    void getSearchedUsers() {

        // given
        User user1 = createUser("abcd@abc.com", "person1", "1234", "홍길동",
                LocalDate.of(1990, 01, 01), "01012345678", GENERAL_USER);

        User user2 = createUser("abcd@abcd.com", "person2", "12345", "고길동",
                LocalDate.of(2000, 02, 10), "01012345679", BUSINESS_USER);

        User user3 = createUser("abcd@abcde.com", "person3", "123456", "김길동",
                LocalDate.of(2010, 03, 20), "01012345670", GENERAL_USER);

        User user4 = createUser("abcd@abcdef.com", "person4", "1234567", "홍길숙",
                LocalDate.of(2020, 04, 30), "01012345671", BUSINESS_USER);

        userRepository.saveAll(List.of(user1, user2, user3, user4));

        // when
        Page<UserResponseDto> userResponseDtos = userService.getSearchedUsers(
                "길동", Pageable.ofSize(2)
        );

        // then
        Assertions.assertThat(userResponseDtos).hasSize(2)
                .extracting("email", "username", "name", "role")
                .containsExactlyInAnyOrder(
                        tuple("abcd@abc.com", "person1", "홍길동", GENERAL_USER),
                        tuple("abcd@abcd.com", "person2", "고길동", BUSINESS_USER)
                );

    }

    @DisplayName("키워드를 전송하지 않으면 모든 사용자의 목록을 조회할 수 있다.")
    @Test
    void getUsersWithoutAnyKeyword() {

        User user1 = createUser("abcd@abc.com", "person1", "1234", "홍길동",
                LocalDate.of(1990, 01, 01), "01012345678", GENERAL_USER);

        User user2 = createUser("abcd@abcd.com", "person2", "12345", "고길동",
                LocalDate.of(2000, 02, 10), "01012345679", BUSINESS_USER);

        User user3 = createUser("abcd@abcde.com", "person3", "123456", "김길동",
                LocalDate.of(2010, 03, 20), "01012345670", GENERAL_USER);

        User user4 = createUser("abcd@abcdef.com", "person4", "1234567", "홍길숙",
                LocalDate.of(2020, 04, 30), "01012345671", BUSINESS_USER);

        userRepository.saveAll(List.of(user1, user2, user3, user4));

        // when
        Page<UserResponseDto> userResponseDtos = userService.getSearchedUsers(
                "", Pageable.ofSize(2)
        );

        // then
        Assertions.assertThat(userResponseDtos).hasSize(2)
                .extracting("email", "username", "name", "role")
                .containsExactlyInAnyOrder(
                        tuple("abcd@abc.com", "person1", "홍길동", GENERAL_USER),
                        tuple("abcd@abcd.com", "person2", "고길동", BUSINESS_USER)
                );

    }

}
