package personal.yeongyulgori.user.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import personal.yeongyulgori.user.exception.general.sub.DuplicateUserException;
import personal.yeongyulgori.user.exception.general.sub.DuplicateUsernameException;
import personal.yeongyulgori.user.exception.serious.sub.NonExistentUserException;
import personal.yeongyulgori.user.exception.significant.sub.IncorrectPasswordException;
import personal.yeongyulgori.user.exception.significant.sub.TokenExpiredException;
import personal.yeongyulgori.user.model.constant.Role;
import personal.yeongyulgori.user.model.dto.CrucialInformationUpdateDto;
import personal.yeongyulgori.user.model.dto.PasswordRequestDto;
import personal.yeongyulgori.user.model.dto.SignInResponseDto;
import personal.yeongyulgori.user.model.dto.UserResponseDto;
import personal.yeongyulgori.user.model.entity.PasswordResetToken;
import personal.yeongyulgori.user.model.entity.User;
import personal.yeongyulgori.user.model.entity.embedment.Address;
import personal.yeongyulgori.user.model.form.InformationUpdateForm;
import personal.yeongyulgori.user.model.form.SignInForm;
import personal.yeongyulgori.user.model.form.SignUpForm;
import personal.yeongyulgori.user.model.repository.PasswordResetTokenRepository;
import personal.yeongyulgori.user.model.repository.UserRepository;
import personal.yeongyulgori.user.security.JwtTokenProvider;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static personal.yeongyulgori.user.model.constant.Role.*;
import static personal.yeongyulgori.user.testutil.TestConstant.*;
import static personal.yeongyulgori.user.testutil.TestObjectFactory.*;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class AuthenticationServiceTest {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AutoCompleteService autoCompleteService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Value("${spring.redis.host}")
    private String ec2Ip;

    @Value(("${server.port}"))
    private String serverPort;

    @DisplayName("올바른 회원 가입 양식을 전송하면 회원 가입을 할 수 있다.")
    @ParameterizedTest
    @CsvSource({
            "abcd@abc.com, person1, 1234, 홍길동, 2000-01-01, 01012345678, ROLE_GENERAL_USER",
            "abcd@abcd.com, person2, 12345, 고길동, 2000-02-02, 01012345679, ROLE_BUSINESS_USER",
            "abcd@abcde.com, person3, 123456, 김길동, 2000-03-03, 01012345680, ROLE_ADMIN"
    })
    void signUpUser(String email, String username, String password, String fullName,
                    LocalDate birthDate, String phoneNumber, Role role) {

        // given
        SignUpForm signUpForm = enterUserForm
                (email, username, password, fullName, birthDate, phoneNumber, List.of(role));

        // when
        UserResponseDto userResponseDto = authenticationService.signUpUser(signUpForm);

        // then
        assertThat(userResponseDto.getEmail()).isEqualTo(email);
        assertThat(userResponseDto.getUsername()).isEqualTo(username);
        assertThat(userResponseDto.getRoles().get(0)).isEqualTo(role);

    }

    @DisplayName("올바른 회원 가입 양식을 전송하면 여러 권한을 가진 회원으로 가입할 수 있다.")
    @ParameterizedTest
    @CsvSource({
            "abcd@abc.com, person1, 1234, 홍길동, 2000-01-01, 01012345678, ROLE_GENERAL_USER, ROLE_BUSINESS_USER",
            "abcd@abcd.com, person2, 12345, 고길동, 2000-02-02, 01012345679, ROLE_BUSINESS_USER, ROLE_GENERAL_USER",
            "abcd@abcde.com, person3, 123456, 김길동, 2000-03-03, 01012345680, ROLE_GENERAL_USER, ROLE_ADMIN"
    })
    void signUpUserWithMultipleRoles(String email, String username, String password, String fullName,
                                     LocalDate birthDate, String phoneNumber, Role role1, Role role2) {

        // given
        SignUpForm signUpForm = enterUserForm
                (email, username, password, fullName, birthDate, phoneNumber, List.of(role1, role2));

        // when
        UserResponseDto userResponseDto = authenticationService.signUpUser(signUpForm);

        // then
        assertThat(userResponseDto.getEmail()).isEqualTo(email);
        assertThat(userResponseDto.getUsername()).isEqualTo(username);
        assertThat(userResponseDto.getRoles().get(0)).isEqualTo(role1);
        assertThat(userResponseDto.getRoles().get(1)).isEqualTo(role2);

    }

    @DisplayName("중복된 이메일을 전송하면 UserAlreadyExistsException이 발생한다.")
    @Test
    void signUpUserByDuplicateEmail() {

        // given

        SignUpForm signUpForm1 = enterUserForm(EMAIL1, USERNAME1,
                PASSWORD1, FULL_NAME1, BIRTH_DATE1,
                PHONE_NUMBER1, List.of(ROLE_GENERAL_USER));

        authenticationService.signUpUser(signUpForm1);

        SignUpForm signUpForm2 = enterUserForm(EMAIL1, USERNAME2,
                PASSWORD2, FULL_NAME2, BIRTH_DATE2,
                PHONE_NUMBER2, List.of(ROLE_BUSINESS_USER));

        // when, then
        assertThatThrownBy(() -> authenticationService.signUpUser(signUpForm2))
                .isInstanceOf(DuplicateUserException.class)
                .hasMessage("이미 가입된 이메일입니다. email: " + signUpForm2.getEmail());

    }

    @DisplayName("중복된 휴대폰 번호를 전송하면 UserAlreadyExistsException이 발생한다.")
    @Test
    void signUpUserByDuplicatePhoneNumber() {

        // given

        SignUpForm signUpForm1 = enterUserForm(EMAIL1, USERNAME1,
                PASSWORD1, FULL_NAME1, BIRTH_DATE1,
                PHONE_NUMBER1, List.of(ROLE_GENERAL_USER));

        authenticationService.signUpUser(signUpForm1);

        SignUpForm signUpForm2 = enterUserForm(EMAIL2, USERNAME2,
                PASSWORD2, "고길동", BIRTH_DATE2,
                PHONE_NUMBER1, List.of(ROLE_BUSINESS_USER));

        // when, then
        assertThatThrownBy(() -> authenticationService.signUpUser(signUpForm2))
                .isInstanceOf(DuplicateUserException.class)
                .hasMessage("이미 가입된 전화번호입니다. phoneNumber: " + signUpForm2.getPhoneNumber());

    }

    @DisplayName("중복된 사용자 이름을 전송하면 UserAlreadyExistsException이 발생한다.")
    @Test
    void signUpUserByDuplicateUsername() {

        // given

        SignUpForm signUpForm1 = enterUserForm(EMAIL1, USERNAME1,
                PASSWORD1, FULL_NAME1, BIRTH_DATE1,
                PHONE_NUMBER1, List.of(ROLE_GENERAL_USER));

        authenticationService.signUpUser(signUpForm1);

        SignUpForm signUpForm2 = enterUserForm(EMAIL2, USERNAME1,
                PASSWORD2, FULL_NAME2, BIRTH_DATE2,
                PHONE_NUMBER2, List.of(ROLE_BUSINESS_USER));

        // when, then
        assertThatThrownBy(() -> authenticationService.signUpUser(signUpForm2))
                .isInstanceOf(DuplicateUsernameException.class)
                .hasMessage("중복된 사용자 이름입니다. username: " + signUpForm2.getUsername());

    }

    @DisplayName("가입된 이메일 주소 또는 사용자 이름과 올바른 비밀번호를 전송하면 로그인을 할 수 있다.")
    @Test
    void signInUser() {

        // given
        SignUpForm signUpForm = enterUserForm(EMAIL1, USERNAME1,
                PASSWORD1, FULL_NAME1, BIRTH_DATE1,
                PHONE_NUMBER1, List.of(ROLE_GENERAL_USER));

        authenticationService.signUpUser(signUpForm);

        SignInForm signInForm1 = new SignInForm(EMAIL1, PASSWORD1);
        SignInForm signInForm2 = new SignInForm(USERNAME1, PASSWORD1);

        // when
        SignInResponseDto signInResponseDto1 = authenticationService.signInUser(signInForm1);
        SignInResponseDto signInResponseDto2 = authenticationService.signInUser(signInForm2);

        // then
        assertThat(signInResponseDto1.getUsername()).isEqualTo(USERNAME1);
        assertThat(signInResponseDto2.getUsername()).isEqualTo(USERNAME1);

    }

    @DisplayName("존재하지 않는 이메일이나 사용자 이름으로 로그인하려 하면 EntityNotFoundException이 발생한다.")
    @Test
    void signInUserWithNonExistentEmailOrUsername() {

        // given
        SignUpForm signUpForm = enterUserForm(EMAIL1, USERNAME1,
                PASSWORD1, FULL_NAME1, BIRTH_DATE1,
                PHONE_NUMBER1, List.of(ROLE_GENERAL_USER));

        authenticationService.signUpUser(signUpForm);

        SignInForm signInForm1 = new SignInForm(EMAIL2, PASSWORD1);
        SignInForm signInForm2 = new SignInForm(USERNAME2, PASSWORD1);

        // when, then
        assertThatThrownBy(() -> authenticationService.signInUser(signInForm1))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당 회원이 존재하지 않습니다. email: " + signInForm1.getEmailOrUsername());

        assertThatThrownBy(() -> authenticationService.signInUser(signInForm2))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당 회원이 존재하지 않습니다. username: " + signInForm2.getEmailOrUsername());

    }

    @DisplayName("잘못된 비밀번호를 통해 로그인하려 하면 IncorrectPasswordException이 발생한다.")
    @Test
    void signInUserWithWrongPassword() {

        // given
        SignUpForm signUpForm = enterUserForm(EMAIL1, USERNAME1,
                PASSWORD1, FULL_NAME1, BIRTH_DATE1,
                PHONE_NUMBER1, List.of(ROLE_GENERAL_USER));

        authenticationService.signUpUser(signUpForm);

        SignInForm signInForm1 = new SignInForm(EMAIL1, PASSWORD1);
        SignInForm signInForm2 = new SignInForm(USERNAME1, PASSWORD1);

        // when, then

    }

    @DisplayName("사용자 이름을 통해 회원 개인 정보를 조회할 수 있다.")
    @ParameterizedTest
    @CsvSource({
            "abcd@abc.com, person1, 1234, 홍길동, 2000-01-01, 01012345678, ROLE_GENERAL_USER",
            "abcd@abcd.com, person2, 12345, 고길동, 2000-02-02, 01012345679, ROLE_BUSINESS_USER",
            "abcd@abcde.com, person3, 123456, 김길동, 2000-03-03, 01012345680, ROLE_GENERAL_USER"
    })
    void getUserDetails(String email, String username, String password, String fullName,
                        LocalDate birthDate, String phoneNumber, Role role) {

        // given
        User user = createUser(email, username, password, fullName, birthDate, phoneNumber, List.of(role));

        userRepository.save(user);

        // when
        UserResponseDto userResponseDto = authenticationService.getUserDetails(username);

        // then
        assertThat(userResponseDto.getEmail()).isEqualTo(email);
        assertThat(userResponseDto.getUsername()).isEqualTo(username);
        assertThat(userResponseDto.getPhoneNumber()).isEqualTo(phoneNumber);
        assertThat(userResponseDto.getRoles()).isEqualTo(user.getRoles());

    }

    @DisplayName("잘못된 사용자 이름으로 회원 개인 정보를 조회하면 EntityNotFoundException이 발생한다.")
    @Test
    void getUserDetailsByWrongUsername() {

        // given
        User user = createUser(EMAIL1, USERNAME1, PASSWORD1, FULL_NAME1,
                BIRTH_DATE1, PHONE_NUMBER1, List.of(ROLE_GENERAL_USER));

        userRepository.save(user);

        // when, then
        assertThatThrownBy(() -> authenticationService.getUserDetails(USERNAME2))
                .isInstanceOf(NonExistentUserException.class)
                .hasMessage("해당 회원이 존재하지 않습니다. username: " + USERNAME2);

    }

    @DisplayName("사용자 이름과 informationUpdateForm 양식을 통해 다수의 회원 개인 정보를 수정할 수 있다.")
    @Test
    void updateUserInformation() {

        // given
        User user1 = createUserWithAddress(EMAIL1, USERNAME1,
                PASSWORD1, FULL_NAME1, BIRTH_DATE1,
                PHONE_NUMBER1, new ArrayList<>(List.of(ROLE_GENERAL_USER)));
        User user2 = createUserWithAddress(EMAIL2, USERNAME2,
                PASSWORD2, FULL_NAME2, BIRTH_DATE2,
                PHONE_NUMBER2, new ArrayList<>(List.of(ROLE_BUSINESS_USER)));
        User user3 = createUserWithAddress(EMAIL3, USERNAME3,
                PASSWORD3, "길동", BIRTH_DATE3,
                PHONE_NUMBER3, new ArrayList<>(List.of(ROLE_GENERAL_USER)));

        userRepository.saveAll(List.of(user1, user2, user3));

        InformationUpdateForm informationUpdateForm1 = InformationUpdateForm.builder()
                .id(user1.getId())
                .fullName(FULL_NAME3)
                .build();

        InformationUpdateForm informationUpdateForm2 = InformationUpdateForm.builder()
                .id(user2.getId())
                .address(Address.builder()
                        .city(CITY)
                        .build())
                .roles(List.of(ROLE_GENERAL_USER))
                .build();

        InformationUpdateForm informationUpdateForm3 = InformationUpdateForm.builder()
                .id(user3.getId())
                .fullName(FULL_NAME4)
                .address(Address.builder()
                        .street(STREET)
                        .zipcode(PASSWORD2)
                        .build())
                .profileImage(TEST_IMAGE)
                .roles(List.of(ROLE_BUSINESS_USER))
                .build();

        // when
        UserResponseDto userResponseDto1 = authenticationService
                .updateUserInformation(USERNAME4, informationUpdateForm1);
        UserResponseDto userResponseDto2 = authenticationService
                .updateUserInformation(user2.getUsername(), informationUpdateForm2);
        UserResponseDto userResponseDto3 = authenticationService
                .updateUserInformation(USERNAME5, informationUpdateForm3);

        // then
        assertThat(userResponseDto1.getUsername()).isEqualTo(USERNAME4);
        assertThat(userResponseDto1.getFullName()).isEqualTo(FULL_NAME3);
        assertThat(userResponseDto1.getPhoneNumber()).isEqualTo(PHONE_NUMBER1);
        assertThat(userResponseDto1.getRoles()).isEqualTo(user1.getRoles());

        assertThat(userResponseDto2.getUsername()).isEqualTo(USERNAME2);
        assertThat(userResponseDto2.getFullName()).isEqualTo(FULL_NAME2);
        assertThat(userResponseDto2.getAddress().getCity()).isEqualTo(CITY);
        assertThat(userResponseDto2.getRoles()).isEqualTo(user2.getRoles());

        assertThat(userResponseDto3.getUsername()).isEqualTo(USERNAME5);
        assertThat(userResponseDto3.getFullName()).isEqualTo(FULL_NAME4);
        assertThat(userResponseDto3.getAddress().getStreet()).isEqualTo(STREET);
        assertThat(userResponseDto3.getAddress().getZipcode()).isEqualTo(PASSWORD2);
        assertThat(userResponseDto3.getRoles()).isEqualTo(user3.getRoles());

    }

    @DisplayName("잘못된 id로 회원 개인 정보를 수정하려 하면 EntityNotFoundException이 발생한다.")
    @Test
    void updateUserInformationByWrongUserId() {

        // given
        User user = createUser(EMAIL1, USERNAME1, PASSWORD1, FULL_NAME1,
                BIRTH_DATE1, PHONE_NUMBER1, List.of(ROLE_GENERAL_USER));

        userRepository.save(user);

        InformationUpdateForm informationUpdateForm = InformationUpdateForm.builder()
                .id(user.getId() + 1)
                .build();

        // when, then
        assertThatThrownBy(() -> authenticationService
                .updateUserInformation(user.getUsername(), informationUpdateForm))
                .isInstanceOf(NonExistentUserException.class)
                .hasMessage("해당 회원이 존재하지 않습니다. username: " + user.getUsername());

    }

    @DisplayName("crucialInformationUpdateDto를 통해 하나의 중요한 회원 개인 정보를 수정할 수 있다.")
    @Test
    void updateCrucialUserInformation() {

        // given
        User user1 = createUser(EMAIL1, USERNAME1, PASSWORD1, FULL_NAME1,
                BIRTH_DATE1,
                PHONE_NUMBER1, new ArrayList<>(List.of(ROLE_GENERAL_USER)));
        User user2 = createUser(EMAIL2, USERNAME2, PASSWORD2, FULL_NAME2,
                BIRTH_DATE2,
                PHONE_NUMBER2, new ArrayList<>(List.of(ROLE_BUSINESS_USER)));
        User user3 = createUser(EMAIL3, USERNAME3, PASSWORD3, FULL_NAME3,
                BIRTH_DATE3,
                PHONE_NUMBER3, new ArrayList<>(List.of(ROLE_ADMIN)));

        userRepository.saveAll(List.of(user1, user2, user3));

        CrucialInformationUpdateDto crucialInformationUpdateDto1 = CrucialInformationUpdateDto.builder()
                .id(user1.getId())
                .email(EMAIL4)
                .build();

        CrucialInformationUpdateDto crucialInformationUpdateDto2 = CrucialInformationUpdateDto.builder()
                .id(user2.getId())
                .password(PASSWORD3)
                .build();

        CrucialInformationUpdateDto crucialInformationUpdateDto3 = CrucialInformationUpdateDto.builder()
                .id(user3.getId())
                .phoneNumber(PHONE_NUMBER4)
                .build();

        // when
        authenticationService.updateCrucialUserInformation(user1.getUsername(), crucialInformationUpdateDto1);
        authenticationService.updateCrucialUserInformation(user2.getUsername(), crucialInformationUpdateDto2);
        authenticationService.updateCrucialUserInformation(user3.getUsername(), crucialInformationUpdateDto3);

        User savedUser1 = userRepository.findById(user1.getId()).get();
        User savedUser2 = userRepository.findById(user2.getId()).get();
        User savedUser3 = userRepository.findById(user3.getId()).get();

        // then
        assertThat(savedUser1.getEmail()).isEqualTo(EMAIL4);
        assertThat(savedUser1.getPassword()).isEqualTo(PASSWORD1);
        assertThat(savedUser1.getPhoneNumber()).isEqualTo(PHONE_NUMBER1);

        assertThat(savedUser2.getEmail()).isEqualTo(EMAIL2);
        assertThat(savedUser2.getPassword()).isEqualTo(PASSWORD3);
        assertThat(savedUser2.getPhoneNumber()).isEqualTo(PHONE_NUMBER2);

        assertThat(savedUser3.getEmail()).isEqualTo(EMAIL3);
        assertThat(savedUser3.getPassword()).isEqualTo(PASSWORD3);
        assertThat(savedUser3.getPhoneNumber()).isEqualTo(PHONE_NUMBER4);

    }

    @DisplayName("잘못된 id로 중요한 회원 개인 정보를 수정하려 하면 EntityNotFoundException이 발생한다.")
    @Test
    void updateCrucialUserInformationByWrongUserId() {

        // given
        User user = createUser(EMAIL1, USERNAME1, PASSWORD1, FULL_NAME1,
                BIRTH_DATE1, PHONE_NUMBER1, List.of(ROLE_GENERAL_USER));

        userRepository.save(user);

        CrucialInformationUpdateDto crucialInformationUpdateDto = CrucialInformationUpdateDto.builder()
                .id(user.getId() + 1)
                .build();

        // when, then
        assertThatThrownBy(() -> authenticationService
                .updateCrucialUserInformation(user.getUsername(), crucialInformationUpdateDto))
                .isInstanceOf(NonExistentUserException.class)
                .hasMessage("해당 회원이 존재하지 않습니다. username: " + user.getUsername());

    }

    @DisplayName("이메일 주소를 통해 비밀번호 재설정을 요청하고, 비밀번호 재설정 URL을 반환 받을 수 있다.")
    @Test
    void requestPasswordReset() {

        // given
        User user = createUser(EMAIL1, USERNAME1, PASSWORD1, FULL_NAME1,
                BIRTH_DATE1, PHONE_NUMBER1, List.of(ROLE_GENERAL_USER));

        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(EMAIL1);

        // when
        String url = authenticationService.requestPasswordReset(user.getEmail(), token);

        // then
        assertThat(url).isEqualTo("http://" + ec2Ip + ":" + serverPort + "/password-reset?token=" + token);

    }

    @DisplayName("잘못된 형식의 이메일 주소를 전송하면 IllegalStateException이 발생한다.")
    @Test
    void requestPasswordResetWithWrongEmailForm() {

        // given
        User user = createUser(EMAIL1, USERNAME1, PASSWORD1, FULL_NAME1,
                BIRTH_DATE1, PHONE_NUMBER1, List.of(ROLE_GENERAL_USER));

        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(EMAIL1);

        // when, then
        assertThatThrownBy(() -> authenticationService.requestPasswordReset("abcde.com", token))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("email 형식이 올바르지 않습니다. 예: " + EMAIL1);

    }

    @DisplayName("존재하지 않는 이메일 주소를 전송하면 NonExistenceException이 발생한다.")
    @Test
    void requestPasswordResetWithNonExisetentEmail() {

        // given
        User user = createUser(EMAIL1, USERNAME1, PASSWORD1, FULL_NAME1,
                BIRTH_DATE1, PHONE_NUMBER1, List.of(ROLE_GENERAL_USER));

        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(EMAIL1);

        // when, then
        assertThatThrownBy(() -> authenticationService.requestPasswordReset(EMAIL2, token))
                .isInstanceOf(NonExistentUserException.class)
                .hasMessage("해당 회원이 존재하지 않습니다. email: " + EMAIL2);

    }

    @DisplayName("발급 받은 토큰과 새로운 비밀번호를 통해 새로운 비밀번호를 설정할 수 있다.")
    @Test
    void resetPassword() {

        // given
        User user = createUser(EMAIL1, USERNAME1, PASSWORD1, FULL_NAME1,
                BIRTH_DATE1, PHONE_NUMBER1, List.of(ROLE_GENERAL_USER));

        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(EMAIL1);

        authenticationService.requestPasswordReset(user.getEmail(), token);

        entityManager.refresh(user);

        PasswordRequestDto passwordRequestDto = new PasswordRequestDto(PASSWORD2);

        // when
        authenticationService.resetPassword(token, passwordRequestDto);

        // then
        assertThat(user.getPassword()).isEqualTo(PASSWORD2);

    }

    @DisplayName("존재하지 않는 토큰을 통해 비밀번호를 재설정하려 하면 EntityNotFoundException이 발생한다.")
    @Test
    void resetPasswordWithInvalidToken() {

        // given
        User user = createUser(EMAIL1, USERNAME1, PASSWORD1, FULL_NAME1,
                BIRTH_DATE1, PHONE_NUMBER1, List.of(ROLE_GENERAL_USER));

        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(EMAIL1);

        authenticationService.requestPasswordReset(user.getEmail(), token);

        entityManager.refresh(user);

        PasswordRequestDto passwordRequestDto = new PasswordRequestDto(PASSWORD2);

        // when, then
        assertThatThrownBy(() -> authenticationService.resetPassword(token + "-", passwordRequestDto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("토큰이 유효하지 않습니다. 잘못된 URL이 입력되었을 수 있습니다.");

    }

    @DisplayName("유효기간이 만료된 토큰을 통해 비밀번호를 재설정하려 하면 TokenExpiredException이 발생한다.")
    @Test
    void resetPasswordWithExpiredToken() {

        // given
        User user = createUser(EMAIL1, USERNAME1, PASSWORD1, FULL_NAME1,
                BIRTH_DATE1, PHONE_NUMBER1, List.of(ROLE_GENERAL_USER));

        userRepository.save(user);

        String token = jwtTokenProvider.generateToken(EMAIL1);

        authenticationService.requestPasswordReset(user.getEmail(), token);

        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findById(token).get();
        passwordResetToken.setExpirationDate(LocalDateTime.now().minusDays(1));

        entityManager.refresh(user);

        PasswordRequestDto passwordRequestDto = new PasswordRequestDto(PASSWORD2);

        // when, then
        assertThatThrownBy(() -> authenticationService.resetPassword(token, passwordRequestDto))
                .isInstanceOf(TokenExpiredException.class)
                .hasMessage("토큰이 만료되었습니다. 비밀번호를 재설정하려면 새로운 토큰을 발급 받아야 합니다.");

    }

    @DisplayName("비밀번호 인증을 통해 회원을 탈퇴할 수 있다.")
    @ParameterizedTest
    @CsvSource({
            "abcd@abc.com, person1, 1234, 홍길동, 2000-01-01, 01012345678, ROLE_GENERAL_USER",
            "abcd@abcd.com, person2, 12345, 고길동, 2000-02-02, 01012345679, ROLE_BUSINESS_USER",
            "abcd@abcde.com, person3, 123456, 김길동, 2000-03-03, 01012345680, ROLE_GENERAL_USER"
    })
    void deleteUser(
            String email, String username, String password,
            String fullName, LocalDate birthDate, String phoneNumber, Role role
    ) {

        // given
        SignUpForm signUpForm = enterUserForm
                (email, username, password, fullName, birthDate, phoneNumber, List.of(role));

        authenticationService.signUpUser(signUpForm);

        Long userId = userRepository.findByEmail(email).get().getId();

        PasswordRequestDto passwordRequestDto = new PasswordRequestDto(password);

        // when
        authenticationService.deleteUser(username, passwordRequestDto);

        // then
        assertThat(userRepository.findById(userId).isPresent()).isFalse();

    }

    @DisplayName("존재하지 않는 사용자 이름으로 회원을 탈퇴하려 하면 NonExistentUserException이 발생한다.")
    @Test
    void deleteUserByWrongUsername() {

        // given
        SignUpForm signUpForm = enterUserForm
                (EMAIL1, USERNAME1, PASSWORD1, FULL_NAME1,
                        BIRTH_DATE1, PHONE_NUMBER1, List.of(ROLE_GENERAL_USER));

        authenticationService.signUpUser(signUpForm);

        PasswordRequestDto passwordRequestDto = new PasswordRequestDto(PASSWORD1);

        // when, then
        assertThatThrownBy(() -> authenticationService
                .deleteUser(USERNAME2, passwordRequestDto))
                .isInstanceOf(NonExistentUserException.class)
                .hasMessage("해당 회원이 존재하지 않습니다. username: " + USERNAME2);

    }

    @DisplayName("일치하지 않는 비밀번호로 회원을 탈퇴하려 하면 IncorrectPasswordException이 발생한다.")
    @Test
    void deleteUserByWrongPassword() {

        // given
        SignUpForm signUpForm = enterUserForm
                (EMAIL1, USERNAME1, PASSWORD1, FULL_NAME1,
                        BIRTH_DATE1, PHONE_NUMBER1, List.of(ROLE_GENERAL_USER));

        authenticationService.signUpUser(signUpForm);

        PasswordRequestDto passwordRequestDto = new PasswordRequestDto(PASSWORD2);

        // when, then
        assertThatThrownBy(() -> authenticationService
                .deleteUser(USERNAME1, passwordRequestDto))
                .isInstanceOf(IncorrectPasswordException.class)
                .hasMessage("비밀번호가 일치하지 않습니다.");

    }

}
