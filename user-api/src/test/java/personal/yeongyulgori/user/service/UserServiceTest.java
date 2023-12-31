package personal.yeongyulgori.user.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import personal.yeongyulgori.user.model.constant.Role;
import personal.yeongyulgori.user.model.dto.UserResponseDto;
import personal.yeongyulgori.user.model.entity.User;
import personal.yeongyulgori.user.model.repository.UserRepository;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static personal.yeongyulgori.user.model.constant.Role.ROLE_BUSINESS_USER;
import static personal.yeongyulgori.user.model.constant.Role.ROLE_GENERAL_USER;
import static personal.yeongyulgori.user.testutil.TestConstant.*;
import static personal.yeongyulgori.user.testutil.TestObjectFactory.createUser;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @DisplayName("사용자 이름을 통해 다른 회원의 프로필을 조회할 수 있다.")
    @ParameterizedTest
    @CsvSource({
            "abcd@abc.com, person1, 1234, 홍길동, 2000-01-01, 01012345678, ROLE_GENERAL_USER",
            "abcd@abcd.com, person2, 12345, 고길동, 2000-02-02, 01012345679, ROLE_BUSINESS_USER",
            "abcd@abcde.com, person3, 123456, 김길동, 2000-03-03, 01012345680, ROLE_GENERAL_USER"
    })
    void getUserProfile(
            String email, String username, String password,
            String fullName, LocalDate birthDate, String phoneNumber, Role role
    ) {

        // given
        User user = createUser(email, username, password, fullName, birthDate, phoneNumber, List.of(role));

        userRepository.save(user);

        // when
        UserResponseDto userResponseDto = userService.getUserProfile(username);

        // then
        assertThat(userResponseDto.getUsername()).isEqualTo(username);
        assertThat(userResponseDto.getFullName()).isEqualTo(fullName);
        assertThat(userResponseDto.getRoles()).isEqualTo(user.getRoles());

    }

    @DisplayName("존재하지 않는 사용자 이름으로 조회하면 EntityNotFoundException이 발생한다.")
    @Test
    void getUserProfileByWrongUsername() {

        // given
        User user = createUser(
                EMAIL1, USERNAME1, PASSWORD1, FULL_NAME1,
                BIRTH_DATE1, PHONE_NUMBER1, List.of(ROLE_BUSINESS_USER)
        );

        userRepository.save(user);

        // when, then
        assertThatThrownBy(() -> userService.getUserProfile(USERNAME2))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당 회원이 존재하지 않습니다. username: " + USERNAME2);


    }

    @DisplayName("키워드를 통해 회원들을 검색하고 페이징 처리할 수 있다.")
    @Test
    void getSearchedUsers() {

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
        Page<UserResponseDto> userResponseDtos = userService.getSearchedUsers(
                FIRST_NAME, Pageable.ofSize(2)
        );

        // then
        Assertions.assertThat(userResponseDtos).hasSize(2)
                .extracting("email", "username", "fullName", "roles")
                .containsExactlyInAnyOrder(
                        tuple(EMAIL1, USERNAME1, FULL_NAME1, user1.getRoles()),
                        tuple(EMAIL2, USERNAME2, FULL_NAME2, user2.getRoles())
                );

    }

    @DisplayName("키워드를 전송하지 않으면 모든 사용자의 목록을 조회할 수 있다.")
    @Test
    void getUsersWithoutAnyKeyword() {

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
        Page<UserResponseDto> userResponseDtos = userService.getSearchedUsers(
                "", Pageable.ofSize(2)
        );

        // then
        Assertions.assertThat(userResponseDtos).hasSize(2)
                .extracting("email", "username", "fullName", "roles")
                .containsExactlyInAnyOrder(
                        tuple(EMAIL1, USERNAME1, FULL_NAME1, user1.getRoles()),
                        tuple(EMAIL2, USERNAME2, FULL_NAME2, user2.getRoles())
                );

    }

}
