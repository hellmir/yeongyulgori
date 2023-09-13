package personal.yeongyulgori.user.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;
import personal.yeongyulgori.user.constant.Role;
import personal.yeongyulgori.user.domain.*;
import personal.yeongyulgori.user.domain.model.User;
import personal.yeongyulgori.user.domain.repository.UserRepository;
import personal.yeongyulgori.user.dto.UserResponseDto;
import personal.yeongyulgori.user.exception.general.sub.DuplicateUserException;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static personal.yeongyulgori.user.constant.Role.BUSINESS_USER;
import static personal.yeongyulgori.user.constant.Role.GENERAL_USER;
import static personal.yeongyulgori.user.testutil.TestObjectFactory.createUser;
import static personal.yeongyulgori.user.testutil.TestObjectFactory.enterUserForm;

@ActiveProfiles("test")
@SpringBootTest
class AuthenticationServiceTest {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAllInBatch();
    }

    @DisplayName("사용자가 올바른 양식을 입력하면 회원 가입을 할 수 있다.")
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
        UserResponseDto userResponseDto = authenticationService.signUpUser(signUpForm);

        // then
        assertThat(userResponseDto.getEmail()).isEqualTo(email);
        assertThat(userResponseDto.getUsername()).isEqualTo(username);
        assertThat(userResponseDto.getRole()).isEqualTo(role);

    }

    @DisplayName("회원 가입 시 중복된 이메일을 입력하면 UserAlreadyExistsException이 발생한다.")
    @Test
    void signUpUserByDuplicateEmail() {

        // given

        SignUpForm signUpForm1 = enterUserForm(
                "abcd@abc.com", "person1", "1234", "홍길동",
                LocalDate.of(2000, 1, 1), "01012345678", Role.GENERAL_USER
        );
        SignUpForm signUpForm2 = enterUserForm(
                "abcd@abc.com", "person2", "12345", "고길동",
                LocalDate.of(2000, 2, 2), "01012345679", BUSINESS_USER
        );

        UserResponseDto userResponseDto1 = authenticationService.signUpUser(signUpForm1);

        // when, then
        assertThatThrownBy(() -> authenticationService.signUpUser(signUpForm2))
                .isInstanceOf(DuplicateUserException.class)
                .hasMessage("이미 가입된 회원입니다. email: " + signUpForm2.getEmail());

    }

    // TODO
    @DisplayName("사용자가 올바른 양식을 입력하면 로그인을 할 수 있다.")
    @Test
    void signInUser() {

        // given
        SignInForm signInForm = new SignInForm("abcd@abc.com", "person1", "1234");

        // when
        String token = authenticationService.signInUser(signInForm);

        // then
        assertThat(token).isNull();

    }

    @DisplayName("사용자 이름을 통해 회원 개인 정보를 조회할 수 있다.")
    @ParameterizedTest
    @CsvSource({
            "abcd@abc.com, person1, 1234, 홍길동, 2000-01-01, 01012345678, GENERAL_USER",
            "abcd@abcd.com, person2, 12345, 고길동, 2000-02-02, 01012345679, BUSINESS_USER",
            "abcd@abcde.com, person3, 123456, 김길동, 2000-03-03, 01012345680, GENERAL_USER"
    })
    void getUserDetails(
            String email, String username, String password, String name,
            LocalDate birthDate, String phoneNumber, Role role
    ) {

        // given
        User user = createUser(email, username, password, name, birthDate, phoneNumber, role);

        userRepository.save(user);

        // when
        UserResponseDto userResponseDto = authenticationService.getUserDetails(username);

        // then
        assertThat(userResponseDto.getEmail()).isEqualTo(email);
        assertThat(userResponseDto.getUsername()).isEqualTo(username);
        assertThat(userResponseDto.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(userResponseDto.getRole()).isEqualTo(role);

    }

    @DisplayName("잘못된 사용자 이름으로 회원 개인 정보를 조회하면 EntityNotFoundException이 발생한다.")
    @Test
    void getUserDetailsByWrongUsername() {

        // given
        User user = createUser(
                "abcd@abc.com", "person1", "1234", "홍길동",
                LocalDate.of(2000, 1, 1), "01012345678", Role.GENERAL_USER
        );

        userRepository.save(user);

        // when, then
        assertThatThrownBy(() -> authenticationService.getUserDetails("person2"))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당 회원이 존재하지 않습니다. username: person2");

    }

    @DisplayName("사용자 이름과 informationUpdateForm을 통해 다수의 회원 개인 정보를 수정할 수 있다.")
    @Test
    void updateUserInformations() {

        // given
        User user1 = createUser(
                "abcd@abc.com", "person1", "1234", "홍길동",
                LocalDate.of(2000, 1, 1), "01012345678", Role.GENERAL_USER
        );
        User user2 = createUser(
                "abcd@abcd.com", "person2", "12345", "고길동",
                LocalDate.of(2000, 2, 2), "01012345679", BUSINESS_USER
        );
        User user3 = createUser(
                "abcd@abcde.com", "person3", "123456", "길동",
                LocalDate.of(2000, 3, 3), "01012345670", Role.GENERAL_USER
        );

        userRepository.saveAll(List.of(user1, user2, user3));

        InformationUpdateForm informationUpdateForm1 = InformationUpdateForm.builder()
                .id(user1.getId())
                .name("김길동")
                .build();

        InformationUpdateForm informationUpdateForm2 = InformationUpdateForm.builder()
                .id(user2.getId())
                .address(Address.builder()
                        .city("서울")
                        .build())
                .role(GENERAL_USER)
                .build();

        InformationUpdateForm informationUpdateForm3 = InformationUpdateForm.builder()
                .id(user3.getId())
                .name("이길동")
                .address(Address.builder()
                        .street("테헤란로 231")
                        .zipcode("12345")
                        .build())
                .profileImage(mock(MultipartFile.class))
                .role(BUSINESS_USER)
                .build();

        // when
        UserResponseDto userResponseDto1 = authenticationService.updateUserInformations("person0", informationUpdateForm1);
        UserResponseDto userResponseDto2 = authenticationService.updateUserInformations(user2.getUsername(), informationUpdateForm2);
        UserResponseDto userResponseDto3 = authenticationService.updateUserInformations("person4", informationUpdateForm3);

        // then
        assertThat(userResponseDto1.getUsername()).isEqualTo("person0");
        assertThat(userResponseDto1.getName()).isEqualTo("김길동");
        assertThat(userResponseDto1.getPhoneNumber()).isEqualTo("01012345678");
        assertThat(userResponseDto1.getRole()).isEqualTo(GENERAL_USER);

        assertThat(userResponseDto2.getUsername()).isEqualTo("person2");
        assertThat(userResponseDto2.getName()).isEqualTo("고길동");
        assertThat(userResponseDto2.getAddress().getCity()).isEqualTo("서울");
        assertThat(userResponseDto2.getRole()).isEqualTo(GENERAL_USER);

        assertThat(userResponseDto3.getUsername()).isEqualTo("person4");
        assertThat(userResponseDto3.getName()).isEqualTo("이길동");
        assertThat(userResponseDto3.getAddress().getStreet()).isEqualTo("테헤란로 231");
        assertThat(userResponseDto3.getAddress().getZipcode()).isEqualTo("12345");
        assertThat(userResponseDto3.getRole()).isEqualTo(BUSINESS_USER);

    }

    @DisplayName("잘못된 id로 회원 개인 정보를 수정하려 하면 EntityNotFoundException이 발생한다.")
    @Test
    void updateUserInformationsByWrongUserId() {

        // given
        User user = createUser(
                "abcd@abc.com", "person1", "1234", "홍길동",
                LocalDate.of(2000, 1, 1), "01012345678", Role.GENERAL_USER
        );

        userRepository.save(user);

        InformationUpdateForm informationUpdateForm = InformationUpdateForm.builder()
                .id(user.getId() + 1)
                .build();

        // when, then
        assertThatThrownBy(
                () -> authenticationService.updateUserInformations(user.getUsername(), informationUpdateForm)
        )
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당 회원이 존재하지 않습니다. username: " + user.getUsername());

    }

    @DisplayName("crucialInformationUpdateForm을 통해 하나의 중요한 회원 개인 정보를 수정할 수 있다.")
    @Test
    void updateCrucialUserInformation() {

        // given
        User user1 = createUser(
                "abcd@abc.com", "person1", "1234", "홍길동",
                LocalDate.of(2000, 1, 1), "01012345678", Role.GENERAL_USER
        );
        User user2 = createUser(
                "abcd@abcd.com", "person2", "12345", "고길동",
                LocalDate.of(2000, 2, 2), "01012345679", BUSINESS_USER
        );
        User user3 = createUser(
                "abcd@abcde.com", "person3", "123456", "길동",
                LocalDate.of(2000, 3, 3), "01012345670", Role.GENERAL_USER
        );

        userRepository.saveAll(List.of(user1, user2, user3));

        CrucialInformationUpdateForm crucialInformationUpdateForm1 = CrucialInformationUpdateForm.builder()
                .id(user1.getId())
                .email("abcd@abcdef.com")
                .build();

        CrucialInformationUpdateForm crucialInformationUpdateForm2 = CrucialInformationUpdateForm.builder()
                .id(user2.getId())
                .password("123456")
                .build();

        CrucialInformationUpdateForm crucialInformationUpdateForm3 = CrucialInformationUpdateForm.builder()
                .id(user3.getId())
                .phoneNumber("01012345671")
                .build();

        // when

        authenticationService.updateCrucialUserInformation(user1.getUsername(), crucialInformationUpdateForm1);
        authenticationService.updateCrucialUserInformation(user2.getUsername(), crucialInformationUpdateForm2);
        authenticationService.updateCrucialUserInformation(user3.getUsername(), crucialInformationUpdateForm3);

        User savedUser1 = userRepository.findById(user1.getId()).get();
        User savedUser2 = userRepository.findById(user2.getId()).get();
        User savedUser3 = userRepository.findById(user3.getId()).get();

        // then
        assertThat(savedUser1.getEmail()).isEqualTo("abcd@abcdef.com");
        assertThat(savedUser1.getPassword()).isEqualTo("1234");
        assertThat(savedUser1.getPhoneNumber()).isEqualTo("01012345678");

        assertThat(savedUser2.getEmail()).isEqualTo("abcd@abcd.com");
        assertThat(savedUser2.getPassword()).isEqualTo("123456");
        assertThat(savedUser2.getPhoneNumber()).isEqualTo("01012345679");

        assertThat(savedUser3.getEmail()).isEqualTo("abcd@abcde.com");
        assertThat(savedUser3.getPassword()).isEqualTo("123456");
        assertThat(savedUser3.getPhoneNumber()).isEqualTo("01012345671");

    }

    @DisplayName("잘못된 id로 중요한 회원 개인 정보를 수정하려 하면 EntityNotFoundException이 발생한다.")
    @Test
    void updateCrucialUserInformationByWrongUserId() {

        // given
        User user = createUser(
                "abcd@abc.com", "person1", "1234", "홍길동",
                LocalDate.of(2000, 1, 1), "01012345678", Role.GENERAL_USER
        );

        userRepository.save(user);

        CrucialInformationUpdateForm crucialInformationUpdateForm = CrucialInformationUpdateForm.builder()
                .id(user.getId() + 1)
                .build();

        // when, then
        assertThatThrownBy(
                () -> authenticationService.updateCrucialUserInformation(
                        user.getUsername(), crucialInformationUpdateForm
                )
        )
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당 회원이 존재하지 않습니다. username: " + user.getUsername());

    }

    // TODO
    @DisplayName("비밀번호 인증을 통해 회원을 탈퇴할 수 있다.")
    @ParameterizedTest
    @CsvSource({
            "abcd@abc.com, person1, 1234, 홍길동, 2000-01-01, 01012345678, GENERAL_USER",
            "abcd@abcd.com, person2, 12345, 고길동, 2000-02-02, 01012345679, BUSINESS_USER",
            "abcd@abcde.com, person3, 123456, 김길동, 2000-03-03, 01012345680, GENERAL_USER"
    })
    void deleteUser(
            String email, String username, String password,
            String name, LocalDate birthDate, String phoneNumber, Role role
    ) {

        // given
        User user = createUser(email, username, password, name, birthDate, phoneNumber, role);

        userRepository.save(user);

        // when
        authenticationService.deleteUser(username, password);

        // then
        assertThat(userRepository.findById(user.getId()).isPresent()).isFalse();

    }

    // TODO
    @DisplayName("존재하지 않는 사용자 이름으로 회원을 탈퇴하려 하면 EntityNotFoundException이 발생한다.")
    @Test
    void deleteUserByWrongUsername() {

        // given
        User user = createUser(
                "abcd@abc.com", "person1", "1234", "홍길동",
                LocalDate.of(2000, 1, 1), "01012345678", Role.GENERAL_USER
        );

        userRepository.save(user);

        // when, then
        assertThatThrownBy(
                () -> authenticationService.deleteUser("person2", "1234")
        )
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당 회원이 존재하지 않습니다. username: person2");

    }

}
