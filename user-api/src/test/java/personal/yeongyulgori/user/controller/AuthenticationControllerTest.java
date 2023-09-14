package personal.yeongyulgori.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import personal.yeongyulgori.user.constant.Role;
import personal.yeongyulgori.user.domain.Address;
import personal.yeongyulgori.user.domain.InformationUpdateForm;
import personal.yeongyulgori.user.domain.SignUpForm;
import personal.yeongyulgori.user.domain.model.User;
import personal.yeongyulgori.user.domain.repository.UserRepository;
import personal.yeongyulgori.user.dto.CrucialInformationUpdateDto;
import personal.yeongyulgori.user.dto.UserResponseDto;
import personal.yeongyulgori.user.service.AuthenticationService;
import personal.yeongyulgori.user.service.AutoCompleteService;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static personal.yeongyulgori.user.constant.Role.BUSINESS_USER;
import static personal.yeongyulgori.user.constant.Role.GENERAL_USER;
import static personal.yeongyulgori.user.testutil.TestObjectFactory.*;

@ActiveProfiles("test")
@WebMvcTest(controllers = AuthenticationController.class)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private AutoCompleteService autoCompleteService;

    @MockBean
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        userRepository.deleteAllInBatch();
    }

    @DisplayName("사용자가 올바른 회원 가입 양식을 입력하면 회원 가입을 할 수 있다.")
    @Test
    void signUpUser() throws Exception {

        // given

        SignUpForm signUpForm = enterUserFormWithAddress(
                "abcd@abc.com", "gildong1234", "1234", "홍길동",
                LocalDate.of(2000, 1, 1), "01012345678", GENERAL_USER
        );

        UserResponseDto userResponseDto = UserResponseDto.of(
                "abcd@abc.com", "gildong1234", "홍길동", GENERAL_USER
        );


        when(authenticationService.signUpUser(any(SignUpForm.class))).thenReturn(userResponseDto);

        // when, then

        mockMvc.perform(
                        post("/users/v1/sign-up")
                                .content(objectMapper.writeValueAsString(signUpForm))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isCreated());

    }

    @DisplayName("올바른 로그인 양식을 입력하면 로그인을 할 수 있다.")
    @Test
    void signInUser() throws Exception {

        // given

        SignUpForm signUpForm = enterUserFormWithAddress(
                "abcd@abc.com", "gildong1234", "1234", "홍길동",
                LocalDate.of(2000, 1, 1), "01012345678", Role.GENERAL_USER
        );

        // when, then

        mockMvc.perform(
                        post("/users/v1/sign-in")
                                .content(objectMapper.writeValueAsString(signUpForm))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());

    }

    @DisplayName("사용자 이름을 입력하면 회원 개인정보를 조회할 수 있다.")
    @Test
    void getUserInformation() throws Exception {

        // given
        String username = "gildong1234";

        User user = createUserWithAddress(
                "abcd@abc.com", username, "1234", "홍길동",
                LocalDate.of(2000, 1, 1), "01012345678", Role.GENERAL_USER
        );

        userRepository.save(user);

        UserResponseDto userResponseDto = UserResponseDto.from(user);

        when(authenticationService.getUserDetails(username)).thenReturn(userResponseDto);

        // when, then
        mockMvc.perform(
                        get("/users/v1/{username}/details", username)
                )
                .andDo(print())
                .andExpect(status().isOk());

    }

    @DisplayName("하나의 수정할 변수를 입력해 회원 정보를 수정할 수 있다.")
    @Test
    void updateUserInformationWithOneVariable() throws Exception {

        // given

        String username = "gildong1234";

        User user = createUser(
                "abcd@abc.com", username, "1234", "홍길동",
                LocalDate.of(2000, 1, 1), "01012345678", GENERAL_USER
        );

        userRepository.save(user);

        InformationUpdateForm informationUpdateForm = InformationUpdateForm.builder()
                .id(1L)
                .name("고길동")
                .build();

        UserResponseDto userResponseDto = UserResponseDto.from(user);

        when(authenticationService
                .updateUserInformation(any(String.class), any(InformationUpdateForm.class)))
                .thenReturn(userResponseDto);

        // when, then

        mockMvc.perform(
                        patch("/users/v1/{username}", username)
                                .content(objectMapper.writeValueAsString(informationUpdateForm))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());

    }

    @DisplayName("여러 개의 수정할 변수를 입력해 회원 정보를 수정할 수 있다.")
    @Test
    void updateUserInformationWithVariables() throws Exception {

        // given

        String username = "gildong1234";

        User user = createUser(
                "abcd@abc.com", username, "1234", "홍길동",
                LocalDate.of(2000, 1, 1), "01012345678", GENERAL_USER
        );

        userRepository.save(user);

        InformationUpdateForm informationUpdateForm = InformationUpdateForm.builder()
                .id(1L)
                .name("고길동")
                .role(BUSINESS_USER)
                .address(Address.builder()
                        .city("부산")
                        .build())
                .build();

        UserResponseDto userResponseDto = UserResponseDto.from(user);

        when(authenticationService
                .updateUserInformation(any(String.class), any(InformationUpdateForm.class)))
                .thenReturn(userResponseDto);

        // when, then

        mockMvc.perform(
                        patch("/users/v1/{username}", username)
                                .content(objectMapper.writeValueAsString(informationUpdateForm))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isOk());

    }

    @DisplayName("하나의 수정할 변수를 입력해 중요한 회원 정보를 수정할 수 있다.")
    @Test
    void updateUserInformationWithAuthentication() throws Exception {

        // given

        String username = "gildong1234";

        User user = createUser(
                "abcd@abc.com", username, "1234", "홍길동",
                LocalDate.of(2000, 1, 1), "01012345678", GENERAL_USER
        );

        userRepository.save(user);

        CrucialInformationUpdateDto crucialInformationUpdateDto = CrucialInformationUpdateDto.builder()
                .id(1L)
                .email("abcd@abcd.com")
                .build();

        // when, then

        mockMvc.perform(
                        patch("/users/v1/{username}/auth", username)
                                .content(objectMapper.writeValueAsString(crucialInformationUpdateDto))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isNoContent());

    }

    @DisplayName("이메일 인증을 통해 비밀번호 재설정을 요청할 수 있다.")
    @Test
    void requestPasswordReset() throws Exception {

        // given

        User user = createUser(
                "abcd@abc.com", "gildong1234", "1234", "홍길동",
                LocalDate.of(2000, 1, 1), "01012345678", GENERAL_USER
        );

        userRepository.save(user);

        // when, then

        mockMvc.perform(
                        post("/users/v1/password-reset/request")
                                .content(objectMapper.writeValueAsString(user.getEmail()))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isNoContent());

    }

    @DisplayName("이메일 인증에 성공하면 토큰을 통해 비밀번호를 재설정할 수 있다.")
    @Test
    void resetPassword() throws Exception {

        // given

        User user = createUser(
                "abcd@abc.com", "gildong1234", "1234", "홍길동",
                LocalDate.of(2000, 1, 1), "01012345678", GENERAL_USER
        );

        userRepository.save(user);

        String token = "abcdefghijklmnopqrstuvwxyz";

        // when, then

        mockMvc.perform(
                        patch("/users/v1/password-reset")
                                .param("token", token)
                                .param("password", user.getPassword())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isNoContent());

    }

    @DisplayName("비밀번호 인증을 통해 회원을 탈퇴할 수 있다.")
    @Test
    void deleteUser() throws Exception {

        // given

        User user = createUser(
                "abcd@abc.com", "gildong1234", "1234", "홍길동",
                LocalDate.of(2000, 1, 1), "01012345678", GENERAL_USER
        );

        userRepository.save(user);

        // when, then

        mockMvc.perform(
                        delete("/users/v1/{username}", user.getUsername())
                                .param("username", user.getUsername())
                                .param("password", user.getPassword())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(print())
                .andExpect(status().isNoContent());

    }

}